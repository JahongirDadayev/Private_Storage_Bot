package com.example.restfulapi.controller;

import com.example.restfulapi.entity.DbUser;
import com.example.restfulapi.service.BotService;
import com.example.restfulapi.state.BotState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BotController extends TelegramLongPollingBot {
    @Autowired
    private BotService botService;

    @Value(value = "${spring.telegram.bot.username}")
    private String username;

    @Value(value = "${spring.telegram.bot.token}")
    private String token;

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        System.out.println(message.getDocument().getMimeType());
        try {
            DbUser user = botService.getUser(update);
            if (user.getBotState() == BotState.START) {
                botService.tgStart(update, user, true);
            } else if (user.getBotState() == BotState.AUTHENTICATION) {
                botService.tgAuthentication(update, user);
            } else if (user.getBotState() == BotState.NEW_LOGIN) {
                botService.tgNewLogin(update, user);
            } else if (user.getBotState() == BotState.LOGIN) {
                botService.tgLogin(update, user);
            } else if (user.getBotState() == BotState.NEW_PASSWORD) {
                botService.tgNewPassword(update, user);
            } else if (user.getBotState() == BotState.PASSWORD) {
                botService.tgPassword(update, user);
            } else if (user.getBotState() == BotState.ACCEPTED) {
                botService.tgAccept(update, user, true);
            } else if (user.getBotState() == BotState.FILES) {
                botService.tgFails(update, user);
            } else if (user.getBotState() == BotState.POST) {
                botService.tgPost(update, user);
            } else if (user.getBotState() == BotState.GET) {
                botService.tgGet(update, user);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
