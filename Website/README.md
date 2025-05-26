# WebSite Setup Guide

## Requirements
- Node.js 16.x or newer
- npm 8.x or newer
- Modern web browser (Chrome, Firefox, Safari, Edge)
- Git

## Project Structure
- WebSite
  - life-cycles/ 
  - api/ 
  - public/ 
  - src/ 
  - package.json 
  - package-lock.json

## Project Setup

### 1. Clone the Repository
```bash
git clone https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data.git
cd WebSite/life-cycles
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Configure Environment
Create a `.env` file in the life-cycles directory:
```env
REACT_APP_API_URL=http://localhost:8080
REACT_APP_MAP_API_KEY=your_map_api_key
```

### 4. Development
```bash
npm start
```
This will start the development server at http://localhost:3000

### 5. Build for Production
```bash
npm run build
```
This will create a production build in the `build` directory.

## Features
- React 18.3.1
- Redux Toolkit for state management
- React Router for navigation
- Leaflet for maps
- Axios for API calls
- React DatePicker for date selection
- React Icons for icons
- React Slider for range selection

## Dependencies
Main dependencies include:
- @reduxjs/toolkit: State management
- axios: HTTP client
- leaflet & react-leaflet: Map integration
- react-datepicker: Date selection
- react-icons: Icon library
- react-router-dom: Routing
- react-slider: Range slider component

## Development Guidelines

### Code Structure
- Use functional components with hooks
- Follow Redux best practices
- Implement proper error handling
- Write unit tests for components
- Use TypeScript for type safety

### Styling
- Use CSS modules or styled-components
- Follow responsive design principles
- Maintain consistent color scheme
- Ensure accessibility compliance

### Development Tools
- React Developer Tools browser extension
- Redux DevTools browser extension
- ESLint for code linting
- Prettier for code formatting

## Deployment
1. Build the application:
```bash
npm run build
```

2. Deploy the contents of the `build` directory to your web server

3. Configure your web server to:
   - Serve index.html for all routes
   - Enable CORS if needed
   - Set up proper caching headers

## API Integration
The application integrates with the backend API:
- Base URL: http://localhost:8080
- Authentication: JWT tokens
- Endpoints documented in the API directory
