package com.gizmohub.services;

import com.gizmohub.dto.ProductRequest;
import com.gizmohub.dto.ProductResponse;
import com.gizmohub.models.Category;
import com.gizmohub.models.Product;
import com.gizmohub.repositories.CartItemRepository;
import com.gizmohub.repositories.ProductRepository;
import com.gizmohub.validation.BusinessException;
import com.gizmohub.validation.ProductValidator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Product Management module (Team Member 2): rules PROD-01 .. PROD-08. */
@Service
public class ProductService {

    public static final int MAX_QUERY_LENGTH = 100; // PROD-08
    public static final String OUT_OF_STOCK = "Out of Stock";
    public static final String IN_STOCK = "In Stock";

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public ProductService(ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Search + filter (PROD-07, PROD-08). All parameters are optional raw strings exactly as typed
     * in the URL, so invalid input can be rejected consistently with a 400 / PROD-08 error.
     *
     *   query    : case-insensitive text matched against name or description, max 100 chars
     *   category : one of Category (case-insensitive)
     *   minPrice : decimal >= 0          (inclusive lower bound)
     *   maxPrice : decimal >= 0          (inclusive upper bound), and minPrice <= maxPrice
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> search(String query, String category, String minPrice, String maxPrice) {
        String text = normalizeQuery(query);
        Category wantedCategory = parseFilterCategory(category);
        BigDecimal min = parseFilterPrice(minPrice, "minPrice");
        BigDecimal max = parseFilterPrice(maxPrice, "maxPrice");
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw BusinessException.badRequest("PROD-08", "minPrice cannot be greater than maxPrice.");
        }

        // ponytail: filters in Java over all rows; fine for a few hundred products, move to a JPA query if the catalog grows.
        List<ProductResponse> results = new ArrayList<>();
        for (Product product : productRepository.findAll(Sort.by("id"))) {
            if (matchesFilters(product, text, wantedCategory, min, max)) {
                results.add(toResponse(product));
            }
        }
        return results;
    }

    /** True when the product satisfies every filter that was given (null filter = "any"). PROD-07: bounds are inclusive. */
    public static boolean matchesFilters(Product product, String text, Category category, BigDecimal min, BigDecimal max) {
        boolean textMatches = text == null
                || product.getName().toLowerCase().contains(text)
                || (product.getDescription() != null && product.getDescription().toLowerCase().contains(text));
        boolean categoryMatches = category == null || product.getCategory() == category;
        boolean priceMatches = (min == null || product.getPrice().compareTo(min) >= 0)
                && (max == null || product.getPrice().compareTo(max) <= 0);
        return textMatches && categoryMatches && priceMatches;
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(findProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(null, null, null, null, 0);
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        applyRequest(product, request);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateStock(Long id, Integer stock) {
        ProductValidator.validateStock(stock); // PROD-03
        Product product = findProduct(id);
        product.setStock(stock);
        return toResponse(product);
    }

    /** Deleting a product also removes it from every cart. Past orders keep their own copy of name/price. */
    @Transactional
    public void delete(Long id) {
        Product product = findProduct(id);
        cartItemRepository.deleteByProductId(product.getId());
        productRepository.delete(product);
    }

    /** PROD-06: label shown by the frontend. */
    public static String stockLabel(int stock) {
        return stock == 0 ? OUT_OF_STOCK : IN_STOCK;
    }

    public static ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getCategory().name(),
                p.getPrice(), p.getStock(), p.getStock() > 0, stockLabel(p.getStock()));
    }

    // ---------- helpers ----------

    private void applyRequest(Product product, ProductRequest request) {
        String name = ProductValidator.validateName(request.name());             // PROD-01
        ProductValidator.validatePrice(request.price());                         // PROD-02
        ProductValidator.validateStock(request.stock());                         // PROD-03
        Category category = ProductValidator.parseCategory(request.category());  // PROD-04
        product.setName(name);
        product.setDescription(request.description() == null ? "" : request.description().trim());
        product.setCategory(category);
        product.setPrice(request.price().setScale(2));
        product.setStock(request.stock());
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("PROD-NOT-FOUND", "Product " + id + " was not found."));
    }

    static String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String trimmed = query.trim();
        if (trimmed.length() > MAX_QUERY_LENGTH) {
            throw BusinessException.badRequest("PROD-08", "Search text must be at most " + MAX_QUERY_LENGTH + " characters.");
        }
        return trimmed.toLowerCase();
    }

    static Category parseFilterCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return Category.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException unknown) {
            throw BusinessException.badRequest("PROD-08", "Unknown category filter: " + category.trim());
        }
    }

    static BigDecimal parseFilterPrice(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(value.trim());
        } catch (NumberFormatException notANumber) {
            throw BusinessException.badRequest("PROD-08", parameterName + " must be a number.");
        }
        if (price.signum() < 0) {
            throw BusinessException.badRequest("PROD-08", parameterName + " cannot be negative.");
        }
        return price;
    }
}
