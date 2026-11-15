package com.gizmohub.repositories;

import com.gizmohub.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/** Search/filter logic lives in ProductService (in Java, so it can be unit tested and analysed). */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
