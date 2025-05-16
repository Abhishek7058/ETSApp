package com.ets.repository;

import com.ets.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    /**
     * Find all users that are currently online.
     * @return List of online users
     */
    List<User> findByIsOnlineTrue();
} 