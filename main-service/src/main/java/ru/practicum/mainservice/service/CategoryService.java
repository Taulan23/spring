package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.NewCategoryDto;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        
        Category category = new Category();
        category.setName(newCategoryDto.getName());
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (categoryRepository.existsByName(categoryDto.getName()) && 
            !category.getName().equals(categoryDto.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        
        category.setName(categoryDto.getName());
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
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
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return convertToDto(category);
    }
    
    private CategoryDto convertToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
