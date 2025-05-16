

MIT 

# ETS Application API Documentation

This document provides details of all API endpoints available in the ETS application.

## WebSocket Endpoints

### Connection Endpoint
- **Main Socket URL**: `ws://[server-host]:[port]/ws`
- **SockJS URL**: `http://[server-host]:[port]/ws`

### Message Destinations (Client to Server)

| Destination | Description | Payload Example |
|-------------|-------------|----------------|
| `/app/user.connect` | Register user connection | `{"userId": "user123", "slotId": "slot456"}` |
| `/app/driver.connect` | Register driver connection | `{"userId": "driver789", "slotId": "slot456"}` |
| `/app/location.update` | Update location | `{"userId": "user123", "slotId": "slot456", "latitude": 12.9716, "longitude": 77.5946}` |
| `/app/trip.request` | Create a trip request | `{"slotId": "slot456", "userId": "user123", "driverId": "driver789", "status": "REQUESTED", "pickupLocation": {"latitude": 12.9716, "longitude": 77.5946}, "dropLocation": {"latitude": 12.9816, "longitude": 77.6046}}` |
| `/app/trip.status` | Update trip status | `{"slotId": "slot456", "userId": "user123", "driverId": "driver789", "status": "ACCEPTED"}` |

### Subscription Topics (Server to Client)

| Topic | Description | Usage |
|-------|-------------|-------|
| `/topic/slot/{slotId}/trips` | Receive broadcasts for a specific slot | Subscribe to `/topic/slot/slot456/trips` |
| `/user/{userId}/queue/trips` | Receive user-specific messages | Subscribe to `/user/user123/queue/trips` |

## REST Endpoints

### Trip Controller

| Method | Endpoint | Description | Parameters/Body |
|--------|----------|-------------|----------------|
| GET | `/api/trips/slot/{slotId}` | Get trips by slot | Path: `slotId` |
| GET | `/api/trips/user/{userId}` | Get user trips | Path: `userId`, Query: `status` (optional) |
| GET | `/api/trips/driver/{driverId}` | Get driver trips | Path: `driverId`, Query: `status` (optional) |
| POST | `/api/trips/verify-otp` | Verify OTP | Body: `{"slotId": "string", "userId": "string", "driverId": "string", "otp": "string"}` |
| POST | `/api/trips/send-otp` | Send OTP | Body: `{"slotId": "string", "userId": "string", "driverId": "string", "otp": "string" (optional)}` |
| GET | `/api/trips/test-websocket/{slotId}/{userId}` | Test WebSocket | Path: `slotId`, `userId`, Query: `message` (optional) |

### Slot Controller

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/slots/{slotId}/users` | Get users in slot | Path: `slotId` |

## Response Formats

### Trip Object

```json
{
  "id": "string",
  "userId": "string",
  "driverId": "string",
  "slotId": "string",
  "status": "string",
  "otp": "string",
  "otpVerified": boolean,
  "createdAt": "timestamp"
}
```

### User Object

```json
{
  "id": "string",
  "name": "string",
  "isOnline": boolean
}
```

### OTP Verification Response

```json
{
  "status": "success|error",
  "message": "string",
  "otp": "string" (only for send-otp endpoint)
}
```

## WebSocket Message Types

### ConnectionMessage

```json
{
  "userId": "string",
  "slotId": "string"
}
```

### LocationMessage

```json
{
  "userId": "string",
  "slotId": "string",
  "latitude": number,
  "longitude": number
}
```

### TripMessage

```json
{
  "slotId": "string",
  "userId": "string",
  "driverId": "string",
  "status": "string",
  "pickupLocation": {
    "latitude": number,
    "longitude": number
  },
  "dropLocation": {
    "latitude": number,
    "longitude": number
  }
}
```

## Status Codes

The application uses standard HTTP status codes:
- `200 OK`: Request succeeded
- `400 Bad Request`: Invalid input or request
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server-side error

## Development Setup

For local development, the server runs at: 