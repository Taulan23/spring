package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    
    @NotNull(message = "Широта не может быть null")
    private Float lat;
    
    @NotNull(message = "Долгота не может быть null")
    private Float lon;
}
