package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.model.RequestStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    
    @NotEmpty(message = "Список ID заявок не может быть пустым")
    private List<Long> requestIds;
    
    @NotNull(message = "Статус не может быть null")
    private RequestStatus status;
    
    public void setStatus(RequestStatus status) {
        if (status != null && status != RequestStatus.CONFIRMED && status != RequestStatus.REJECTED) {
            throw new IllegalArgumentException("Можно установить только статус CONFIRMED или REJECTED");
        }
        this.status = status;
    }
}
