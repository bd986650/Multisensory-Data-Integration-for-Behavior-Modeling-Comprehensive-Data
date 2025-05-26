//
//  ContentView.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 03.10.2024.
//

import SwiftUI

struct ContentView: View {
    @AppStorage("isLogined") private var isLogined: Bool = false
    
    var body: some View {
        NavigationStack {
            Form {
                HealthView()
                
                LocationView()
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        withAnimation {
                            isLogined = false
                        }
                    }) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

#Preview {
    ContentView()
}

