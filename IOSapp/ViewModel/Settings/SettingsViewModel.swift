//
//  SettingsViewModel.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 03.12.2024.
//

import Foundation
import SwiftUI

class SettingsViewModel: ObservableObject {
    @AppStorage("username") var username: String = ""
    @AppStorage("password") var password: String = ""
    
    @AppStorage("jwtToken") var jwtToken: String = ""
    
    @AppStorage("firstName") var firstName: String = ""
    @AppStorage("lastName") var lastName: String = ""
    
    @AppStorage("weight") var weight: String = ""
    @AppStorage("height") var height: String = ""
}
