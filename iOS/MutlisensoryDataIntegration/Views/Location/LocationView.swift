import SwiftUI
import Foundation
import CoreLocation
import MapKit

struct LocationView: View {
    @StateObject private var locationVM = LocationViewModel()
    
    @State var isShowMap = false
        
    var body: some View {
        Section {
            if let coordinate = locationVM.lastKnownLocation {
                Text("Latitude: \(coordinate.latitude)")
                Text("Longitude: \(coordinate.longitude)")
                Text("Location: \(locationVM.locationName)") // Отображение адреса
            } else {
                Text("Unknown Location")
            }
            
            Button {
                withAnimation(.spring) {
                    isShowMap.toggle()
                }
            } label: {
                Text(isShowMap ? "Close Map" : "Show Map")
            }
            
            if (isShowMap) {
                Map(coordinateRegion: $locationVM.region, showsUserLocation: true)
                    .frame(height: 300)
                    .cornerRadius(10)
            }
            
            Button("Get location") {
                locationVM.checkLocationAuthorization()
            }
        } header: {
            Text("Location")
        }
    }
}

struct LocationView_Previews: PreviewProvider {
    static var previews: some View {
        LocationView()
    }
}

