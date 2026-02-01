package com.crud_project.crud.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crud_project.crud.entity.User;

// Repository interface that provide CRUD operations for User entity
public interface UserRepo extends JpaRepository<User, Integer> {

    final static String USER_PROJECTION_FIELDS = "u.id as id, u.userName as userName, u.awCrudsPerformed as awCrudsPerformed, u.dead as dead";

    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    Optional<User> findByUserName(@Param("userName") String userName);

    @Query("SELECT u.dead FROM User u WHERE u.userName = :userName")
    Optional<Boolean> findDeadByUserName(@Param("userName") String userName);

    @Query("SELECT " + USER_PROJECTION_FIELDS + " FROM User u WHERE u.userName = :userName")
    Optional<UserProjection> findUserProjectionByUserName(@Param("userName") String userName);

    // pagination to projections seems to break when @version is involved so
    // explicit @query fixes it
    @Query("SELECT " + USER_PROJECTION_FIELDS + " FROM User u")
    Page<UserProjection> findAllUserProjectionBy(Pageable pageable);

    @Query("SELECT u FROM User u")
    Page<User> findAllBy(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    Set<User> findAllByIdIn(Collection<Integer> ids);

    @Query("SELECT u FROM User u WHERE u.userName IN :names")
    Set<User> findAllByUserNameIn(Collection<String> names);

    @Query("SELECT u.userName FROM User u")
    Page<String> findAllUserNames(Pageable pageable);

    boolean existsByUserName(String userName);
}
