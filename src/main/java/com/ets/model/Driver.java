package com.ets.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    private String driverId;
    private String name;
    private Boolean isOnline = false;
} 