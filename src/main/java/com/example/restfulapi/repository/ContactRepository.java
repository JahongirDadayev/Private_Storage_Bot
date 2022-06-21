package com.example.restfulapi.repository;

import com.example.restfulapi.entity.DbContact;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface ContactRepository extends JpaRepository<DbContact, Long> {
    List<DbContact> findAllByDbUser_ChatId(String dbUser_chatId);

    @Transactional
    void deleteByDbUser_ChatIdAndFirstnameAndPhoneNumber(String dbUser_chatId, String firstname, String phoneNumber);
}