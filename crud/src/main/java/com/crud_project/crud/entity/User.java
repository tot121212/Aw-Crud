package com.crud_project.crud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Entity class that represent a table in relational db
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false, unique = true)
    private int id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false)
    private int awCrudsPerformed = 0;
    
    @Column(nullable = false)
    private boolean deleted = false;

    @Version
    private long version;
}
