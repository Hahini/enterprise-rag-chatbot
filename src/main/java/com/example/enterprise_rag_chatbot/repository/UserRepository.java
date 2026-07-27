package com.example.enterprise_rag_chatbot.repository;

import com.example.enterprise_rag_chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}