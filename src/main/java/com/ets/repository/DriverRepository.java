package com.ets.repository;

import com.ets.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface DriverRepository extends JpaRepository<Driver, String> {
} 