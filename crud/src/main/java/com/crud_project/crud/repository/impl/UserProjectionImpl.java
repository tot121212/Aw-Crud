package com.crud_project.crud.repository.impl;

import java.io.Serializable;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserProjection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProjectionImpl implements UserProjection, Serializable {

    private final String userName;
    private final int awCrudsPerformed;
    private final boolean dead;

    public static UserProjectionImpl from(UserProjection projection) {
        return new UserProjectionImpl(projection.getUserName(), projection.getAwCrudsPerformed(), projection.isDead());
    }

    public static UserProjectionImpl from(User user) {
        return new UserProjectionImpl(user.getUserName(), user.getAwCrudsPerformed(), user.isDead());
    }
}
