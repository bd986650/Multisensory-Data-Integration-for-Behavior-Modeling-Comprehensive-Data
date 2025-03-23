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
        reverseGeocode(location)
        checkAndStoreLocation(location)
    }

    private func reverseGeocode(_ location: CLLocation) {
        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, error in
            guard let self = self else { return }
            if let placemark = placemarks?.first {
                self.locationName = [placemark.name, placemark.locality, placemark.administrativeArea, placemark.country]
                    .compactMap { $0 }
                    .joined(separator: ", ")
            } else {
                self.locationName = "Unknown Location"
                print("Ошибка обратного геокодирования: \(error?.localizedDescription ?? "Unknown error")")
            }
        }
    }

    private func checkAndStoreLocation(_ newLocation: CLLocation) {
        guard let lastLocation = savedLocations.last else {
            savedLocations.append(newLocation.coordinate)
            sendLocationToServer(newLocation.coordinate)
            return
        }

        let distance = newLocation.distance(from: CLLocation(latitude: lastLocation.latitude, longitude: lastLocation.longitude))

        if distance > 100 {
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

//        let locationData: [String: Any] = [
//            "value": "\(coordinate.latitude):\(coordinate.longitude)",
//            "type": "coordinates",
//            "timestamp": Int(Date().timeIntervalSince1970) // Время в секундах с 1970
//        ]
//
//        guard let jsonData = try? JSONSerialization.data(withJSONObject: locationData) else {
//            print("Ошибка сериализации JSON")
//            return
//        }

//        guard let url = URL(string: "\(Constants.baseURL)/api/users/save") else {
//            print("Ошибка: Неверный URL")
//            return
//        }
//
//        var request = URLRequest(url: url)
//        request.httpMethod = "POST"
//        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
//        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
//        request.httpBody = jsonData
        
        let urlString = "\(Constants.baseURL)/api/users/save?type=coordinates&value=\(coordinate.latitude):\(coordinate.longitude)&timestamp=\(Int(Date().timeIntervalSince1970))"
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


    // 🚀 Функция обновления токена
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

                if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200, let data = data {
                    do {
                        if let json = try JSONSerialization.jsonObject(with: data) as? [String: String],
                           let newToken = json["jwtToken"],
                           let newRefreshToken = json["refreshToken"] {
                            UserDefaults.standard.set(newToken, forKey: "jwtToken")
                            UserDefaults.standard.set(newRefreshToken, forKey: "refreshToken")
                            print("✅ Токен успешно обновлен")
                            completion(true)
                        } else {
                            print("Ошибка парсинга ответа сервера")
                            completion(false)
                        }
                    } catch {
                        print("Ошибка обработки JSON: \(error.localizedDescription)")
                        completion(false)
                    }
                } else {
                    print("Ошибка обновления токена: Сервер вернул некорректный ответ")
                    completion(false)
                }
            }
        }.resume()
    }
}
