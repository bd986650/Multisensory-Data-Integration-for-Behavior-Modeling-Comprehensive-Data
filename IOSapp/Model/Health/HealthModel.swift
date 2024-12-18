//
//  HealthModel.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 04.12.2024.
//

import Foundation

struct HealthDataModel: Identifiable, Codable {
    var id = UUID()
    var timestamp: Date
    var data: [String: Double]
}
