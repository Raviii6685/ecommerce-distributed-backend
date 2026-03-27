package zatribune.spring.ex_mongodb_docker.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zatribune.spring.ex_mongodb_docker.converters.ProductToProductCommand;
import zatribune.spring.ex_mongodb_docker.dto.ProductDTO;
import zatribune.spring.ex_mongodb_docker.entities.Product;
import zatribune.spring.ex_mongodb_docker.services.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductToProductCommand productToProductCommand;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> listProducts() {
        List<ProductDTO> products = productService.listAll().stream()
                .map(productToProductCommand::convert)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(productToProductCommand.convert(product));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody @Valid ProductDTO productDTO) {
        // Ensure id is null for creation
        productDTO.setId(null);
        Product saved = productService.saveOrUpdateProductForm(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productToProductCommand.convert(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable String id,
            @RequestBody @Valid ProductDTO productDTO) {
        productDTO.setId(id);
        Product updated = productService.saveOrUpdateProductForm(productDTO);
        return ResponseEntity.ok(productToProductCommand.convert(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
