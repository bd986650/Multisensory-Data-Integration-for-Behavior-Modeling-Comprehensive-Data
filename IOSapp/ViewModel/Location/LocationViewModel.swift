import Foundation
import CoreLocation
import MapKit

struct CodableCoordinate: Codable {
    var latitude: Double
    var longitude: Double
    
    init(coordinate: CLLocationCoordinate2D) {
        self.latitude = coordinate.latitude
        self.longitude = coordinate.longitude
    }
}

final class LocationViewModel: NSObject, CLLocationManagerDelegate, ObservableObject {

    @Published var lastKnownLocation: CLLocationCoordinate2D?
    @Published var locationName: String = "Unknown Location"
    @Published var savedLocations: [CLLocationCoordinate2D] = []
    @Published var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194),
        span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
    )
    
    var manager = CLLocationManager()
    let geocoder = CLGeocoder()
    
    override init() {
        super.init()
        manager.delegate = self
    }
    
    func checkLocationAuthorization() {
        manager.startUpdatingLocation()
        
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
            
        case .restricted, .denied:
            print("Доступ к геолокации запрещен")
            
        case .authorizedAlways, .authorizedWhenInUse:
            print("Геолокация разрешена")
            if let location = manager.location {
                updateLocation(location)
            }
            
        @unknown default:
            print("Неизвестный статус геолокации")
        }
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        checkLocationAuthorization()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            updateLocation(location)
        }
    }
    
    private func updateLocation(_ location: CLLocation) {
        lastKnownLocation = location.coordinate
        region = MKCoordinateRegion(
            center: location.coordinate,
            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
        )
        requestReverseGeocoding(location: location)
        checkAndStoreLocation(location)
    }

    var lastGeocodeRequestTime: Date?

    func requestReverseGeocoding(location: CLLocation) {
        let now = Date()
        
        // Проверяем, когда был последний запрос
        if let lastRequest = lastGeocodeRequestTime, now.timeIntervalSince(lastRequest) < 1.2 {
            print("⏳ Пропускаем геокодирование, чтобы избежать лимитов")
            return
        }
        
        lastGeocodeRequestTime = now
        
        let geocoder = CLGeocoder()
        geocoder.reverseGeocodeLocation(location) { placemarks, error in
            if let error = error {
                print("Ошибка геокодирования: \(error.localizedDescription)")
                return
            }

            if let placemark = placemarks?.first {
                print("Местоположение: \(placemark.locality ?? "Неизвестно")")
            }
        }
    }

    private func checkAndStoreLocation(_ newLocation: CLLocation) {
        // Если список пустой — добавляем сразу
        guard let lastLocation = savedLocations.last else {
            savedLocations.append(newLocation.coordinate)
            sendLocationToServer(newLocation.coordinate)
            return
        }
        
        // Создаем CLLocation из последней сохраненной координаты
        let lastCLLocation = CLLocation(latitude: lastLocation.latitude, longitude: lastLocation.longitude)
        
        // Вычисляем расстояние
        let distance = newLocation.distance(from: lastCLLocation)
        
//        print("📏 Расстояние до последней точки: \(distance) м")
        
        // Отправляем на сервер только если расстояние > 100 
        if distance > 1 {
            savedLocations.append(newLocation.coordinate)
            sendLocationToServer(newLocation.coordinate)
        }
    }

    // 🚀 Функция отправки геолокации на сервер
    private func sendLocationToServer(_ coordinate: CLLocationCoordinate2D, retry: Bool = true) {
        guard let token = UserDefaults.standard.string(forKey: "jwtToken") else {
            print("Ошибка: Токен отсутствует")
            return
        }
        
        guard let url = URL(string: "\(Constants.baseURL)/api/users/save") else {
            print("Ошибка: Неверный URL")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let timestamp = Int(Date().timeIntervalSince1970)
        let value = "\(coordinate.latitude):\(coordinate.longitude)"
        
        let body: [String: Any] = [
            "type": "coordinates",
            "value": value,
            "timestamp": timestamp
        ]

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
        } catch {
            print("Ошибка сериализации JSON: \(error.localizedDescription)")
            return
        }

        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("Ошибка запроса: \(error.localizedDescription)")
                    return
                }

                if let httpResponse = response as? HTTPURLResponse {
                    if httpResponse.statusCode == 200 {
                        print("📍 Локация успешно сохранена на сервере!")
                    } else if httpResponse.statusCode == 401, retry {
                        print("⚠️ Токен устарел, обновляем...")
                        self?.refreshToken { success in
                            if success {
                                self?.sendLocationToServer(coordinate, retry: false)
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

    private func refreshToken(completion: @escaping (Bool) -> Void) {
        guard let refreshToken = UserDefaults.standard.string(forKey: "refreshToken") else {
            print("Ошибка: Refresh Token отсутствует")
            completion(false)
            return
        }

        guard let url = URL(string: "\(Constants.baseURL)/api/auth/refresh") else {
            print("Ошибка: Неверный URL")
            completion(false)
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let refreshData: [String: Any] = ["refreshToken": refreshToken]

        request.httpBody = try? JSONSerialization.data(withJSONObject: refreshData)

        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("Ошибка запроса обновления токена: \(error.localizedDescription)")
                    completion(false)
                    return
                }

                if let httpResponse = response as? HTTPURLResponse {
                    // Логируем статус ответа от сервера
                    print("Ответ от сервера: \(httpResponse.statusCode)")
                }

                // Логируем тело ответа
                if let data = data {
                    let responseBody = String(data: data, encoding: .utf8) ?? "Ошибка преобразования в строку"
                    print("Полученные данные: \(responseBody)")
                    
                    // Теперь предполагаем, что ответ - это новый JWT токен
                    let newToken = responseBody
                    if !newToken.isEmpty {
                        // Сохраняем новый токен
                        UserDefaults.standard.set(newToken, forKey: "jwtToken")
                        print("✅ Токен успешно обновлен: \(newToken)")
                        completion(true)
                    } else {
                        print("Ошибка: Новый токен пустой")
                        completion(false)
                    }
                } else {
                    print("Ошибка: Нет данных в ответе от сервера")
                    completion(false)
                }
            }
        }.resume()
    }
}
