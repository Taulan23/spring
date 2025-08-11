package ru.practicum.statsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.statsservice.model.EndpointHit;
import ru.practicum.statsservice.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    
    @Query("SELECT new ru.practicum.statsservice.dto.ViewStatsDto(e.app, e.uri, COUNT(e.ip)) " +
           "FROM EndpointHit e " +
           "WHERE e.timestamp BETWEEN :start AND :end " +
           "AND (:uris IS NULL OR SIZE(:uris) = 0 OR e.uri IN :uris) " +
           "GROUP BY e.app, e.uri " +
           "ORDER BY COUNT(e.ip) DESC")
    List<ViewStatsDto> getStats(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("uris") List<String> uris);
    
    @Query("SELECT new ru.practicum.statsservice.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
           "FROM EndpointHit e " +
           "WHERE e.timestamp BETWEEN :start AND :end " +
           "AND (:uris IS NULL OR SIZE(:uris) = 0 OR e.uri IN :uris) " +
           "GROUP BY e.app, e.uri " +
           "ORDER BY COUNT(DISTINCT e.ip) DESC")
    List<ViewStatsDto> getStatsUnique(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("uris") List<String> uris);
}
