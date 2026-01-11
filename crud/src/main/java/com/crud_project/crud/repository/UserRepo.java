package com.crud_project.crud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.crud_project.crud.entity.User;

// Repository interface that provide CRUD operations for User entity
public interface UserRepo extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    Optional<User> findByUserName(String userName);

    @Query("SELECT u.id as id, u.userName as userName, u.awCrudsPerformed as awCrudsPerformed FROM User u WHERE u.userName = :userName")
    Optional<UserProjection> findUserProjectionByUserName(String userName);
    
    // pagination to projections seems to break when @version is involved so explicit @query fixes it
    @Query("SELECT u.id as id, u.userName as userName, u.awCrudsPerformed as awCrudsPerformed FROM User u")
    Page<UserProjection> findAllBy(Pageable pageable);

    @Query("SELECT u.isDeleted FROM User u WHERE u.userName = :userName")
    Optional<Boolean> findIsDeletedByUserName(String userName);
}
