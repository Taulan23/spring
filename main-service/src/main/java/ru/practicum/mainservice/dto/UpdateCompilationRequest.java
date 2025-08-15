package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {
    
    @Size(max = 50, message = "Название подборки не может превышать 50 символов")
    private String title;
    
    private Boolean pinned;
    private List<Long> events;
}
