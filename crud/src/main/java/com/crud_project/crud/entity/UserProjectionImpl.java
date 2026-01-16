package com.crud_project.crud.entity;

import java.io.Serializable;

import com.crud_project.crud.repository.UserProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// This is for serialization if needed
@Getter
@Builder
@AllArgsConstructor
public class UserProjectionImpl implements UserProjection, Serializable {
    private final int id;
    private final String userName;
    private final int awCrudsPerformed;
    private final boolean deleted;

    public static UserProjectionImpl from(UserProjection projection) {
        return UserProjectionImpl.builder()
            .id(projection.getId())
            .userName(projection.getUserName())
            .awCrudsPerformed(projection.getAwCrudsPerformed())
            .deleted(projection.isDeleted())
            .build();
    }
}
