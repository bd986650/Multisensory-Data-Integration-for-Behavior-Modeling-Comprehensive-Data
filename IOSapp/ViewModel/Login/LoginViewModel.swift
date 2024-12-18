//
//  LoginViewModel.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 02.12.2024.
//

import Foundation
import SwiftUI

class LoginViewModel: ObservableObject {
    @Published var username: String = ""
    @Published var password: String = ""
    
    @AppStorage("jwtToken") var jwtToken: String = ""
    
    @Published var showPSWDField: Bool = false
    
    @Published var errorMessage: String = ""
    @Published var showError: Bool = false
    
    @Published var isInputDataIncorrect: Bool = false
    
    @AppStorage("isRegistered") private var isRegistered: Bool = false
    
    @StateObject var settingsVM: SettingsViewModel = .init()
        
    func isShowPSWDField() {
        showPSWDField = username != "" ? true : false
    }
    
    func loginUser(username: String, password: String) {
        guard let url = URL(string: "http://172.20.10.14:8080/api/auth/login?username=\(username)&password=\(password)") else {
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
                
                if httpResponse.statusCode == 200 {
                    print("Авторизация успешна")
                    
                    if let data = data, let token = String(data: data, encoding: .utf8) {
                        settingsVM.jwtToken = token
                        jwtToken = token
                        print("Полученный токен: \(token)")
                        UserDefaults.standard.set(token, forKey: "jwtToken")
                    }
                } else if httpResponse.statusCode == 401 {
                    isInputDataIncorrect = true
                    self.setError(message: "Ошибка авторизации: Неверные учетные данные")
                } else {
                    self.setError(message: "Ошибка: Сервер вернул код \(httpResponse.statusCode)")
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


