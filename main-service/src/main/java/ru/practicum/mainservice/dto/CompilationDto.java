package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private String id;
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events;
}
