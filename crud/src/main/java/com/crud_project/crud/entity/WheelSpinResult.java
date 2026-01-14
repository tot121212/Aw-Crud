package com.crud_project.crud.entity;

import java.util.List;

import com.crud_project.crud.repository.UserProjectionImpl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WheelSpinResult {
    private final String winnerName;
    private final List<String> participants;
}