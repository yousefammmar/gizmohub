package com.gizmohub.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared setup for integration tests: full Spring context + MockMvc + seeded H2 test database.
 * @Transactional rolls back every change after each test, so tests never depend on each other.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class IntegrationTestBase {

    static final String ADMIN = "admin@gizmohub.test";
    static final String ADMIN_PASSWORD = "Admin@1234";
    static final String CUSTOMER1 = "customer1@gizmohub.test";
    static final String CUSTOMER2 = "customer2@gizmohub.test";
    static final String CUSTOMER_PASSWORD = "Customer@123";

    // Seed product ids (see DataSeeder / documentation/requirements/seed-data.md)
    static final long PULSE_PHONE_MINI_OUT_OF_STOCK = 6;
    static final long ECHOBUDS_LITE_49_99 = 9;
    static final long BASSBOX_50_00 = 10;
    static final long ORBIT_BAND_STOCK_1 = 13;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    /** Logs in and returns the session to pass to later requests (.session(session)). */
    MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    int stockOf(long productId) throws Exception {
        String body = mvc.perform(get("/api/products/" + productId)).andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(body);
        return node.get("stock").asInt();
    }
}
