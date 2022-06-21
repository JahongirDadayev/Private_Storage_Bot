package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbMusic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MusicRepository extends JpaRepository<DbMusic, Long> {
    List<DbMusic> findAllByDbUser_ChatId(String dbUser_chatId);
}

