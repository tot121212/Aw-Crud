package com.crud_project.crud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.crud_project.crud.entity.User;

// Repository interface that provide CRUD operations for User entity
public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String name);
    Optional<UserProjection> findUserProjectionByUserName(String name);
    @Query("SELECT u.id as id, u.userName as userName, u.awCrudsPerformed as awCrudsPerformed FROM User u")
    Page<UserProjection> findAllProjectionsByPage(Pageable pageable);
}
