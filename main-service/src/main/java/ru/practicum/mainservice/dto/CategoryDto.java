package ru.practicum.mainservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    
    @JsonProperty("id")
    private String id;
    
    @NotBlank(message = "Name не может быть пустым")
    @Size(min = 1, max = 50, message = "Name должно быть от 1 до 50 символов")
    @JsonProperty("name")
    private String name;
}
