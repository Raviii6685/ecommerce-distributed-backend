package zatribune.spring.ex_mongodb_docker.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;

@Component
public class ProductCommandToProduct implements Converter<ProductDTO, Product> {

    @Override
    public Product convert(ProductDTO dto) {
        Product product = new Product();
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            product.setId(dto.getId());
        }
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        return product;
    }
}
