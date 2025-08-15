package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.NewCompilationDto;
import ru.practicum.mainservice.dto.UpdateCompilationRequest;
import ru.practicum.mainservice.service.CompilationService;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {
    
    private final CompilationService compilationService;
    
    @PostMapping
    public ResponseEntity<CompilationDto> createCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        CompilationDto compilation = compilationService.createCompilation(newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(compilation);
    }
    
    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@PathVariable Long compId,
                                                           @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        CompilationDto compilation = compilationService.updateCompilation(compId, updateRequest);
        return ResponseEntity.ok(compilation);
    }
}
