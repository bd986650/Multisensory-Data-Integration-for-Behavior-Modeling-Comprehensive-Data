//
//  TokenResponse.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 18.03.2025.
//


struct TokenResponse: Codable {
    let jwt: String
    let refresh: String
}