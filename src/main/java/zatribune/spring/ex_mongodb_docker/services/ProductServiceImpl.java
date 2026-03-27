package zatribune.spring.ex_mongodb_docker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zatribune.spring.ex_mongodb_docker.converters.ProductCommandToProduct;
import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;
import zatribune.spring.ex_mongodb_docker.exception.ResourceNotFoundException;
import zatribune.spring.ex_mongodb_docker.repositories.ProductRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCommandToProduct productCommandToProduct;

    @Override
    public List<Product> listAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    public Product saveOrUpdate(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public Product saveOrUpdateProductForm(ProductDTO productDTO) {
        Product product = productCommandToProduct.convert(productDTO);

        // If updating, preserve existing fields that aren't in the DTO
        if (productDTO.getId() != null && !productDTO.getId().isEmpty()) {
            Product existing = productRepository.findById(productDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productDTO.getId()));
            product.setSellerId(existing.getSellerId());
            product.setCategory(existing.getCategory());
        }

        Product savedProduct = productRepository.save(product);
        log.info("Saved Product Id: {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    public List<Product> findByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> findBySeller(String sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Override
    public List<Product> searchByDescription(String description) {
        return productRepository.findByDescriptionContainingIgnoreCase(description);
    }
}
