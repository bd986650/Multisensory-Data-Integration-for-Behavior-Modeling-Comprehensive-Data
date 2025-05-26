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
            if isRegistred {
                if isLogined {
                    ContentView()
                } else {
                    LoginView(userIsRegistred: $isRegistred, isLogined: $isLogined)
                        .ignoresSafeArea(.keyboard)
                }
            } else {
                RegisterView(userIsRegistred: $isRegistred, isLogined: $isLogined)
                    .ignoresSafeArea(.keyboard)
            }
        }
    }
}




