package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<DbPhoto, Long> {
    List<DbPhoto> findAllByDbUser_ChatId(String dbUser_chatId);
}
