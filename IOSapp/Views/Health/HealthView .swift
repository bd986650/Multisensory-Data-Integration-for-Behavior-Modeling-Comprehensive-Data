import SwiftUI

struct HealthView: View {
    @StateObject var healthVM = HealthViewModel()
    @State var healthData: [String: Double] = [:]
    
    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()

    var body: some View {
        Section {
            List(healthData.keys.sorted(), id: \.self) { key in
                if let value = healthData[key] {
                    HStack {
                        Text("\(key):")
                        Spacer()
                        if key == "Start Time" || key == "End Time" {
                            Text("\(Date(timeIntervalSince1970: value), formatter: dateFormatter)")
                        } else {
                            Text("\(value, specifier: "%.2f")")
                        }
                    }
                }
            }
            
            Button {
                healthVM.requestAuthorization()
            } label: {
                Text("Request access to health data")
            }

            Button {
                healthVM.fetchNewStepData() // ❗️ Используем правильную функцию
            } label: {
                Text("Get health data")
            }
        } header: {
            Text("Health Data")
        }
        .onAppear {
            healthVM.requestAuthorization()
            healthVM.fetchNewStepData()
        }
        .onReceive(healthVM.$healthDataHistory) { newHistory in
            DispatchQueue.main.async {
                if let latest = newHistory.last {
                    healthData = latest.data
                }
            }
        }
    }
}

#Preview {
    HealthView()
}

