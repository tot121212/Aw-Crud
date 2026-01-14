package com.crud_project.crud.repository;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// This is for serialization if needed
@Getter
@Builder
@AllArgsConstructor
public class UserProjectionImpl implements UserProjection, Serializable {
    private final Integer id;
    private final String userName;
    private final Integer awCrudsPerformed;
    private final Boolean isDeleted;

    public static UserProjectionImpl from(UserProjection projection) {
        return UserProjectionImpl.builder()
            .id(projection.getId())
            .userName(projection.getUserName())
            .awCrudsPerformed(projection.getAwCrudsPerformed())
            .isDeleted(projection.getIsDeleted())
            .build();
    }
}
