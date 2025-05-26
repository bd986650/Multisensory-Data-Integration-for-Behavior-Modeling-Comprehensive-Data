# iOS Application Setup Guide

## Requirements
- macOS Monterey (12.0) or newer
- Xcode 14.0 or newer
- iOS 15.0 or newer for deployment
- CocoaPods (for dependency management)
- Apple Developer Account (for running on physical devices)
- Physical iOS device or simulator

## Project Setup

### 1. Clone the Repository
```bash
git clone https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data.git
cd iOS
```

### 2. Configure Apple Services
1. Open Xcode and sign in with your Apple ID
2. Update the Bundle Identifier in project settings
3. Configure the following capabilities in Xcode:
   - Background Modes (Location updates)
   - HealthKit (if using health data)
   - Push Notifications (if needed)

### 3. Open Project in Xcode
1. Launch Xcode
2. Select "Open a project or file"
3. Navigate to the iOS folder and select `MutlisensoryDataIntegration.xcodeproj`
4. Wait for the project to index and resolve dependencies

### 4. Project Structure
The project follows MVVM architecture:
MutlisensoryDataIntegration/
- Application 
- Helper 
- Model 
- ViewModel
- Views
- Preview Content
- Info.plist 
- MutlisensoryDataIntegration.entitlements

### 5. Configure Dependencies
The project uses the following main dependencies:
- SwiftUI for UI
- CoreLocation for location services
- HealthKit for health data (if implemented)
- URLSession for networking
- CoreData for local storage (if implemented)

### 6. Build and Run
1. Select your target device (simulator or physical device)
2. Click "Run" in Xcode or use the command line:
```bash
xcodebuild -scheme MutlisensoryDataIntegration -destination 'platform=iOS Simulator,name=iPhone 14'
```

## Features
- Location tracking with background updates
- Health data integration (if implemented)
- Map visualization
- Data synchronization with server
- Background data collection

## Development Guidelines
1. Follow Swift style guide
2. Use SwiftUI for new views
3. Implement proper error handling
4. Write unit tests for new features
5. Document public APIs
