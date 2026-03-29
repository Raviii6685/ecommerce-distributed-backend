package zatribune.spring.ex_mongodb_docker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;

import java.util.List;

public interface ProductService {

    List<Product> listAll();

    Page<Product> listAll(Pageable pageable);

    Product getById(String id);

    Product saveOrUpdate(Product product);

    void delete(String id);

    Product saveOrUpdateProductForm(ProductDTO productDTO);

    List<Product> findByCategory(String categoryId);

    Page<Product> findByCategory(String categoryId, Pageable pageable);

    List<Product> findBySeller(String sellerId);

    List<Product> searchByDescription(String description);

    Page<Product> searchByDescription(String description, Pageable pageable);
}
