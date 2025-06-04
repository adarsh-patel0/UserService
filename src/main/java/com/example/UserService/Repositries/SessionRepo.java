package com.example.UserService.Repositries;

import com.example.UserService.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepo extends JpaRepository<Session,Long> {
}
