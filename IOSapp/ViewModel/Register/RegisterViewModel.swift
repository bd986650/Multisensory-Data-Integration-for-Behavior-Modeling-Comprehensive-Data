//
//  RegisterViewModel.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 04.12.2024.
//

import Foundation
import SwiftUI

class RegisterViewModel: ObservableObject {
    @Published var username: String = ""
    @Published var password: String = ""
    
    @AppStorage("jwtToken") var jwtToken: String = ""
    
    @Published var userIsRegistred: Bool = false
    
    @Published var showPSWDField: Bool = false
    
    @Published var errorMessage: String = ""
    @Published var showError: Bool = false
    
    @Published var userAlreadyRegistred: Bool = false
    
    @AppStorage("isRegistered") private var isRegistered: Bool = false
    @AppStorage("isLogined") private var isLogined: Bool = false
    
    @StateObject var settingsVM: SettingsViewModel = .init()
    
    func checkIsCorrectUsername() -> Bool {
        // Правила для имени пользователя:
        // Минимум 3 символа, максимум 20, только буквы и цифры
        let usernameRegex = "^[a-zA-Z0-9]{3,20}$"
        let usernamePredicate = NSPredicate(format: "SELF MATCHES %@", usernameRegex)
        return usernamePredicate.evaluate(with: username)
    }
    
    func checkIsCorrectPassword() -> Bool {
        // Правила для пароля:
        // Минимум 8 символов, хотя бы одна буква, одна цифра и один специальный символ
        let passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$"
        let passwordPredicate = NSPredicate(format: "SELF MATCHES %@", passwordRegex)
        return passwordPredicate.evaluate(with: password)
    }
    
    func acceptInputDataAndStartRegistration() -> Bool {
        return checkIsCorrectUsername() && checkIsCorrectPassword()
    }
        
    func isShowPSWDField() {
        showPSWDField = username != "" ? true : false
    }
    
    func registerUserAndRequestToken(username: String, password: String) {
        guard let url = URL(string: "http://172.20.10.14:8080/api/auth/register?username=\(username)&password=\(password)")
        else {
            setError(message: "Неверный URL")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            guard let self = self else { return }
            
            guard error == nil else {
                self.setError(message: "Ошибка запроса: \(error!.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                print("Код ответа: \(httpResponse.statusCode)")
                
                if httpResponse.statusCode == 200 {
                    isRegistered = true
                    
                    settingsVM.username = username
                    settingsVM.password = password
                    
                    print("Запрос успешен")
                    
                    if let data = data, let token = String(data: data, encoding: .utf8) {
                        settingsVM.jwtToken = token
                        jwtToken = token
                        print("Полученный токен: \(token)")
                        UserDefaults.standard.set(token, forKey: "jwtToken")
                    }
                }
                else if httpResponse.statusCode == 400 {
                    userAlreadyRegistred = true
                }
                else {
                    self.setError(message: "Ошибка: Сервер вернул код \(httpResponse.statusCode)")
                    print("error, \(httpResponse.statusCode)")
                }
            }
        }.resume()
    }
    
    private func setError(message: String) {
        DispatchQueue.main.async {
            self.errorMessage = message
            self.showError = true
        }
    }
}


