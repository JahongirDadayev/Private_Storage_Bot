package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<DbUser, Long> {
    Boolean existsByChatId(String chatId);

    Optional<DbUser> findByChatId(String chatId);

    Boolean existsByLogin(String login);

    Optional<DbUser> findByLoginAndPassword(String login, String password);

    Long countAllByChatIdNot(String chatId);
}
