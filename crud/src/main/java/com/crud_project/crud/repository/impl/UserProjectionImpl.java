package com.crud_project.crud.repository.impl;

import java.io.Serializable;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserProjection;

import lombok.Builder;
import lombok.Value;

// This is for serialization if needed
@Value
@Builder
public class UserProjectionImpl implements UserProjection, Serializable {

    private final String userName;
    private final int awCrudsPerformed;
    private final boolean dead;

    public static UserProjectionImpl from(UserProjection projection) {
        return UserProjectionImpl.builder()
                .userName(projection.getUserName())
                .awCrudsPerformed(projection.getAwCrudsPerformed())
                .dead(projection.isDead())
                .build();
    }

    /**
     * @param user
     * @return
     */
    public static UserProjectionImpl from(User user) {
        return UserProjectionImpl.builder()
                .userName(user.getUserName())
                .awCrudsPerformed(user.getAwCrudsPerformed())
                .dead(user.isDead())
                .build();
    }
}
