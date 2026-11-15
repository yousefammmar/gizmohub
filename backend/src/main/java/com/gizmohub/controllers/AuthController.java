package com.gizmohub.controllers;

import com.gizmohub.dto.LoginRequest;
import com.gizmohub.dto.RegisterRequest;
import com.gizmohub.dto.UserResponse;
import com.gizmohub.services.AuthService;
import com.gizmohub.validation.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /api/auth/register   201 created user | 400 AUTH-01/03/04 | 409 AUTH-02
 * POST /api/auth/login      200 user + session cookie | 400 AUTH-05 | 401 AUTH-08
 * POST /api/auth/logout     204
 * GET  /api/auth/me         200 user | 401 when not logged in
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest body, HttpServletRequest request) {
        UserResponse user = authService.login(body);
        // Session fixation protection: throw away any old session and start a fresh one.
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthService.SESSION_USER_ID, user.id());
        session.setAttribute(AuthService.SESSION_ROLE, user.role());
        return user;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(AuthService.SESSION_USER_ID);
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH-05", "Not logged in.");
        }
        return authService.findById((Long) userId);
    }
}
