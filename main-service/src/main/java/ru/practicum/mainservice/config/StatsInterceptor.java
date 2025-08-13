package ru.practicum.mainservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.EndpointHit;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StatsInterceptor implements HandlerInterceptor {

	private final StatsClient statsClient;

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		// Отправляем статистику после завершения запроса для синхронности
		EndpointHit hit = new EndpointHit();
		hit.setApp("ewm-main-service");
		hit.setUri(request.getRequestURI());
		hit.setIp(request.getRemoteAddr());
		hit.setTimestamp(LocalDateTime.now());
		
		System.out.println("StatsInterceptor: Sending hit for URI: " + request.getRequestURI());
		try {
			statsClient.hit(hit);
			System.out.println("StatsInterceptor: Hit sent successfully");
		} catch (Exception e) {
			System.out.println("StatsInterceptor: Error sending hit: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		return true;
	}
}


