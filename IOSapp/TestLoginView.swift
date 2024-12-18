//
//  TestLoginView.swift
//  MutlisensoryDataIntegration
//
//  Created by chouqxwhatdouknow on 06.12.2024.
//

import SwiftUI

struct TestLoginView: View {
    @State var testVar: Bool = false
    var body: some View {
        if testVar {
            RegisterView(userIsRegistred: $testVar)
        } else {
            LoginView(userIsRegistred: $testVar)
        }
    }
}

#Preview {
    TestLoginView()
}
