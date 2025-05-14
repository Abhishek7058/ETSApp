# ETS Location Tracking System

A real-time location tracking system with WebSocket communication between users and drivers.

## Features

- Real-time location tracking via WebSockets
- User and Driver interfaces
- Trip requests with pickup and drop locations
- OTP verification for trip completion
- Multiple users can connect to one driver via slot ID
- Google Maps integration for visualizing locations

## Technologies Used

- Spring Boot 2.7.14
- Spring WebSocket
- Spring Data JPA
- H2 Database
- HTML, CSS, JavaScript
- Google Maps API
- jQuery, Bootstrap

## How to Run

1. Clone the repository
2. Navigate to the project directory
3. Run `mvn spring-boot:run`
4. Open a web browser and go to `http://localhost:8080`

## Usage Instructions

1. Open both User and Driver interfaces in separate browser tabs
2. Enter a User ID in the User interface and a Driver ID in the Driver interface
3. Enter the same Slot ID in both interfaces to connect them
4. Click Connect to establish WebSocket connection
5. View real-time location updates on maps
6. Request trips from the User interface
7. Accept/reject trips from the Driver interface
8. When driver arrives, verify with OTP

## Project Structure

- `src/main/java/com/ets/` - Java source code
  - `Application.java` - Main application class
  - `config/` - Configuration classes
  - `controller/` - REST and WebSocket controllers
  - `model/` - Entity and message classes
  - `repository/` - Data repositories
  - `service/` - Business logic services
- `src/main/resources/` - Resources
  - `application.properties` - Application configuration
  - `static/` - Web resources
    - `index.html` - Landing page
    - `user.html` - User interface
    - `driver.html` - Driver interface

## API Endpoints

### WebSocket Endpoints

- `/ws` - WebSocket connection endpoint
- `/app/user.connect` - User connection message
- `/app/driver.connect` - Driver connection message
- `/app/location.update` - Location update message
- `/app/trip.request` - Trip request message
- `/app/trip.status` - Trip status update message

### REST Endpoints

- `GET /api/trips/slot/{slotId}` - Get trips by slot ID
- `GET /api/trips/user/{userId}` - Get trips by user ID
- `GET /api/trips/driver/{driverId}` - Get trips by driver ID
- `POST /api/trips/verify-otp` - Verify OTP

## License

MIT 