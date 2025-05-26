# Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data

## Goal
Get the ability to monitor a user's daily human routines

## How it works ? 
<img width="898" alt="Screenshot 2025-05-26 at 20 26 15" src="https://github.com/user-attachments/assets/23bfaf61-2a81-4348-8e99-7939ff7056f6" />

The iOS and Android mobile applications collect data from phones and another devices. This collected data is sent to the Server, which stores it in InfluxDB and MySQL databases. The Server also interacts with a Behavior Analysis Service for data analysis. Finally, the Server provides data to the Website for presentation and visualization.


## Project Structure

### Actigraph
This module contains the implementation of a BLE GATT server for Actigraph devices. It provides functionality for:
- Bluetooth Low Energy communication
- Data collection from Actigraph devices
- Device simulation and testing
- Integration with the main server

[Setup Guide](https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data/blob/main/Actigraph/README.md)

### Android
The Android application serves as a mobile client for data collection. Features include:
- Google Fit integration
- Location tracking
- Background data collection
- Map visualization
- Data synchronization with server

[Setup Guide](https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data/blob/main/Android/README.md)

### iOS
The iOS application provides similar functionality to the Android app, including:
- Health data integration
- Location tracking
- Background data collection
- Map visualization
- Data synchronization with server

[Setup Guide](https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data/blob/main/iOS/README.md)

### Server
The server component consists of two main parts:
- Web Server: Spring Boot application handling API requests, data storage, and processing
- Analysis Module: Data analysis and processing component

Features:
- RESTful API endpoints
- MySQL database integration
- InfluxDB for time-series data
- Data analysis capabilities
- Docker containerization

[Setup Guide](https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data/blob/main/Server/README.md)

### WebSite
The web interface provides:
- Data visualization
- User management
- Real-time data monitoring
- Historical data analysis
- Interactive maps

Built with:
- React
- Redux
- Leaflet for maps
- Various data visualization libraries

[Setup Guide](https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data/blob/main/Website/README.md)

## Important 
The server is located on a remote server and has already been deployed
The website has already been deployed and located by domain https://multisensory-data-iota.vercel.app/

## Authors
- Belov Danil
- Mikiyansky Artem
- Abramkin Nikita
- Pronin Nikolay

