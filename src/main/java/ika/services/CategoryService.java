package ika.services;

import ika.controllers.aux_classes.category.CategoryResponse;
import ika.entities.Category;
import ika.repositories.CategoryRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;


    // Método para buscar uma categoria por ID
    public Category findById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Page<CategoryResponse> getAllCategories(String description, Pageable pageable) {
        System.out.println(description);
        return categoryRepository.findAllWithFilters(description, pageable)
                .map(this::convertToCategoryResponse);
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        return new CategoryResponse(category);
    }
}
