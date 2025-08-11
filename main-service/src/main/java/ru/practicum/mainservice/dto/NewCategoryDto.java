package ru.practicum.mainservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {
    
    @NotBlank(message = "Name не может быть пустым")
    @JsonProperty("name")
    private String name;
}
