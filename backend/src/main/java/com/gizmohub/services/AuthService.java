package com.gizmohub.services;

import com.gizmohub.dto.LoginRequest;
import com.gizmohub.dto.RegisterRequest;
import com.gizmohub.dto.UserResponse;
import com.gizmohub.models.Role;
import com.gizmohub.models.User;
import com.gizmohub.repositories.UserRepository;
import com.gizmohub.validation.AuthValidator;
import com.gizmohub.validation.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authentication module (Team Member 1): registration and login rules AUTH-01 .. AUTH-08. */
@Service
public class AuthService {

    /** Names of the values stored in the HTTP session after login. */
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_ROLE = "role";

    /** AUTH-08: the same message for "unknown email" and "wrong password". */
    public static final String INVALID_CREDENTIALS = "Invalid email or password.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /** Hash compared against when the email does not exist, so both failure cases take similar time. */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    /** Creates a CUSTOMER account. Nobody can self-register as ADMIN. */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = AuthValidator.normalizeEmail(request.email());                         // AUTH-01
        AuthValidator.validatePassword(request.password());                                    // AUTH-03
        AuthValidator.validatePasswordConfirmation(request.password(), request.confirmPassword()); // AUTH-04
        if (userRepository.existsByEmail(email)) {                                             // AUTH-02
            throw BusinessException.conflict("AUTH-02", "An account with this email already exists.");
        }
        String hash = passwordEncoder.encode(request.password());                              // AUTH-07
        User saved = userRepository.save(new User(email, hash, Role.CUSTOMER));
        return toResponse(saved);
    }

    /** AUTH-05 / AUTH-08: returns the user when the credentials are valid, otherwise 401 with a generic message. */
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isEmpty()) {
            throw BusinessException.badRequest("AUTH-05", "Email and password are required.");
        }
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash); // equalise timing, result ignored
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH-08", INVALID_CREDENTIALS);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH-08", INVALID_CREDENTIALS);
        }
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long userId) {
        return userRepository.findById(userId)
                .map(AuthService::toResponse)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH-05", "Please log in."));
    }

    static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole().name());
    }
}
