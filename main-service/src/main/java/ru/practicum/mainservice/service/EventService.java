package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.dto.UpdateEventUserRequest;
import ru.practicum.mainservice.exception.CategoryNotFoundException;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StatsClient statsClient;
    
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        // Валидация обязательных полей
        if (newEventDto.getAnnotation() == null || newEventDto.getAnnotation().trim().isEmpty()) {
            throw new RuntimeException("Аннотация не может быть пустой");
        }
        if (newEventDto.getDescription() == null || newEventDto.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Описание не может быть пустым");
        }
        if (newEventDto.getTitle() == null || newEventDto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Заголовок не может быть пустым");
        }
        if (newEventDto.getParticipantLimit() != null && newEventDto.getParticipantLimit() < 0) {
            throw new RuntimeException("Лимит участников не может быть отрицательным");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CategoryNotFoundException("Пользователь не найден"));
        
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
        
        // Проверяем дату события (должна быть в будущем, минимум через 2 часа)
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new RuntimeException("Дата события должна быть минимум через 2 часа от текущего времени");
        }
        
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setInitiator(user);
        event.setLocation(new Location(newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon()));
        event.setPaid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false);
        event.setParticipantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0);
        event.setRequestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true);
        event.setTitle(newEventDto.getTitle());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        
        Event savedEvent = eventRepository.save(event);
        return mapToFullDto(savedEvent);
    }
    
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        return events.getContent().stream()
                .map(this::mapToShortDto)
                .collect(Collectors.toList());
    }
    
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Событие не найдено"));
        return mapToFullDto(event);
    }
    
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Событие не найдено"));
        
        // Проверяем, что событие можно изменить (не опубликовано)
        if (event.getState() == EventState.PUBLISHED) {
            throw new IllegalStateException("Нельзя изменить опубликованное событие");
        }
        
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            // Проверяем дату события (должна быть в будущем, минимум через 2 часа)
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new RuntimeException("Дата события должна быть минимум через 2 часа от текущего времени");
            }
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(new Location(updateRequest.getLocation().getLat(), updateRequest.getLocation().getLon()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        
        Event savedEvent = eventRepository.save(event);
        return mapToFullDto(savedEvent);
    }
    
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CategoryNotFoundException("Событие не найдено"));
        
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            // Проверяем дату события (должна быть в будущем, минимум через 1 час для админа)
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new RuntimeException("Дата события должна быть минимум через 1 час от текущего времени");
            }
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(new Location(updateRequest.getLocation().getLat(), updateRequest.getLocation().getLon()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new IllegalStateException("Событие уже опубликовано");
                    }
                    if (event.getState() == EventState.CANCELED) {
                        throw new IllegalStateException("Нельзя опубликовать отмененное событие");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new IllegalStateException("Нельзя отменить опубликованное событие");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        
        Event savedEvent = eventRepository.save(event);
        return mapToFullDto(savedEvent);
    }
    
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                              LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                              boolean onlyAvailable, String sort, int from, int size) {
        
        Sort sorting = Sort.by("eventDate");
        if ("VIEWS".equals(sort)) {
            sorting = Sort.by("views").descending();
        }
        
        Pageable pageable = PageRequest.of(from / size, size, sorting);
        
        // Применяем фильтры и получаем только опубликованные события
        List<EventState> states = List.of(EventState.PUBLISHED);
        Page<Event> events = eventRepository.findEventsWithFilters(null, states, categories, rangeStart, rangeEnd, pageable);
        
        return events.getContent().stream()
                .filter(event -> text == null || event.getAnnotation().toLowerCase().contains(text.toLowerCase()) || 
                                event.getDescription().toLowerCase().contains(text.toLowerCase()) ||
                                event.getTitle().toLowerCase().contains(text.toLowerCase()))
                .filter(event -> paid == null || event.getPaid().equals(paid))
                .filter(event -> !onlyAvailable || event.getParticipantLimit() == 0 || 
                                event.getConfirmedRequests() < event.getParticipantLimit())
                .map(this::mapToShortDto)
                .collect(Collectors.toList());
    }
    
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new CategoryNotFoundException("Событие не найдено"));
        
        // Увеличиваем счетчик просмотров
        event.setViews(event.getViews() + 1);
        eventRepository.save(event);
        
        return mapToFullDto(event);
    }
    
    public List<EventFullDto> searchEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        
        Page<Event> events = eventRepository.findEventsWithFilters(
                users, states, categories, rangeStart, rangeEnd, pageable);
        
        return events.getContent().stream()
                .map(this::mapToFullDto)
                .collect(Collectors.toList());
    }
    
    private EventFullDto mapToFullDto(Event event) {
        return new EventFullDto(
                String.valueOf(event.getId()),
                event.getAnnotation(),
                new CategoryDto(String.valueOf(event.getCategory().getId()), event.getCategory().getName()),
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                new UserShortDto(String.valueOf(event.getInitiator().getId()), event.getInitiator().getName()),
                new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                event.getViews()
        );
    }
    
    private EventShortDto mapToShortDto(Event event) {
        return new EventShortDto(
                String.valueOf(event.getId()),
                event.getAnnotation(),
                new CategoryDto(String.valueOf(event.getCategory().getId()), event.getCategory().getName()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                new UserShortDto(String.valueOf(event.getInitiator().getId()), event.getInitiator().getName()),
                event.getPaid(),
                event.getTitle(),
                event.getViews()
        );
    }
}
