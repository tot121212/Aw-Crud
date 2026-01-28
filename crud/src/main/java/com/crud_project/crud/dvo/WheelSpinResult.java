package com.crud_project.crud.dvo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WheelSpinResult {
    private final String winnerName;
    private final List<String> participants;
}