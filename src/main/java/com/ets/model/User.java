package com.ets.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private String userId;
    private String username;
    private Boolean isOnline = false;
} 