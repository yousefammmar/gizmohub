package com.gizmohub.unit;

import com.gizmohub.models.CartItem;
import com.gizmohub.models.Category;
import com.gizmohub.models.Product;

import java.math.BigDecimal;

/** Small builders so unit tests can create objects without a database. */
final class TestData {

    private TestData() {
    }

    static Product product(long id, String price, int stock) {
        Product p = new Product("Product " + id, "Test product", Category.ACCESSORIES, new BigDecimal(price), stock);
        p.setId(id);
        return p;
    }

    static CartItem cartItem(long userId, Product product, int quantity) {
        return new CartItem(userId, product, quantity);
    }
}
