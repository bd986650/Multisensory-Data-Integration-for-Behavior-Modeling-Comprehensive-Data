import SwiftUI

struct LoginView: View {
    
    @StateObject var loginVM: LoginViewModel = .init()
    @State var errorIncorrectInputData: Bool = false
    @Binding var userIsRegistred: Bool
    @Binding var isLogined: Bool // Добавляем привязку к isLogined

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
                         Text("\nLogin to continue")
                            .foregroundColor(.gray)
                        )
                        .font(.title)
                        .fontWeight(.semibold)
                        .lineSpacing(10)
                        .padding(.top, 20)
                        .padding(.trailing, 15)
                        
                        CustomTextField(hint: "username", text: $loginVM.username)
                            .disabled(loginVM.showPSWDField)
                            .opacity(loginVM.showPSWDField ? 0.4 : 1)
                            .overlay(alignment: .trailing, content: {
                                Button("Change") {
                                    withAnimation(.easeInOut) {
                                        loginVM.showPSWDField = false
                                    }
                                }
                                .font(.caption)
                                .foregroundColor(.indigo)
                                .opacity(loginVM.showPSWDField ? 1 : 0)
                                .padding(.trailing, 15)
                            })
                            .padding(.top, 50)
                        
                        CustomTextField(hint: "password", text: $loginVM.password)
                            .disabled(!loginVM.showPSWDField)
                            .opacity(!loginVM.showPSWDField ? 0 : 1)
                            .padding(.top, 20)
                        
                        if errorIncorrectInputData {
                            Text("Invalid Input Data.")
                                .foregroundColor(.red)
                                .underline()
                        }
                        
                        Button(action: {
                            if loginVM.showPSWDField {
                                loginVM.loginUser(username: loginVM.username, password: loginVM.password)
                            } else {
                                loginVM.isShowPSWDField()
                            }
                        }) {
                            HStack(spacing: 15) {
                                Text(loginVM.showPSWDField ? "Login" : "Enter password")
                                    .fontWeight(.semibold)
                                    .contentTransition(.identity)
                                
                                Image(systemName: "line.diagonal.arrow")
                                    .font(.title3)
                                    .rotationEffect(.init(degrees: 45))
                            }
                            .foregroundColor(.black)
                            .padding(.horizontal, 25)
                            .padding(.vertical)
                            .background {
                                RoundedRectangle(cornerRadius: 10, style: .continuous)
                                    .fill(.black.opacity(0.05))
                            }
                        }
                        .padding(.top, errorIncorrectInputData ? 10 : 30)
                        
                        Spacer()
                    }
                    .padding(.leading, 60)
                    .padding(.vertical, 15)
                }
                
                Button {
                    withAnimation(.easeInOut) {
                        userIsRegistred.toggle() // Переключаем на экран регистрации
                        isLogined = false // Устанавливаем isLogined в false, так как мы сейчас находимся в процессе логина
                    }
                } label: {
                    Text("Don't have account?")
                        .fontWeight(.regular)
                        .frame(maxWidth: .infinity)
                        .padding()
                }
                .foregroundColor(.blue)
                .padding(.bottom, geometry.safeAreaInsets.bottom + 10)
            }
        }
        .alert(loginVM.errorMessage, isPresented: $loginVM.showError) {
            Button("OK", role: .cancel) { }
        }
    }
}
