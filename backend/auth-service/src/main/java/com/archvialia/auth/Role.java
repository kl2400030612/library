package com.archvialia.auth;

/**
 * Application roles. USER is retained as a legacy value so existing databases
 * created by older versions continue to work; legacy USER accounts behave as students.
 */
public enum Role {
    STUDENT,
    FACULTY,
    ADMIN,
    USER
}
