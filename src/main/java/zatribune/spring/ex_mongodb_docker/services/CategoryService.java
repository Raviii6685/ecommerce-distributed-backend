package zatribune.spring.ex_mongodb_docker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zatribune.spring.ex_mongodb_docker.entities.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Page<Category> getAllCategories(Pageable pageable);
    Category getCategoryById(String id);
    Category createCategory(Category category);
}
