//
//  RegisterView.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 04.12.2024.
//

import SwiftUI

struct RegisterView: View {
    @StateObject var registerVM: RegisterViewModel = .init()
    
    @Binding var userIsRegistred: Bool
    @Binding var isLogined: Bool // Добавляем привязку к isLogined
    
    @State var errorIncorrectInputData: Bool = false
    
    var body: some View {
        GeometryReader { geometry in
            VStack {
                ScrollView(.vertical, showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 15) {
                        Image(systemName: "triangle")
                            .font(.system(size: 38))
                            .foregroundColor(.blue)
                        
                        (Text("Welcome,")
                            .foregroundColor(.black) +
                         Text("\nRegister to continue")
                            .foregroundColor(.gray)
                        )
                        .font(.title)
                        .fontWeight(.semibold)
                        .lineSpacing(10)
                        .padding(.top,20)
                        .padding(.trailing,15)
                        
                        CustomTextField(hint: "username", text: $registerVM.username)
                            .disabled(registerVM.showPSWDField)
                            .opacity(registerVM.showPSWDField ? 0.4 : 1)
                            .overlay(alignment: .trailing, content: {
                                Button("Change"){
                                    withAnimation(.easeInOut){
                                        registerVM.showPSWDField = false
                                    }
                                }
                                .font(.caption)
                                .foregroundColor(.indigo)
                                .opacity(registerVM.showPSWDField ? 1 : 0)
                                .padding(.trailing,15)
                            })
                            .padding(.top,50)
                        
                        CustomTextField(hint: "password", text: $registerVM.password)
                            .disabled(!registerVM.showPSWDField)
                            .opacity(!registerVM.showPSWDField ? 0 : 1)
                            .padding(.top,20)
                        
                        if errorIncorrectInputData {
                            Text("Invalid Input Data.")
                                .foregroundColor(.red)
                                .underline()
                        }
                        
                        if registerVM.userAlreadyRegistred {
                            Text("User already registred.")
                                .foregroundColor(.red)
                                .underline()
                        }
                        
                        Button(action: {
                            if registerVM.showPSWDField {
                                if registerVM.acceptInputDataAndStartRegistration() {
                                    registerVM.registerUserAndRequestToken(username: registerVM.username, password: registerVM.password)
                                } else {
                                    withAnimation(.easeInOut) {
                                        errorIncorrectInputData = true
                                        registerVM.showPSWDField = false
                                        registerVM.username = ""
                                        registerVM.password = ""
                                        
                                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                                            withAnimation(.easeInOut) {
                                                errorIncorrectInputData = false
                                            }
                                        }
                                    }
                                }
                            } else {
                                registerVM.isShowPSWDField()
                            }
                        }) {
                            HStack(spacing: 15){
                                Text(registerVM.showPSWDField ? "Register" : "Enter password")
                                    .fontWeight(.semibold)
                                    .contentTransition(.identity)
                                
                                Image(systemName: "line.diagonal.arrow")
                                    .font(.title3)
                                    .rotationEffect(.init(degrees: 45))
                            }
                            .foregroundColor(.black)
                            .padding(.horizontal,25)
                            .padding(.vertical)
                            .background {
                                RoundedRectangle(cornerRadius: 10, style: .continuous)
                                    .fill(.black.opacity(0.05))
                            }
                        }
                        .padding(.top, errorIncorrectInputData ? 10 : 30)
                    }
                    .padding(.leading,60)
                    .padding(.vertical,15)
                }
                
                
                Button {
                    withAnimation(.easeInOut) {
                        userIsRegistred.toggle() // Переключаем на экран логина
                        isLogined = false // Устанавливаем isLogined в false, так как мы сейчас находимся в процессе регистрации
                    }
                } label: {
                    Text("Already have account?")
                        .fontWeight(.regular)
                        .frame(maxWidth: .infinity)
                        .padding()
                }
                .foregroundColor(.blue)
                .padding(.bottom, geometry.safeAreaInsets.bottom + 10)
            }
        }
        .alert(registerVM.errorMessage, isPresented: $registerVM.showError) {
            Button("OK", role: .cancel) { }
        }
    }
}
