# Server Setup Guide

## Requirements
- Java 17 or newer
- Maven 3.6 or newer
- Docker and Docker Compose
- MySQL 8.0
- InfluxDB 2.0
- At least 4GB RAM
- 20GB free disk space


## Project Setup

### 1. Clone the Repository
```bash
git clone https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data.git
cd Server
```

### 2. Configure Environment
1. Create a `.env` file in the web-server directory:
```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/mysql
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQL8Dialect

# InfluxDB Configuration
INFLUXDB_URL=http://localhost:8086
INFLUXDB_TOKEN=your_token
INFLUXDB_ORG=MultiSens
INFLUXDB_BUCKET=MultiSens

# Application Configuration
SERVER_PORT=8080
```

### 3. Build the Project
```bash
# Build web-server
cd web-server
mvn clean install

# Build analysis module
cd ../analysis
mvn clean install
```

### 4. Run with Docker Compose
```bash
cd web-server
docker-compose up -d
```

This will start:
- Spring Boot application (port 8080)
- MySQL database (port 3306)
- InfluxDB (port 8086)

## Architecture

### Web Server Module
- Spring Boot application
- RESTful API endpoints
- MySQL for structured data
- InfluxDB for time-series data
- JPA/Hibernate for data access
- Spring Security for authentication
- AOP for cross-cutting concerns

### Analysis Module
- Data processing and analysis
- Statistical computations
- Machine learning models
- Data visualization support

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI specification: http://localhost:8080/v3/api-docs

## Database Schema
- MySQL: Structured data storage
- InfluxDB: Time-series data storage

## Monitoring and Logging
- Spring Boot Actuator endpoints
- InfluxDB metrics
- Application logs
- Performance monitoring

## Security
- JWT authentication
- Role-based access control
- Secure password storage
- CORS configuration
- Rate limiting

## Troubleshooting

### Common Issues
1. **Port Conflicts**
   - Check if ports 8080, 3306, and 8086 are available
   - Modify ports in docker-compose.yml if needed

2. **Database Connection Issues**
   - Verify MySQL and InfluxDB are running
   - Check environment variables
   - Ensure network connectivity between containers

3. **Build Failures**
   - Check Java version
   - Update Maven dependencies
   - Clear Maven cache if needed

### Logs
- Spring Boot logs: `docker logs java-app`
- MySQL logs: `docker logs mysql`
- InfluxDB logs: `docker logs influxdb`

## Development Guidelines
1. Follow Java coding standards
2. Write unit tests for new features
3. Document API endpoints
4. Use proper error handling
5. Follow security best practices
6. Implement proper logging
7. Use appropriate design patterns

## Testing
```bash
# Run tests
mvn test

# Run tests with coverage
mvn verify
```

## Deployment
1. Build the application:
```bash
mvn clean package
```

2. Run with Docker Compose:
```bash
docker-compose up -d
```

3. Monitor the deployment:
```bash
docker-compose logs -f
```

## License
[Your License Information]
