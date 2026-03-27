package zatribune.spring.ex_mongodb_docker.services;

import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;

import java.util.List;

public interface ProductService {

    List<Product> listAll();

    Product getById(String id);

    Product saveOrUpdate(Product product);

    void delete(String id);

    Product saveOrUpdateProductForm(ProductDTO productDTO);
    
    List<Product> findByCategory(String categoryId);
    
    List<Product> findBySeller(String sellerId);
    
    List<Product> searchByDescription(String description);
}
