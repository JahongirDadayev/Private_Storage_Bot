package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DbDocument, Long> {
    List<DbDocument> findAllByDbUser_ChatId(String dbUser_chatId);

    List<DbDocument> findAllByDbUser_ChatIdAndType(String dbUser_chatId, String type);

    List<DbDocument> findAllByDbUser_ChatIdAndTypeOrType(String dbUser_chatId, String type, String type2);

    List<DbDocument> findAllByDbUser_ChatIdAndTypeNotAndTypeNotOrTypeNotAndTypeNotOrTypeNotAndTypeNotOrTypeNot(String dbUser_chatId, String type, String type2, String type3, String type4, String type5, String type6, String type7);
}
