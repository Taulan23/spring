package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationService {
    
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating compilation with title: {}", newCompilationDto.getTitle());
        
        // Проверяем title
        if (newCompilationDto.getTitle() == null || newCompilationDto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Название подборки не может быть пустым");
        }
        
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle().trim());
        compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);
        
        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
        }
        compilation.setEvents(events);
        
        Compilation savedCompilation = compilationRepository.save(compilation);
        return mapToCompilationDto(savedCompilation);
    }
    
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new RuntimeException("Compilation not found with id: " + compId);
        }
        compilationRepository.deleteById(compId);
    }
    
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);
        
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new RuntimeException("Compilation not found with id: " + compId));
        
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        
        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }
        
        Compilation updatedCompilation = compilationRepository.save(compilation);
        return mapToCompilationDto(updatedCompilation);
    }
    
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Getting compilations with pinned: {}, from: {}, size: {}", pinned, from, size);
        
        Pageable pageable = PageRequest.of(from / size, size);
        return compilationRepository.findAllByPinned(pinned, pageable)
                .stream()
                .map(this::mapToCompilationDto)
                .collect(Collectors.toList());
    }
    
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation by id: {}", compId);
        
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new RuntimeException("Compilation not found with id: " + compId));
        
        return mapToCompilationDto(compilation);
    }
    
    private CompilationDto mapToCompilationDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(String.valueOf(compilation.getId()));
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());
        
        List<EventShortDto> eventDtos = compilation.getEvents().stream()
                .map(this::mapToEventShortDto)
                .collect(Collectors.toList());
        dto.setEvents(eventDtos);
        
        return dto;
    }
    
    private EventShortDto mapToEventShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(String.valueOf(event.getId()));
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(mapToCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(mapToUserShortDto(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        return dto;
    }
    
    private CategoryDto mapToCategoryDto(ru.practicum.mainservice.model.Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(String.valueOf(category.getId()));
        dto.setName(category.getName());
        return dto;
    }
    
    private UserShortDto mapToUserShortDto(ru.practicum.mainservice.model.User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(String.valueOf(user.getId()));
        dto.setName(user.getName());
        return dto;
    }
}
