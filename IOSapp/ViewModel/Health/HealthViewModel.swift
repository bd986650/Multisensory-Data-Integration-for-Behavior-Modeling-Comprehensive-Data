//
//  HealthStore.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 15.10.2024.
//

import Foundation
import HealthKit
import SwiftUI

class HealthViewModel: ObservableObject {
    private var healthStore = HKHealthStore()
    private var timer: Timer?
    
    @Published var healthDataHistory: [HealthDataModel] = []
    @StateObject private var settingsVM: SettingsViewModel = .init()

    let stepCountType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
    let distanceWalkingRunningType = HKQuantityType.quantityType(forIdentifier: .distanceWalkingRunning)!
    let activeEnergyBurnedType = HKQuantityType.quantityType(forIdentifier: .activeEnergyBurned)!
    let sleepAnalysisType = HKCategoryType.categoryType(forIdentifier: .sleepAnalysis)!
    let heartRateType = HKQuantityType.quantityType(forIdentifier: .heartRate)!
    let bodyMassType = HKQuantityType.quantityType(forIdentifier: .bodyMass)!
    let heightType = HKQuantityType.quantityType(forIdentifier: .height)!
    let bodyMassIndexType = HKQuantityType.quantityType(forIdentifier: .bodyMassIndex)!

    func requestAuthorization() {
        let typesToRead: Set<HKObjectType> = [
            stepCountType,
            distanceWalkingRunningType,
            activeEnergyBurnedType,
            sleepAnalysisType,
            heartRateType,
            bodyMassType,
            heightType,
            bodyMassIndexType
        ]
        
        healthStore.requestAuthorization(toShare: nil, read: typesToRead) { success, error in
            if success {
                print("Authorization granted")
            } else {
                print("Authorization denied: \(error?.localizedDescription ?? "Unknown error")")
            }
        }
    }
    
    func getHealthData(completion: @escaping ([String: Double]) -> Void) {
        let calendar = Calendar.current
        let startDate = calendar.startOfDay(for: Date())
        let endDate = Date()
        
        let predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: .strictEndDate)
        
        var healthData: [String: Double] = [:]
        
        let group = DispatchGroup()

        group.enter()
        getQuantityData(for: stepCountType, unit: HKUnit.count(), predicate: predicate) { steps in
            healthData["Steps"] = steps
            group.leave()
        }

        group.enter()
        getQuantityData(for: distanceWalkingRunningType, unit: HKUnit.meter(), predicate: predicate) { distance in
            healthData["Distance Walking/Running"] = distance
            group.leave()
        }

        group.enter()
        getQuantityData(for: activeEnergyBurnedType, unit: HKUnit.kilocalorie(), predicate: predicate) { calories in
            healthData["Active Energy Burned"] = calories
            group.leave()
        }

        group.enter()
        getSleepAnalysisData(predicate: predicate) { sleepDuration in
            healthData["Sleep Analysis (hours)"] = sleepDuration
            group.leave()
        }
        
        group.enter()
        getQuantityData(for: heartRateType, unit: HKUnit(from: "count/min"), predicate: predicate) { heartRate in
            healthData["Heart Rate (bpm)"] = heartRate
            group.leave()
        }
        
        group.enter()
        getQuantityData(for: bodyMassType, unit: HKUnit.gramUnit(with: .kilo), predicate: predicate) { bodyMass in
            healthData["Body Mass (kg)"] = Double(self.settingsVM.weight)
            group.leave()
        }
        
        group.enter()
        getQuantityData(for: heightType, unit: HKUnit.meter(), predicate: predicate) { height in
            healthData["Height (m)"] = Double(self.settingsVM.height)
            group.leave()
        }
        
        group.enter()
        getQuantityData(for: bodyMassIndexType, unit: HKUnit.count(), predicate: predicate) { bmi in
            let height = Double(self.settingsVM.height) ?? 0.0
            let weight = Double(self.settingsVM.weight)  ?? 0.0
            healthData["BMI"] = weight / ((height * height) / 10000)
            group.leave()
        }

        group.notify(queue: .main) {
            completion(healthData)
        }
    }

    private func getQuantityData(for type: HKQuantityType, unit: HKUnit, predicate: NSPredicate, completion: @escaping (Double) -> Void) {
        let query = HKSampleQuery(sampleType: type, predicate: predicate, limit: HKObjectQueryNoLimit, sortDescriptors: nil) { _, results, error in
            var total: Double = 0
            if let results = results as? [HKQuantitySample] {
                total = results.reduce(0) { $0 + $1.quantity.doubleValue(for: unit) }
            }
            DispatchQueue.main.async {
                completion(total)
            }
        }
        healthStore.execute(query)
    }

    private func getSleepAnalysisData(predicate: NSPredicate, completion: @escaping (Double) -> Void) {
        let query = HKSampleQuery(sampleType: sleepAnalysisType, predicate: predicate, limit: HKObjectQueryNoLimit, sortDescriptors: nil) { _, results, error in
            var sleepDuration: Double = 0
            if let results = results as? [HKCategorySample] {
                for result in results {
                    if result.value == HKCategoryValueSleepAnalysis.asleep.rawValue {
                        let sleepTime = result.endDate.timeIntervalSince(result.startDate) / 3600
                        sleepDuration += sleepTime
                    }
                }
            }
            DispatchQueue.main.async {
                completion(sleepDuration)
            }
        }
        healthStore.execute(query)
    }
    
    func startBackgroundTask() {
        // Таймер для сбора данных каждые 6 часов
        timer = Timer.scheduledTimer(withTimeInterval: 21600, repeats: true) { _ in
            self.getHealthData { data in
                let healthDataEntry = HealthDataModel(timestamp: Date(), data: data)
                self.healthDataHistory.append(healthDataEntry)
                self.saveHealthDataToUserDefaults()
            }
        }
    }
    
    func stopBackgroundTask() {
        timer?.invalidate()
    }
    
    private func saveHealthDataToUserDefaults() {
        if let encodedData = try? JSONEncoder().encode(healthDataHistory) {
            UserDefaults.standard.set(encodedData, forKey: "healthDataHistory")
        }
    }
    
    func loadHealthDataFromUserDefaults() {
        if let savedData = UserDefaults.standard.data(forKey: "healthDataHistory"),
           let decodedData = try? JSONDecoder().decode([HealthDataModel].self, from: savedData) {
            self.healthDataHistory = decodedData
        }
    }
}
