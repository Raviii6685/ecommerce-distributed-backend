package zatribune.spring.ex_mongodb_docker.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zatribune.spring.ex_mongodb_docker.entities.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategoryId(String categoryId);

    List<Product> findBySellerId(String sellerId);

    List<Product> findByDescriptionContainingIgnoreCase(String description);

    Page<Product> findByCategoryId(String categoryId, Pageable pageable);

    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
}
