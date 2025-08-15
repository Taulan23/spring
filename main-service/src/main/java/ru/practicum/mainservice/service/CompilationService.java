package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.util.ArrayList;
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
        
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);
        
        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
        }
        compilation.setEvents(events);
        
        Compilation savedCompilation = compilationRepository.save(compilation);
        compilationRepository.flush(); // Принудительная синхронизация с базой данных
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
        
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        return compilationRepository.findAllByPinned(pinned, pageable)
                .stream()
                .filter(compilation -> compilation != null && compilation.getId() != null) // Фильтрация null подборок
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
        if (compilation == null || compilation.getId() == null) {
            throw new IllegalArgumentException("Compilation or Compilation.id cannot be null");
        }
        
        CompilationDto dto = new CompilationDto();
        dto.setId(String.valueOf(compilation.getId()));
        dto.setTitle(compilation.getTitle() != null ? compilation.getTitle() : "");
        dto.setPinned(compilation.getPinned() != null ? compilation.getPinned() : false);
        
        List<EventShortDto> eventDtos = compilation.getEvents() != null ? 
                compilation.getEvents().stream()
                    .filter(event -> event != null && event.getId() != null) // Фильтрация null событий
                    .map(this::mapToEventShortDto)
                    .collect(Collectors.toList()) : 
                new ArrayList<>();
        dto.setEvents(eventDtos);
        
        return dto;
    }
    
    private EventShortDto mapToEventShortDto(Event event) {
        // Дополнительные проверки на null значения
        if (event == null || event.getId() == null) {
            throw new IllegalArgumentException("Event or Event.id cannot be null");
        }
        if (event.getCategory() == null || event.getCategory().getId() == null) {
            throw new IllegalArgumentException("Event.category or Event.category.id cannot be null");
        }
        if (event.getInitiator() == null || event.getInitiator().getId() == null) {
            throw new IllegalArgumentException("Event.initiator or Event.initiator.id cannot be null");
        }
        
        return new EventShortDto(
                String.valueOf(event.getId()),
                event.getAnnotation(),
                new CategoryDto(String.valueOf(event.getCategory().getId()), event.getCategory().getName()),
                event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L,
                event.getEventDate(),
                new UserShortDto(String.valueOf(event.getInitiator().getId()), event.getInitiator().getName()),
                event.getPaid() != null ? event.getPaid() : false,
                event.getTitle(),
                event.getViews() != null ? event.getViews() : 0L
        );
    }
    

}
