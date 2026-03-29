package zatribune.spring.ex_mongodb_docker.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zatribune.spring.ex_mongodb_docker.dto.CategoryDTO;
import zatribune.spring.ex_mongodb_docker.entities.Category;
import zatribune.spring.ex_mongodb_docker.services.CategoryService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Page<CategoryDTO> getAllCategories(@PageableDefault(size = 10) Pageable pageable) {
        return categoryService.getAllCategories(pageable).map(this::mapToDTO);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryDTO createCategory(@RequestBody CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .build();
        return mapToDTO(categoryService.createCategory(category));
    }

    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
