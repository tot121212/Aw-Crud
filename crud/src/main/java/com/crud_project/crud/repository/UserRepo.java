package com.crud_project.crud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crud_project.crud.entity.User;

// Repository interface that provide CRUD operations for User entity
public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String name);
}