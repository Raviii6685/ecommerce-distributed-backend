package zatribune.spring.ex_mongodb_docker.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;

@Component
public class ProductToProductCommand implements Converter<Product, ProductDTO> {

    @Override
    public ProductDTO convert(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
