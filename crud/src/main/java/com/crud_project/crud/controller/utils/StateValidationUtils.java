package com.crud_project.crud.controller.utils;

import java.util.regex.Pattern;

/**
 * Methods for string validation using regex patterns
 */
public class StateValidationUtils {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\w]{3,32}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*])[A-Za-z0-9#?!@$%^&*]{8,128}$");
    
    /**
     * Validates username using a regex pattern
     * @param username
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validates password using a regex pattern
     * @param password
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password.trim()).matches();
    }
}
