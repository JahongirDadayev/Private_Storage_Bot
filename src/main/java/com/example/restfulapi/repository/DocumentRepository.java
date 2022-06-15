package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DbDocument, Long> {
    List<DbDocument> findAllByDbUser_ChatId(String dbUser_chatId);
}
