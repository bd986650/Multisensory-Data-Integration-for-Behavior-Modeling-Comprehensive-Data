//
//  ProfileSettingsView.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 04.12.2024.
//

import SwiftUI

struct ProfileSettingsView: View {
    
    @Environment(\.presentationMode) var presentationMode
    
    @Binding var firstName: String
    @Binding var lastName: String

    var username: String
    var password: String
    var jwtToken: String
    
    @Binding var height: String
    @Binding var weight: String

    @State var spoilerIsOn: Bool = true
    @State var isShowInformation: Bool = false
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("First Name", text: $firstName)
                    TextField("Last Name", text: $lastName)
                } footer: {
                    Text("Enter your name and optional profile color")
                }
                
                Section {
                    TextField("Your Height", text: $height)
                        .keyboardType(.numberPad)
                    TextField("Your Weight", text: $weight)
                        .keyboardType(.numberPad)
                } footer: {
                    Text("Enter your height and weight")
                }
                
                Section {
                    Toggle(isShowInformation ? "Hide security information" : "Show security information", isOn: $isShowInformation.animation())
                        .tint(.blue)
                    
                    if isShowInformation {
                        Text("\(username)")
                            .onLongPressGesture {
                                UIPasteboard.general.string = username
                            }
                        
                        Text("\(password)")
                            .spoiler(isOn: $spoilerIsOn)
                            .onLongPressGesture {
                                UIPasteboard.general.string = password
                            }
                        
                        Text("\(jwtToken)")
                            .spoiler(isOn: $spoilerIsOn)
                            .onLongPressGesture {
                                UIPasteboard.general.string = jwtToken
                            }
                    }
                } footer: {
                    Text("Don't show this information other people")
                }
            }
            .navigationBarTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Exit") {
                        self.presentationMode.wrappedValue.dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        self.presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
    }
}
