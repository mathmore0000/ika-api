package ika.controllers;

import ika.controllers.aux_classes.category.CategoryResponse;
import ika.controllers.aux_classes.CustomPageResponse;
import ika.entities.Category;
import ika.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        Category category = categoryService.findById(id);
        CategoryResponse categoryResponse = new CategoryResponse(category);
        return ResponseEntity.ok(categoryResponse);
    }

    @GetMapping("/")
    public ResponseEntity<CustomPageResponse<CategoryResponse>> getAllCategories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") int size,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(defaultValue = "description") String sortBy,  // Campo de ordenação
            @RequestParam(defaultValue = "asc") String sortDirection // Direção de ordenação
    ) {
        page = CustomPageResponse.getValidPage(page);
        size = CustomPageResponse.getValidSize(size);

        System.out.println(page + size + description);
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);
        Page<CategoryResponse> categoryPage = categoryService.getAllCategories(description, pageable);

        CustomPageResponse<CategoryResponse> customPageResponse = new CustomPageResponse<>(
                categoryPage.getContent(),
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getSort(),
                categoryPage.getPageable().getOffset(),
                categoryPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }
}
