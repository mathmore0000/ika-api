package ika.services;

import ika.entities.Category;
import ika.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Método para buscar uma categoria por ID
    public Category findById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // Método para criar uma nova categoria
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Método para listar todas as categorias
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
