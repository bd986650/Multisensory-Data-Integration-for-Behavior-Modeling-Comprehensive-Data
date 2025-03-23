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
            Form {
                HealthView()
                
                LocationView()
            }
        }
    }
}

#Preview {
    ContentView()
}

