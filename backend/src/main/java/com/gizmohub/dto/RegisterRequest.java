package com.gizmohub.dto;

/** Body of POST /api/auth/register */
public record RegisterRequest(String email, String password, String confirmPassword) {
}
