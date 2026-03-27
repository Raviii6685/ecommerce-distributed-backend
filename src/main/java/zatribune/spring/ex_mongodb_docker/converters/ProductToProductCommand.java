package zatribune.spring.ex_mongodb_docker.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;

@Component
public class ProductToProductCommand implements Converter<Product, ProductDTO> {

    @Override
    public ProductDTO convert(Product product) {
        ProductDTO productCommand = new ProductDTO();
        productCommand.setId(product.getId());
        productCommand.setDescription(product.getDescription());
        productCommand.setPrice(product.getPrice());
        productCommand.setImageUrl(product.getImageUrl());
        productCommand.setStock(product.getStock());
        productCommand.setSellerId(product.getSellerId());
        
        if (product.getCategory() != null) {
            productCommand.setCategoryId(product.getCategory().getId());
            productCommand.setCategoryName(product.getCategory().getName());
        }
        
        return productCommand;
    }
}
