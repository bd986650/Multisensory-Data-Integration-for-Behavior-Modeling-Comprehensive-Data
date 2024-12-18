//
//  ContentView.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 03.10.2024.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        NavigationStack {
            TabView {
                Form {
                    HealthView()
                    
                    LocationView()
                    
                }
                .tabItem {
                    VStack {
                        Image(systemName: "target")
                        
                        Text("Activity")
                    }
                }
                
                SettingsView()
                    .tabItem {
                        VStack {
                            Image(systemName: "gear")
                            
                            Text("Settings")
                        }
                    }
            }
        }
    }
}

#Preview {
    ContentView()
}

