package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.NewCategoryDto;
import ru.practicum.mainservice.exception.CategoryAlreadyExistsException;
import ru.practicum.mainservice.exception.CategoryNotFoundException;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (newCategoryDto.getName() == null || newCategoryDto.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name cannot be empty");
        }
        
        if (categoryRepository.existsByName(newCategoryDto.getName().trim())) {
            throw new CategoryAlreadyExistsException("Category with this name already exists");
        }
        
        Category category = new Category();
        category.setName(newCategoryDto.getName().trim());
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        if (categoryDto.getName() == null || categoryDto.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name cannot be empty");
        }
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        
        if (categoryRepository.existsByName(categoryDto.getName().trim()) && 
            !category.getName().equals(categoryDto.getName().trim())) {
            throw new CategoryAlreadyExistsException("Category with this name already exists");
        }
        
        category.setName(categoryDto.getName().trim());
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
    
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        return convertToDto(category);
    }
    
    private CategoryDto convertToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
