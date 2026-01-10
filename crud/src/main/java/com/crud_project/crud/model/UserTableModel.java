package com.crud_project.crud.model;

import java.util.List;

import com.crud_project.crud.entity.User;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserTableModel {
    private final List<User> users;
    private final Integer currentPage;
    private final Integer totalPages;
    private final Long totalElements;
}
