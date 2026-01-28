package com.crud_project.crud.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResourceHandler {

    @Value("classpath:static/data/dbUsernames.txt")
    private Resource dbUsernamesResource;
    @Value("classpath:static/data/dbPassword.txt")
    private Resource dbPasswordResource;

    /**
     * Reads a resource file into a String, Closes the resource afterwards
     *
     * @param resource
     * @throws Exception
     */
    private String readResourceFile(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    /**
     * Gets resource and seperates to Unmodifiable List of String
     *
     * @return Unmodifiable List of String
     * @param resource
     */
    private List<String> getESVResourceToListOfStr(Resource resource) {
        try {
            return Collections.unmodifiableList(Arrays.asList(readResourceFile(resource).split("\\r?\\n")));
        } catch (Exception e) {
            log.error("Error reading resource: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getTestUserDbUsernames() {
        return getESVResourceToListOfStr(dbUsernamesResource);
    }

    public List<String> getTestUserDbPasswords() {
        return getESVResourceToListOfStr(dbPasswordResource);
    }
}
