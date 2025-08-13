package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.NewCategoryDto;
import ru.practicum.mainservice.exception.CategoryAlreadyExistsException;
import ru.practicum.mainservice.exception.CategoryNotFoundException;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
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
        
        // Проверяем, нет ли событий с этой категорией
        boolean hasEvents = eventRepository.findAll().stream()
                .anyMatch(event -> event.getCategory().getId().equals(id));
        
        if (hasEvents) {
            throw new IllegalStateException("Нельзя удалить категорию, связанную с событиями");
        }
        
        categoryRepository.deleteById(id);
    }
    
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .filter(category -> category != null)
                .map(this::convertToDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
    
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        return convertToDto(category);
    }
    
    private CategoryDto convertToDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(category.getId(), category.getName());
    }
}
