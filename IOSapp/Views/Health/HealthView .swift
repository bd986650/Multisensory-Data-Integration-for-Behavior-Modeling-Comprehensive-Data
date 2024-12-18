//
//  HealthView .swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 14.10.2024.
//

import SwiftUI
import Foundation
import HealthKit

struct HealthView: View {
    @StateObject var healthVM = HealthViewModel()
    
    @State var healthData: [String: Double] = [:]
    
    var body: some View {
        Section {
            List(healthData.keys.sorted(), id: \.self) { key in
                if let value = healthData[key] {
                    HStack {
                        Text("\(key):")
                        Spacer()
                        Text("\(value, specifier: "%.2f")")
                    }
                }
            }
            
            Button {
                healthVM.requestAuthorization()
            } label: {
                Text(("Request access to health data"))
            }
            
            Button {
                healthVM.getHealthData { data in
                    healthData = data
                }
            } label: {
                Text("Get health data")
            }
        } header: {
            Text("Health")
        }
        .onAppear {
            healthVM.requestAuthorization()
            healthVM.loadHealthDataFromUserDefaults()
            healthVM.startBackgroundTask()
        }
        .onDisappear {
            healthVM.stopBackgroundTask()
        }
    }
}

#Preview {
    HealthView()
}
