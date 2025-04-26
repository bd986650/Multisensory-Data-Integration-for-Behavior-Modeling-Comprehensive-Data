import Foundation
import HealthKit
import SwiftUI

class HealthViewModel: ObservableObject {
    private var healthStore = HKHealthStore()
    
    @AppStorage("jwtToken") var jwtToken: String = ""
    @AppStorage("refreshToken") var refreshToken: String = ""

    private var anchor: HKQueryAnchor?

    // Обязательно добавьте @Published
    @Published var healthDataHistory: [HealthDataModel] = []

    // Функция для запроса авторизации
    func requestAuthorization() {
        let typesToRead: Set<HKObjectType> = [
            HKQuantityType.quantityType(forIdentifier: .stepCount)!
        ]

        healthStore.requestAuthorization(toShare: nil, read: typesToRead) { success, error in
            if success {
                print("HealthKit: Авторизация получена")
                self.startObservingSteps()
            } else {
                print("HealthKit: Ошибка авторизации - \(error?.localizedDescription ?? "неизвестная ошибка")")
            }
        }
    }

    // Функция для начала наблюдения за шагами
    func startObservingSteps() {
        let query = HKObserverQuery(sampleType: HKQuantityType.quantityType(forIdentifier: .stepCount)!, predicate: nil) { _, _, error in
            if let error = error {
                print("HealthKit: Ошибка ObserverQuery - \(error.localizedDescription)")
                return
            }
            self.fetchNewStepData()
        }

        healthStore.execute(query)
        healthStore.enableBackgroundDelivery(for: HKQuantityType.quantityType(forIdentifier: .stepCount)!, frequency: .immediate) { success, error in
            if let error = error {
                print("HealthKit: Ошибка фона - \(error.localizedDescription)")
            }
        }
    }

    // Функция для получения новых данных шагов
    func fetchNewStepData() {
        let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
        let query = HKAnchoredObjectQuery(type: stepType, predicate: nil, anchor: anchor, limit: HKObjectQueryNoLimit) { query, samples, _, newAnchor, error in
            guard let samples = samples as? [HKQuantitySample], error == nil else { return }
            self.anchor = newAnchor

            let newEntries = samples.map { sample in
                let stepCount = sample.quantity.doubleValue(for: HKUnit.count())
                let startTime = sample.startDate
                let endTime = sample.endDate

                return HealthDataModel(
                    timestamp: startTime,
                    data: [
                        "Steps": stepCount,
                        "Start Time": startTime.timeIntervalSince1970,
                        "End Time": endTime.timeIntervalSince1970
                    ]
                )
            }

            // Обновление history
            DispatchQueue.main.async {
                self.healthDataHistory.append(contentsOf: newEntries)
            }

            // Отправка шагов на сервер
            self.sendStepsToServer(steps: newEntries)
        }

        healthStore.execute(query)
    }

    // Новая отправка шагов на сервер
    func sendStepsToServer(steps: [HealthDataModel]) {
        guard let token = UserDefaults.standard.string(forKey: "jwtToken") else {
            print("Ошибка: Токен отсутствует")
            return
        }
        
        // Считаем общее количество шагов
        let totalSteps = steps.reduce(0) { $0 + ($1.data["Steps"] ?? 0) }
        
        // Берем максимальный timestamp среди новых шагов
        let maxTimestamp = steps.map { $0.timestamp.timeIntervalSince1970 }.max() ?? Date().timeIntervalSince1970
        
        let requestBody: [String: Any] = [
            "type": "steps",
            "value": Int(totalSteps),
            "timestamp": Int(maxTimestamp)
        ]
        
        guard let url = URL(string: "\(Constants.baseURL)/api/users/save"),
              let jsonData = try? JSONSerialization.data(withJSONObject: requestBody) else {
            print("Ошибка формирования запроса")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData

        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("Ошибка запроса: \(error.localizedDescription)")
                    return
                }
                
                if let httpResponse = response as? HTTPURLResponse {
                    if httpResponse.statusCode == 200 {
                        print("✅ Шаги успешно отправлены на сервер!")
                    } else if httpResponse.statusCode == 401 {
                        print("⚠️ Токен устарел, обновляем...")
                        self?.refreshJWTToken { success in
                            if success {
                                self?.sendStepsToServer(steps: steps) // Повторная отправка всего пакета шагов
                            } else {
                                print("⛔ Ошибка обновления токена")
                            }
                        }
                    } else {
                        print("Ошибка: Сервер вернул код \(httpResponse.statusCode)")
                        if let data = data, let errorMessage = String(data: data, encoding: .utf8) {
                            print("Сообщение об ошибке: \(errorMessage)")
                        }
                    }
                }
            }
        }.resume()
    }


    private func refreshJWTToken(completion: @escaping (Bool) -> Void) {
        guard let url = URL(string: "\(Constants.baseURL)/api/auth/refresh") else {
            completion(false)
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(refreshToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("Ошибка обновления токена: \(error.localizedDescription)")
                completion(false)
                return
            }

            if let httpResponse = response as? HTTPURLResponse {
                guard httpResponse.statusCode == 200 else {
                    print("Ошибка обновления токена: Сервер вернул \(httpResponse.statusCode)")
                    completion(false)
                    return
                }

                if let data = data {
                    do {
                        let tokenResponse = try JSONDecoder().decode(TokenResponse.self, from: data)
                        DispatchQueue.main.async {
                            self.jwtToken = tokenResponse.jwt
                            self.refreshToken = tokenResponse.refresh

                            UserDefaults.standard.set(tokenResponse.jwt, forKey: "jwtToken")
                            UserDefaults.standard.set(tokenResponse.refresh, forKey: "refreshToken")
                        }
                        completion(true)
                    } catch {
                        print("Ошибка декодирования нового токена: \(error.localizedDescription)")
                        completion(false)
                    }
                }
            }
        }.resume()
    }
}
