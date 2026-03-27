package zatribune.spring.ex_mongodb_docker.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import zatribune.spring.ex_mongodb_docker.entities.Product;
import zatribune.spring.ex_mongodb_docker.entities.Role;
import zatribune.spring.ex_mongodb_docker.entities.User;
import zatribune.spring.ex_mongodb_docker.repositories.ProductRepository;
import zatribune.spring.ex_mongodb_docker.repositories.RoleRepository;
import zatribune.spring.ex_mongodb_docker.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (roleRepository.count() > 0) {
            log.info("Database already seeded — skipping bootstrap");
            return;
        }
        initData();
    }

    private void initData() {
        log.info("Seeding bootstrap data...");

        // Create roles
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        Role userRole = roleRepository.save(Role.builder().name("USER").build());

        String encodedPassword = passwordEncoder.encode("pass");

        // Create users
        User user1 = User.builder()
                .username("user1@gmail.com")
                .password(encodedPassword)
                .roles(new HashSet<>(Set.of(adminRole)))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNotExpired(true)
                .enabled(true)
                .build();

        User user2 = User.builder()
                .username("user2@gmail.com")
                .password(encodedPassword)
                .roles(new HashSet<>(Set.of(userRole)))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNotExpired(true)
                .enabled(true)
                .build();

        User user3 = User.builder()
                .username("user3@gmail.com")
                .password(encodedPassword)
                .roles(new HashSet<>(Set.of(userRole)))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNotExpired(true)
                .enabled(true)
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));

        // Create products
        List<String> descriptions = List.of("Ak-74", "M-16", "AR-15", "Browning Auto 5", "Ruger 10/22", "Shotgun EG-22");
        List<Double> prices = List.of(100.0, 255.0, 300.0, 140.0, 500.0, 175.50);

        for (int i = 0; i < descriptions.size(); i++) {
            Product product = Product.builder()
                    .description(descriptions.get(i))
                    .price(BigDecimal.valueOf(prices.get(i)))
                    .build();
            productRepository.save(product);
        }

        log.info("Bootstrap data seeded successfully — {} roles, {} users, {} products",
                roleRepository.count(), userRepository.count(), productRepository.count());
    }
}
