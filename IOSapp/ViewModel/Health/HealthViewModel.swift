import Foundation
import HealthKit
import SwiftUI

class HealthViewModel: ObservableObject {
    private var healthStore = HKHealthStore()

    @Published var healthDataHistory: [HealthDataModel] = []
    @AppStorage("jwtToken") var jwtToken: String = ""
    @AppStorage("refreshToken") var refreshToken: String = ""

    private var anchor: HKQueryAnchor?

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

            DispatchQueue.main.async {
                self.healthDataHistory.append(contentsOf: newEntries)
                self.saveHealthDataToUserDefaults()
                self.sendStepsToServer(steps: newEntries)
            }
        }

        healthStore.execute(query)
    }

    func sendStepsToServer(steps: [HealthDataModel]) {
        guard let token = UserDefaults.standard.string(forKey: "jwtToken") else {
            print("Ошибка: Токен отсутствует")
            return
        }

        // Для каждого шага создаём строку в формате timestamp:steps_count
        let stepsValue = steps.map { step in
            return "\(Int(step.timestamp.timeIntervalSince1970)):\(step.data["Steps"] ?? 0)"
        }.joined(separator: ",")

        // Формируем URL строку с шагами
        let urlString = "\(Constants.baseURL)/api/users/save?type=steps&value=\(stepsValue)&timestamp=\(Int(Date().timeIntervalSince1970))"
        
        guard let url = URL(string: urlString) else {
            print("Ошибка: Неверный URL")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("Ошибка запроса: \(error.localizedDescription)")
                    return
                }

                if let httpResponse = response as? HTTPURLResponse {
                    if httpResponse.statusCode == 200 {
                        print("Шаги успешно сохранены на сервере!")
                    } else if httpResponse.statusCode == 401 {
                        print("⚠️ Токен устарел, обновляем...")
                        self?.refreshJWTToken { success in
                            if success {
                                self?.sendStepsToServer(steps: steps) // Повторная отправка
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

    private func saveHealthDataToUserDefaults() {
        if let encodedData = try? JSONEncoder().encode(healthDataHistory) {
            UserDefaults.standard.set(encodedData, forKey: "healthDataHistory")
        }
    }
}
