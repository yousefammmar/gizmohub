package com.gizmohub.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** A product in the catalog. Money uses BigDecimal (exact decimal arithmetic, never double). */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    /** 2 decimal places, e.g. 49.99 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    /** Optimistic locking: two checkouts cannot both decrement the same old stock value. */
    @Version
    private Long version;

    protected Product() {
        // required by JPA
    }

    public Product(String name, String description, Category category, BigDecimal price, int stock) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }

    /** Used by tests that build products without a database. */
    public void setId(Long id) { this.id = id; }
}
