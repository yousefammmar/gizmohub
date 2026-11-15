package com.gizmohub.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Baseline integration tests: Cart -> Checkout -> Inventory -> Order history. */
class CheckoutIntegrationTest extends IntegrationTestBase {

    private static final String VALID_CHECKOUT = "{\"shippingAddress\":\"12 Rainbow Street, Amman\",\"phone\":\"0791234567\"}";

    private void addToCart(MockHttpSession session, long productId, int quantity) throws Exception {
        mvc.perform(post("/api/cart/items").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void successfulPurchaseReducesStockAndAppearsInHistory() throws Exception {
        MockHttpSession session = login(CUSTOMER1, CUSTOMER_PASSWORD);
        int stockBefore = stockOf(ECHOBUDS_LITE_49_99);
        addToCart(session, ECHOBUDS_LITE_49_99, 2);

        mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON).content(VALID_CHECKOUT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(99.98))
                .andExpect(jsonPath("$.shippingFee").value(0.00))
                .andExpect(jsonPath("$.status").value("PLACED"));

        assertThat(stockOf(ECHOBUDS_LITE_49_99)).isEqualTo(stockBefore - 2);
        mvc.perform(get("/api/cart").session(session)).andExpect(jsonPath("$.itemCount").value(0));
        mvc.perform(get("/api/orders").session(session)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void outOfStockProductCannotBeAdded() throws Exception {
        MockHttpSession session = login(CUSTOMER1, CUSTOMER_PASSWORD);
        mvc.perform(post("/api/cart/items").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + PULSE_PHONE_MINI_OUT_OF_STOCK + ",\"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CART-04"));
    }

    @Test
    void failedCheckoutDoesNotChangeInventoryOrCreateOrder() throws Exception {
        MockHttpSession session = login(CUSTOMER1, CUSTOMER_PASSWORD);
        addToCart(session, BASSBOX_50_00, 1);
        int bassBoxBefore = stockOf(BASSBOX_50_00);

        mvc.perform(post("/api/orders").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shippingAddress\":\"12 Rainbow Street, Amman\",\"phone\":\"079-123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER-04"));

        assertThat(stockOf(BASSBOX_50_00)).isEqualTo(bassBoxBefore);
        mvc.perform(get("/api/orders").session(session)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void customerCannotSeeAnotherCustomersOrder() throws Exception {
        MockHttpSession first = login(CUSTOMER1, CUSTOMER_PASSWORD);
        addToCart(first, ORBIT_BAND_STOCK_1, 1);
        String body = mvc.perform(post("/api/orders").session(first).contentType(MediaType.APPLICATION_JSON).content(VALID_CHECKOUT))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long orderId = json.readTree(body).get("id").asLong();

        MockHttpSession second = login(CUSTOMER2, CUSTOMER_PASSWORD);
        mvc.perform(get("/api/orders/" + orderId).session(second)).andExpect(status().isNotFound());
    }
}
