import Foundation
import SwiftUI

class LoginViewModel: ObservableObject {
    @Published var username: String = ""
    @Published var password: String = ""

    @AppStorage("jwtToken") var jwtToken: String = ""
    @AppStorage("refreshToken") var refreshToken: String = ""

    @Published var showPSWDField: Bool = false
    @Published var errorMessage: String = ""
    @Published var showError: Bool = false
    @AppStorage("isRegistered") private var isRegistered: Bool = false
    @AppStorage("isLogined") private var isLogined: Bool = false
    
    func isShowPSWDField() {
        showPSWDField = !username.isEmpty
    }

    func loginUser(username: String, password: String) {
        guard let url = URL(string: "\(Constants.baseURL)/api/auth/login?username=\(username)&password=\(password)") else {
            setError(message: "Неверный URL")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            guard let self = self else { return }
            
            if let error = error {
                self.setError(message: "Ошибка запроса: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                print("Код ответа: \(httpResponse.statusCode)")
                
                guard httpResponse.statusCode == 200 else {
                    self.setError(message: "Ошибка: Сервер вернул код \(httpResponse.statusCode)")
                    return
                }
                
                if let data = data {
                    do {
                        // Декодируем данные в структуру LoginResponse
                        let tokenResponse = try JSONDecoder().decode(LoginResponse.self, from: data)
                        DispatchQueue.main.async {
                            self.jwtToken = tokenResponse.accessToken // Используем accessToken
                            self.refreshToken = tokenResponse.refreshToken // Используем refreshToken
                            
                            print("JWT: \(tokenResponse.accessToken)") // Выводим accessToken
                            print("Refresh Token: \(tokenResponse.refreshToken)") // Выводим refreshToken
                            
                            UserDefaults.standard.set(tokenResponse.accessToken, forKey: "jwtToken")
                            UserDefaults.standard.set(tokenResponse.refreshToken, forKey: "refreshToken")
                            
                            self.isLogined = true
                        }
                    } catch {
                        self.setError(message: "Ошибка декодирования JSON: \(error.localizedDescription)")
                    }
                }
            }
        }.resume()
    }

    // Ответ сервера при логине (JWT + Refresh Token)
    struct LoginResponse: Codable {
        let accessToken: String
        let refreshToken: String
    }

    private func setError(message: String) {
        DispatchQueue.main.async {
            self.errorMessage = message
            self.showError = true
        }
    }
}

