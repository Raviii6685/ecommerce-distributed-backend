package zatribune.spring.ex_mongodb_docker.services;

import zatribune.spring.ex_mongodb_docker.entities.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(String id);
    Category createCategory(Category category);
}
