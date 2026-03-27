package zatribune.spring.ex_mongodb_docker.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<ProductDTO>> listProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false, name = "q") String searchQuery) {
        
        List<Product> products;
        if (categoryId != null && !categoryId.isEmpty()) {
            products = productService.findByCategory(categoryId);
        } else if (searchQuery != null && !searchQuery.isEmpty()) {
            products = productService.searchByDescription(searchQuery);
        } else {
            products = productService.listAll();
        }
        
        List<ProductDTO> dtos = products.stream()
                .map(productToProductCommand::convert)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(productToProductCommand.convert(product));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(
            @RequestBody @Valid ProductDTO productDTO,
            @org.springframework.security.core.annotation.AuthenticationPrincipal zatribune.spring.ex_mongodb_docker.entities.User user) {
        // Ensure id is null for creation
        productDTO.setId(null);
        // Set seller ID automatically from authenticated user
        productDTO.setSellerId(user.getId());
        
        Product saved = productService.saveOrUpdateProductForm(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productToProductCommand.convert(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable String id,
            @RequestBody @Valid ProductDTO productDTO,
            @org.springframework.security.core.annotation.AuthenticationPrincipal zatribune.spring.ex_mongodb_docker.entities.User user) {
        
        // Ownership check
        Product existing = productService.getById(id);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        if (!isAdmin && !existing.getSellerId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sellers can only edit their own products");
        }
        
        productDTO.setId(id);
        productDTO.setSellerId(existing.getSellerId()); // Preserve original seller
        Product updated = productService.saveOrUpdateProductForm(productDTO);
        return ResponseEntity.ok(productToProductCommand.convert(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal zatribune.spring.ex_mongodb_docker.entities.User user) {
        
        // Ownership check
        Product existing = productService.getById(id);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        if (!isAdmin && !existing.getSellerId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Sellers can only delete their own products");
        }
        
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
