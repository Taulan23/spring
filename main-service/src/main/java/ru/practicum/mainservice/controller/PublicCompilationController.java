package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.service.CompilationService;


import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    
    private final CompilationService compilationService;
    
    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<CompilationDto> compilations = compilationService.getCompilations(pinned, from, size);
        return ResponseEntity.ok(compilations);
    }
    
    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(@PathVariable Long compId) {
        CompilationDto compilation = compilationService.getCompilationById(compId);
        return ResponseEntity.ok(compilation);
    }
}
