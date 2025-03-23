//
//  MutlisensoryDataIntegrationApp.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 03.10.2024.
//

import SwiftUI

@main
struct MutlisensoryDataIntegrationApp: App {
    @AppStorage("isRegistred") private var isRegistred: Bool = false
    @AppStorage("isLogined") private var isLogined: Bool = false
    
    var body: some Scene {
        WindowGroup {
            // Если пользователь зарегистрирован, проверяем, вошел ли он
            if isRegistred {
                if isLogined {
                    // Если пользователь залогинен, показываем ContentView
                    ContentView()
                } else {
                    // Если не залогинен, показываем LoginView
                    LoginView(userIsRegistred: $isRegistred, isLogined: $isLogined)
                }
            } else {
                // Если пользователь не зарегистрирован, показываем RegisterView
                RegisterView(userIsRegistred: $isRegistred, isLogined: $isLogined)
            }
        }
    }
}



