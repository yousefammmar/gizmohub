package com.gizmohub.dto;

/** Safe view of a user: never contains the password hash. */
public record UserResponse(Long id, String email, String role) {
}
