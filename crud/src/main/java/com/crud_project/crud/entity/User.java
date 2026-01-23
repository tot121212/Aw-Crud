package com.crud_project.crud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    @Default
    private int awCrudsPerformed = 0;
    
    @Column(nullable = false)
    @Default
    private boolean dead = false;

    @Version
    private long version;
}
