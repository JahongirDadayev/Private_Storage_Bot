package com.example.restfulapi.entity;

import com.example.restfulapi.state.BotState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DbUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true)
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(name = "another_chat_id")
    private String anotherChatId;

    @Column(name = "confirmation_chat_id")
    private String confirmationChatId;

    @Column(name = "bot_state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private BotState botState;

}
