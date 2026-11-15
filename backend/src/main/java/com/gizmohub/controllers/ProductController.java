package com.gizmohub.controllers;

import com.gizmohub.dto.ProductResponse;
import com.gizmohub.models.Category;
import com.gizmohub.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Public catalog (no login needed).
 * GET /api/products?q=&category=&minPrice=&maxPrice=   200 list | 400 PROD-08
 * GET /api/products/{id}                               200 product | 404
 * GET /api/products/categories                         200 ["LAPTOPS", ...]
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> search(@RequestParam(required = false) String q,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String minPrice,
                                        @RequestParam(required = false) String maxPrice) {
        return productService.search(q, category, minPrice, maxPrice);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return Arrays.stream(Category.values()).map(Enum::name).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.getById(id);
    }
}
