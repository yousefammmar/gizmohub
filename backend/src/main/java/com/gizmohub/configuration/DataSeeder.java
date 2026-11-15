package com.gizmohub.configuration;

import com.gizmohub.models.Category;
import com.gizmohub.models.Product;
import com.gizmohub.models.Role;
import com.gizmohub.models.User;
import com.gizmohub.repositories.ProductRepository;
import com.gizmohub.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Deterministic seed data, inserted on every start (the H2 database is in-memory).
 * Product ids are assigned 1..24 in the order below, so test documents can refer to them by id.
 * Full table with the reason for each value: documentation/requirements/seed-data.md
 *
 * Test accounts (local development only, documented in README):
 *   admin@gizmohub.test     / Admin@1234     (ADMIN)
 *   customer1@gizmohub.test / Customer@123   (CUSTOMER)   ... customer2, customer3 same password
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }
        user("admin@gizmohub.test", "Admin@1234", Role.ADMIN);
        user("customer1@gizmohub.test", "Customer@123", Role.CUSTOMER);
        user("customer2@gizmohub.test", "Customer@123", Role.CUSTOMER);
        user("customer3@gizmohub.test", "Customer@123", Role.CUSTOMER);

        //       name                         category              price      stock   why it is interesting
        product("NovaBook Air 13", "Thin and light 13-inch laptop, 16 GB RAM, 512 GB SSD.", Category.LAPTOPS, "899.00", 15);
        product("NovaBook Pro 16", "16-inch performance laptop for creators.", Category.LAPTOPS, "2499.00", 5);
        product("Titan Workstation X", "Desktop-class mobile workstation.", Category.LAPTOPS, "9999.99", 2);          // just below max price
        product("Titan Workstation Ultra", "Top configuration workstation.", Category.LAPTOPS, "10000.00", 1);        // exactly max price, stock 1
        product("Pulse Phone 12", "6.1-inch OLED phone with dual camera.", Category.PHONES, "699.00", 25);
        product("Pulse Phone Mini", "Compact 5.4-inch phone.", Category.PHONES, "449.00", 0);                         // out of stock
        product("Pulse Phone Lite", "Affordable everyday phone.", Category.PHONES, "299.99", 8);
        product("EchoBuds Pro", "Noise-cancelling wireless earbuds.", Category.AUDIO, "149.00", 40);
        product("EchoBuds Lite", "Lightweight wireless earbuds.", Category.AUDIO, "49.99", 30);                       // just below free shipping
        product("BassBox Speaker", "Portable waterproof Bluetooth speaker.", Category.AUDIO, "50.00", 12);            // exactly free shipping
        product("StudioMax Headphones", "Over-ear studio headphones.", Category.AUDIO, "329.00", 0);                  // out of stock
        product("Orbit Watch 3", "Smartwatch with GPS and heart-rate sensor.", Category.WEARABLES, "249.00", 18);
        product("Orbit Band", "Slim fitness band.", Category.WEARABLES, "79.99", 1);                                  // stock 1
        product("FitRing Smart Ring", "Sleep and activity tracking ring.", Category.WEARABLES, "199.00", 0);          // out of stock
        product("GizmoHub Sticker", "Vinyl logo sticker.", Category.ACCESSORIES, "0.01", 500);                        // minimum valid price
        product("USB-C Cable 1m", "Braided USB-C to USB-C cable.", Category.ACCESSORIES, "0.99", 1000);               // maximum stock
        product("USB-C Cable 2m", "Braided USB-C cable, 2 metres.", Category.ACCESSORIES, "1.00", 999);               // just below max stock
        product("FastCharge 65W Adapter", "GaN wall charger with two USB-C ports.", Category.ACCESSORIES, "39.99", 60);
        product("PowerCore 20K Power Bank", "20,000 mAh power bank.", Category.ACCESSORIES, "59.00", 9);              // stock 9 < cart max 10
        product("Screen Protector Pack", "Pack of two tempered-glass protectors.", Category.ACCESSORIES, "9.99", 10); // stock 10 = cart max
        product("Nexus Controller", "Wireless game controller.", Category.GAMING, "69.99", 22);
        product("Nexus Console S", "Compact home game console.", Category.GAMING, "499.00", 11);
        product("Nexus VR Headset", "Standalone virtual reality headset.", Category.GAMING, "599.00", 3);
        product("Arcade Mouse Pad XL", "Extra-large cloth mouse pad.", Category.GAMING, "24.50", 100);
    }

    private void user(String email, String password, Role role) {
        userRepository.save(new User(email, passwordEncoder.encode(password), role));
    }

    private void product(String name, String description, Category category, String price, int stock) {
        productRepository.save(new Product(name, description, category, new BigDecimal(price), stock));
    }
}
