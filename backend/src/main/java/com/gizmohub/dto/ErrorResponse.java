package com.gizmohub.dto;

/** Every error has this JSON shape: {"status":400,"code":"AUTH-03","message":"..."} */
public record ErrorResponse(int status, String code, String message) {
}
