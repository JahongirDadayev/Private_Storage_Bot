package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DbDocument, Long> {
    List<DbDocument> findAllByDbUser_ChatId(String dbUser_chatId);

    List<DbDocument> findAllByDbUser_ChatIdAndType(String dbUser_chatId, String type);

    List<DbDocument> findAllByDbUser_ChatIdAndTypeOrType(String dbUser_chatId, String type, String type2);

    @Query(value = "select document from DbDocument as document where document.dbUser.chatId=:dbUser_chatId and document.type not in :types")
    List<DbDocument> findAllByDbUser_ChatIdAndTypesNot(String dbUser_chatId, List<String> types);
}
