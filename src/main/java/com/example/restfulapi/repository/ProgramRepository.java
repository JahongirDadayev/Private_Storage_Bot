package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<DbProgram, Long> {
    List<DbProgram> findAllByDbUser_ChatId(String dbUser_chatId);
}
