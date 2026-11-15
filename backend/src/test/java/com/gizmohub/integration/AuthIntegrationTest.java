package com.gizmohub.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Baseline integration tests: Authentication module through the real HTTP layer. */
class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void registerThenLoginThenMe() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"New.User@Example.com\",\"password\":\"Password1\",\"confirmPassword\":\"Password1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        MockHttpSession session = login("new.user@example.com", "Password1");
        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new.user@example.com"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"CUSTOMER1@gizmohub.test\",\"password\":\"Password1\",\"confirmPassword\":\"Password1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH-02"));
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameGenericError() throws Exception {
        String expected = "Invalid email or password.";
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"customer1@gizmohub.test\",\"password\":\"WrongPass1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(expected));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@gizmohub.test\",\"password\":\"WrongPass1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(expected));
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = login(CUSTOMER1, CUSTOMER_PASSWORD);
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());
        mvc.perform(get("/api/cart").session(session)).andExpect(status().isUnauthorized());
    }
}
