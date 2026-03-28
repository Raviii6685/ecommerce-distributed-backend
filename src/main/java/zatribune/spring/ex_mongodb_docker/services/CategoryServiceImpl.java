package zatribune.spring.ex_mongodb_docker.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zatribune.spring.ex_mongodb_docker.entities.Category;
import zatribune.spring.ex_mongodb_docker.exception.ResourceNotFoundException;
import zatribune.spring.ex_mongodb_docker.repositories.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable("categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category", key = "#id")
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
}
