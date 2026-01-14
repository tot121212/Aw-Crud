package com.crud_project.crud.entity;

import java.util.List;

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