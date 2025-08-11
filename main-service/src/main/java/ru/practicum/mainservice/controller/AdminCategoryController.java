package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.NewCategoryDto;
import ru.practicum.mainservice.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    
    private final CategoryService categoryService;
    
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        CategoryDto categoryDto = categoryService.createCategory(newCategoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryDto);
    }
    
    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long catId, 
                                                     @Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto updatedCategory = categoryService.updateCategory(catId, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
