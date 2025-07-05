package com.example.UserService.Repositries;

import com.example.UserService.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepo extends JpaRepository<Session,Long> {

    Optional<Session> findByTokenAndUser_Id(String token, Long id);
}
