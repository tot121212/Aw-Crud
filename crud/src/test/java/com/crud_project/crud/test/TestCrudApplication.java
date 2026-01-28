package com.crud_project.crud.test;

import org.springframework.boot.SpringApplication;

import com.crud_project.crud.CrudApplication;
import com.crud_project.crud.test.config.DatabaseTestcontainersConfiguration;

public class TestCrudApplication {

    public static void main(String[] args) {
        SpringApplication.from(CrudApplication::main)
                .with(DatabaseTestcontainersConfiguration.class)
                .run(args);
    }

}
