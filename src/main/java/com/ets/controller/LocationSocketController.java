package com.ets.controller;

import com.ets.model.message.ConnectionMessage;
import com.ets.model.message.LocationMessage;
import com.ets.model.message.TripMessage;
import com.ets.service.LocationTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LocationSocketController {
    
    private final LocationTrackingService locationTrackingService;

    @MessageMapping("/user.connect")
    public void handleUserConnect(@Payload ConnectionMessage connectionMessage) {
        locationTrackingService.handleUserConnection(connectionMessage);
    }
    
    @MessageMapping("/driver.connect")
    public void handleDriverConnect(@Payload ConnectionMessage connectionMessage) {
        locationTrackingService.handleDriverConnection(connectionMessage);
    }
    
    @MessageMapping("/location.update")
    public void handleLocationUpdate(@Payload LocationMessage locationMessage) {
        locationTrackingService.handleLocationUpdate(locationMessage);
    }
    
    @MessageMapping("/trip.request")
    public void handleTripRequest(@Payload TripMessage tripMessage) {
        locationTrackingService.createTripRequest(tripMessage);
    }
    
    @MessageMapping("/trip.status")
    public void handleTripStatus(@Payload TripMessage tripMessage) {
        locationTrackingService.updateTripStatus(tripMessage);
    }
} 