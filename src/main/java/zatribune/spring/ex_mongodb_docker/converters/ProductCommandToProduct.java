package zatribune.spring.ex_mongodb_docker.converters;

import lombok.Synchronized;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;
import zatribune.spring.ex_mongodb_docker.exception.ResourceNotFoundException;
import zatribune.spring.ex_mongodb_docker.repositories.CategoryRepository;

@Component
public class ProductCommandToProduct implements Converter<ProductDTO, Product> {

    private final CategoryRepository categoryRepository;

    public ProductCommandToProduct(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Synchronized
    @Nullable
    @Override
    public Product convert(ProductDTO source) {
        if (source == null) {
            return null;
        }

        final Product product = new Product();
        product.setId(source.getId());
        product.setDescription(source.getDescription());
        product.setPrice(source.getPrice());
        product.setImageUrl(source.getImageUrl());
        product.setStock(source.getStock());
        
        if (source.getCategoryId() != null && !source.getCategoryId().isEmpty()) {
            product.setCategory(categoryRepository.findById(source.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", source.getCategoryId())));
        }

        return product;
    }
}
