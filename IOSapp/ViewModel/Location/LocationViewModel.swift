import Foundation
import CoreLocation
import MapKit

struct CodableCoordinate: Codable {
    var latitude: Double
    var longitude: Double
    
    init(coordinate: CLLocationCoordinate2D) {
        self.latitude = coordinate.latitude
        self.longitude = coordinate.longitude
    }
    
    func toCLLocationCoordinate2D() -> CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}

final class LocationViewModel: NSObject, CLLocationManagerDelegate, ObservableObject {

    @Published var lastKnownLocation: CLLocationCoordinate2D?
    @Published var locationName: String = "Unknown Location"
    @Published var savedLocations: [CLLocationCoordinate2D] = []
    @Published var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194),
        span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
    )
    
    var manager = CLLocationManager()
    let geocoder = CLGeocoder()
    
    func checkLocationAuthorization() {
        manager.delegate = self
        manager.startUpdatingLocation()
        
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
            
        case .restricted:
            print("Location restricted")
            
        case .denied:
            print("Location denied")
            
        case .authorizedAlways, .authorizedWhenInUse:
            print("Location authorized")
            if let location = manager.location {
                updateLocation(location)
            }
            
        @unknown default:
            print("Location service disabled")
        }
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        checkLocationAuthorization()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            updateLocation(location)
        }
    }
    
    private func updateLocation(_ location: CLLocation) {
        lastKnownLocation = location.coordinate
        region = MKCoordinateRegion(
            center: location.coordinate,
            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
        )
        reverseGeocode(location)
        checkAndStoreLocation(location)
    }

    private func reverseGeocode(_ location: CLLocation) {
        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, error in
            guard let self = self else { return }
            if let placemark = placemarks?.first {
                self.locationName = [placemark.name, placemark.locality, placemark.administrativeArea, placemark.country]
                    .compactMap { $0 }
                    .joined(separator: ", ")
            } else {
                self.locationName = "Unknown Location"
                print("Error in reverse geocoding: \(error?.localizedDescription ?? "Unknown error")")
            }
        }
    }

    private func checkAndStoreLocation(_ newLocation: CLLocation) {
        guard let lastLocation = savedLocations.last else {
            // Если массив пуст, просто сохраняем первую координату
            savedLocations.append(newLocation.coordinate)
            return
        }

        // Вычисляем расстояние между последней сохраненной координатой и текущей
        let distance = newLocation.distance(from:
            CLLocation(latitude: lastLocation.latitude, longitude: lastLocation.longitude))

        // Если расстояние больше 100 метров, сохраняем координаты
        if distance > 100 {
            savedLocations.append(newLocation.coordinate)
            print("New location saved: \(newLocation.coordinate)")
        }
    }
}
