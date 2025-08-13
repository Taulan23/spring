package ru.practicum.mainservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    boolean existsByEmail(String email);
    
    Page<User> findByIdIn(List<Long> ids, Pageable pageable);
}
