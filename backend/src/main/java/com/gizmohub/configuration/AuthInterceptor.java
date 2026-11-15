package com.gizmohub.configuration;

import com.gizmohub.models.Role;
import com.gizmohub.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Runs BEFORE every protected controller method (paths are listed in AppConfig).
 *
 *   not logged in                      -> 401 Unauthorized   (ORDER-01, CART-08)
 *   logged in, /api/admin/**, not ADMIN -> 403 Forbidden      (AUTH-06, PROD-05)
 *   otherwise                          -> continue to the controller
 *
 * This is real server-side authorization: hiding the admin button in the UI is not enough.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(AuthService.SESSION_USER_ID);
        Object role = session == null ? null : session.getAttribute(AuthService.SESSION_ROLE);

        if (userId == null) {
            writeError(response, 401, "AUTH-05", "Please log in to continue.");
            return false;
        }
        boolean adminPath = request.getRequestURI().startsWith(request.getContextPath() + "/api/admin/");
        if (adminPath && !Role.ADMIN.name().equals(role)) {
            writeError(response, 403, "AUTH-06", "Administrator access required.");
            return false;
        }
        return true;
    }

    private static void writeError(HttpServletResponse response, int status, String code, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
