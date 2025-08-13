package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

	private final EventService eventService;

	@GetMapping
	public ResponseEntity<List<EventShortDto>> getEvents(
			@RequestParam(required = false) String text,
			@RequestParam(required = false) List<Long> categories,
			@RequestParam(required = false) Boolean paid,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
			@RequestParam(defaultValue = "false") boolean onlyAvailable,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "10") int size) {
		
		// Проверяем корректность диапазона дат
		if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
			throw new RuntimeException("Дата начала не может быть позже даты окончания");
		}
		
		List<EventShortDto> events = eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
		return ResponseEntity.ok(events);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EventFullDto> getEvent(@PathVariable("id") Long id) {
		EventFullDto event = eventService.getPublicEvent(id);
		return ResponseEntity.ok(event);
	}
}


