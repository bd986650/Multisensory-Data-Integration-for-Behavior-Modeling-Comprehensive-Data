//
//  test.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 03.12.2024.
//

import SwiftUI

struct test: View {
    @State private var isLoading: Bool = false
    @State private var resultMessage: String = ""
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Отправить данные на сервер")
                .font(.title)
                .padding(.top, 50)
            
            Button(action: {
                sendDataToServer()
            }) {
                HStack {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Отправить")
                            .fontWeight(.bold)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .foregroundColor(.white)
                .background(Color.blue)
                .cornerRadius(10)
            }
            .disabled(isLoading)
            .padding(.horizontal, 20)
            
            Text(resultMessage)
                .foregroundColor(resultMessage.contains("Ошибка") ? .red : .green)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 20)
        }
        .padding()
    }
    
    func sendDataToServer() {
        isLoading = true
        resultMessage = ""
        
        let token = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOiJlMTZjZTkwNC1kMzIzLTQ1MGItOWZiNC0xZjlhZDc4ODJjNjEiLCJzdWIiOiJEYW5pbCIsImlhdCI6MTczMzQ3MTA1NSwiZXhwIjoxNzMzNTU3NDU1fQ.FNWiLzFQQB5QzzaWilSxa0SRZZ8POr6s2DbHI5WTHr0p8N6NetPM1HH98MaHxSm9XS1fjS_dIOun9bhwz6_oTA"
        let jsonData =
            """
            {
                "theme": "dark",
            }
            """
        
        guard let url = URL(string: "http://172.20.10.14:8080/api/users/save-data") else {
            resultMessage = "Ошибка: Неверный URL"
            isLoading = false
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData.data(using: .utf8)
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                
                if let error = error {
                    resultMessage = "Ошибка запроса: \(error.localizedDescription)"
                    return
                }
                
                if let httpResponse = response as? HTTPURLResponse {
                    if httpResponse.statusCode == 200 {
                        resultMessage = "Данные успешно сохранены!"
                        
                        if let data = data, let responseString = String(data: data, encoding: .utf8) {
                            print("Ответ сервера: \(responseString)")
                        }
                    } else {
                        resultMessage = "Ошибка: Сервер вернул код \(httpResponse.statusCode)"
                        
                        if let data = data, let errorMessage = String(data: data, encoding: .utf8) {
                            print("Сообщение об ошибке: \(errorMessage)")
                        }
                    }
                }
            }
        }.resume()
    }
}

#Preview {
    test()
}
