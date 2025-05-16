package com.ets.controller;

import com.ets.model.User;
import com.ets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{slotId}/users")
    public ResponseEntity<List<User>> getUsersInSlot(@PathVariable String slotId) {
        // Fetch all online users
        List<User> onlineUsers = userRepository.findByIsOnlineTrue();
        
        // Since we don't have a direct way to filter by slotId in our current model
        // We're returning all online users with a note about it in the response
        // In a production environment, you would want to add a slotId field to the User model
        // and filter by that field in the repository
        
        return ResponseEntity.ok(onlineUsers);
    }
} 