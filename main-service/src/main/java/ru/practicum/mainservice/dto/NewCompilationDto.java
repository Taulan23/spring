package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    
    @NotBlank(message = "Название подборки не может быть пустым")
    @Size(max = 50, message = "Название подборки не может превышать 50 символов")
    private String title;
    
    private Boolean pinned = false;
    private List<Long> events;
}
