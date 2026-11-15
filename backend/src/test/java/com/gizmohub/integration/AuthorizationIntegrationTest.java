package com.gizmohub.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Baseline integration tests: access control (AUTH-06, PROD-05, ORDER-01). */
class AuthorizationIntegrationTest extends IntegrationTestBase {

    private static final String NEW_PRODUCT =
            "{\"name\":\"Test Gadget\",\"description\":\"x\",\"category\":\"GAMING\",\"price\":10.00,\"stock\":5}";

    @Test
    void anonymousUserCannotUseCartOrCheckout() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotCallAdminEndpoints() throws Exception {
        MockHttpSession customer = login(CUSTOMER1, CUSTOMER_PASSWORD);
        mvc.perform(post("/api/admin/products").session(customer).contentType(MediaType.APPLICATION_JSON).content(NEW_PRODUCT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH-06"));
        mvc.perform(delete("/api/admin/products/1").session(customer)).andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        MockHttpSession admin = login(ADMIN, ADMIN_PASSWORD);
        mvc.perform(post("/api/admin/products").session(admin).contentType(MediaType.APPLICATION_JSON).content(NEW_PRODUCT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Gadget"))
                .andExpect(jsonPath("$.stockLabel").value("In Stock"));
    }
}
