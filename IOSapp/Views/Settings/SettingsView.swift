//
//  SettingsView.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 03.12.2024.
//

import SwiftUI

struct SettingsView: View {
    
    @StateObject var settingsVM: SettingsViewModel = .init()
    
    @State var showProfileSettingsView: Bool = false
    @State var showPassword: Bool = false
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    Text("Username: \(settingsVM.username)")
                    Toggle(showPassword ? "Hide password" : "Show password", isOn: $showPassword.animation())
                        .tint(.blue)
                    if showPassword {
                        Text("Password: \(settingsVM.password)")
                    }
                }
                
                Section {
                    Text("Height: \(settingsVM.height)")
                    Text("Weight: \(settingsVM.weight)")
                }
                
                
                Section {
                    NavigationLink(destination: HealthView()) {
                        HStack {
                            Image(systemName: "heart")
                            
                            Text("Health")
                        }
                    }
                }
                
                Section {
                    NavigationLink(destination: HealthView()) {
                        HStack {
                            Image(systemName: "map")
                            
                            Text("Geolocation")
                        }
                    }
                }
                
                Section {
                    
                }
            }
            
            
            .navigationTitle(Text("\(settingsVM.firstName) \(settingsVM.lastName)"))
            .fullScreenCover(isPresented: $showProfileSettingsView) {
                ProfileSettingsView(firstName: $settingsVM.firstName, lastName: $settingsVM.lastName, username: settingsVM.username, password: settingsVM.password, jwtToken: settingsVM.jwtToken, height: $settingsVM.height, weight: $settingsVM.weight)
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        self.showProfileSettingsView = true
                    } label: {
                        Image(systemName: "square.and.pencil")
                    }
                    
                }
            }
        }
    }
}

#Preview {
    SettingsView()
}
