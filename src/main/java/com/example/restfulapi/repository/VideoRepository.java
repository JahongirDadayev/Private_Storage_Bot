package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<DbVideo, Long> {
    List<DbVideo> findAllByDbUser_ChatId(String dbUser_chatId);
}
