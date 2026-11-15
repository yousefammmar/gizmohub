package com.gizmohub.controllers;

import com.gizmohub.dto.*;
import com.gizmohub.services.OrderService;
import com.gizmohub.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administrator endpoints. AuthInterceptor blocks non-admins BEFORE these run (AUTH-06, PROD-05):
 * 401 when not logged in, 403 for a CUSTOMER.
 *
 * POST   /api/admin/products               201 | 400 PROD-01..04
 * PUT    /api/admin/products/{id}          200 | 400 | 404
 * PATCH  /api/admin/products/{id}/stock    200 | 400 PROD-03 | 404
 * DELETE /api/admin/products/{id}          204 | 404
 * GET    /api/admin/orders                 200 all orders
 * PATCH  /api/admin/orders/{id}/status     200 | 400/409 ORDER-11 | 404
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;

    public AdminController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/products/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/products/{id}/stock")
    public ProductResponse updateStock(@PathVariable Long id, @RequestBody StockRequest request) {
        return productService.updateStock(id, request.stock());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders() {
        return orderService.allOrders();
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse changeStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        return orderService.changeStatus(id, request.status());
    }
}
