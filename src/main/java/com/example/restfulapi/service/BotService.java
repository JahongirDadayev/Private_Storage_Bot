package com.example.restfulapi.service;

import com.example.restfulapi.controller.BotController;
import com.example.restfulapi.entity.*;
import com.example.restfulapi.repository.*;
import com.example.restfulapi.state.BotState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Service
public class BotService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotController botController;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ContactRepository contactRepository;

    private final Map<String, String> authLogin = new HashMap<>();

    private final Map<String, Integer> removeMessage = new HashMap<>();

    public DbUser getUser(Update update) {
        Message message = getMessage(update);
        Optional<DbUser> optionalDbUser = userRepository.findByChatId(message.getChatId().toString());
        if (optionalDbUser.isPresent()) {
            if (message.hasText() && message.getText().equals("/start")) {
                DbUser user = optionalDbUser.get();
                user.setBotState(BotState.START);
                userRepository.save(user);
                return user;
            } else {
                return optionalDbUser.get();
            }
        } else {
            DbUser user = new DbUser();
            user.setChatId(message.getChatId().toString());
            user.setBotState(BotState.START);
            userRepository.save(user);
            return user;
        }
    }

    public void tgStart(Update update, DbUser user, boolean isStart) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("/start")) {
                KeyboardButton button1 = new KeyboardButton();
                button1.setText("\uD83D\uDCDDYangi kabinet hosil qilish");
                KeyboardButton button2 = new KeyboardButton();
                button2.setText("\uD83D\uDD11Shaxsiy kabinetga kirish");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Collections.singletonList(button1), Collections.singletonList(button2)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setReplyMarkup(markup);
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText((isStart) ? "_Assalomu Alaykum_ *" + message.getFrom().getFirstName() + "* \uD83D\uDC4B\uD83D\uDE03 . Siz virtual xotiradan ☁️ foydalanishingiz uchun yangi kabinet hosil qilish \uD83D\uDCDD yoki shaxsiy kabinetga kirish \uD83D\uDD11 tugmalarini bosish orqali autentifikatsiya \uD83D\uDD10 jarayonidan o'tishingiz kerak ." : "Tanlovdi amalga oshiring ✅");
                user.setBotState(BotState.AUTHENTICATION);
                userRepository.save(user);
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgAuthentication(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("\uD83D\uDCDDYangi kabinet hosil qilish")) {
                removeMessage.put(message.getChatId().toString(), message.getMessageId());
                KeyboardButton button1 = new KeyboardButton();
                button1.setText("\uD83D\uDCDETelefon raqam jo'natish");
                button1.setRequestContact(true);
                KeyboardButton button2 = new KeyboardButton();
                button2.setText("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Collections.singletonList(button1), Collections.singletonList(button2)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Yangi login ✏️ hosil qilish uchun telefon raqamingizni \uD83D\uDCDE jonating");
                user.setBotState(BotState.NEW_LOGIN);
                userRepository.save(user);
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDD11Shaxsiy kabinetga kirish")) {
                removeMessage.put(message.getChatId().toString(), message.getMessageId());
                KeyboardButton button = new KeyboardButton();
                button.setText("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Login kiriting(+9989xxxxxxxx) ✏️...");
                user.setBotState(BotState.LOGIN);
                userRepository.save(user);
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgNewLogin(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasContact()) {
            if (!userRepository.existsByLoginAndIdNot("+" + message.getContact().getPhoneNumber(), user.getId())) {
                user.setBotState(BotState.NEW_PASSWORD);
                userRepository.save(user);
                authLogin.put(message.getChatId().toString(), "+" + message.getContact().getPhoneNumber());
                KeyboardButton button = new KeyboardButton();
                button.setText("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Parol hosil qiling ✏️...");
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Bu loginga kabinet ochilgan \uD83D\uDEAB .");
                botController.execute(sendMessage);
            }
        } else if (message.hasText()) {
            if (message.getText().equals("⬅️Ortga qaytish")) {
                message.setText("/start");
                update.setMessage(message);
                tgStart(update, user, false);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgLogin(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("⬅️Ortga qaytish")) {
                message.setText("/start");
                update.setMessage(message);
                tgStart(update, user, false);
            } else {
                Optional<DbUser> optionalDbUser = userRepository.findByLogin(message.getText());
                if (optionalDbUser.isPresent()) {
                    user.setBotState(BotState.PASSWORD);
                    userRepository.save(user);
                    authLogin.put(message.getChatId().toString(), message.getText());
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setParseMode(ParseMode.MARKDOWN);
                    sendMessage.setText("Parol kiriting ✏️...");
                    botController.execute(sendMessage);
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setText("Siz mavjud bo'lmagan login kiritdingiz ❌ . Qayta urunib ko'rishingiz mumkin \uD83D\uDD04 .");
                    botController.execute(sendMessage);
                }
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgNewPassword(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("⬅️Ortga qaytish")) {
                message.setText("\uD83D\uDCDDYangi kabinet hosil qilish");
                update.setMessage(message);
                tgAuthentication(update, user);
                authLogin.remove(message.getChatId().toString());
            } else {
                user.setLogin(authLogin.get(message.getChatId().toString()));
                user.setPassword(message.getText());
                user.setBotState(BotState.ACCEPTED);
                userRepository.save(user);
                authLogin.remove(message.getChatId().toString());
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText("✔️");
                button1.setCallbackData("✔️");
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText("❌");
                button2.setCallbackData("❌");
                InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Arrays.asList(button1, button2)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(inlineMarkup);
                sendMessage.setText("Yangi login va parolni tasdiqlaysizmi \uD83D\uDDDE");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgPassword(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("⬅️Ortga qaytish")) {
                message.setText("\uD83D\uDD11Shaxsiy kabinetga kirish");
                update.setMessage(message);
                tgAuthentication(update, user);
                authLogin.remove(message.getChatId().toString());
            } else {
                String login = authLogin.get(message.getChatId().toString());
                String password = message.getText();
                Optional<DbUser> optionalDbUser = userRepository.findByLoginAndPassword(login, password);
                if (optionalDbUser.isPresent()) {
                    DbUser anotherUser = optionalDbUser.get();
                    user.setAnotherChatId(anotherUser.getChatId());
                    user.setBotState(BotState.ACCEPTED);
                    userRepository.save(user);
                    authLogin.remove(message.getChatId().toString());
                    InlineKeyboardButton button1 = new InlineKeyboardButton();
                    button1.setText("✔️");
                    button1.setCallbackData("✔️");
                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText("❌");
                    button2.setCallbackData("❌");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Arrays.asList(button1, button2)));
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setParseMode(ParseMode.MARKDOWN);
                    sendMessage.setReplyMarkup(inlineMarkup);
                    sendMessage.setText("Login va parol tog'ri kiritildi ✅ . Kabinetga kirishni tasdiqlaysizmi");
                    botController.execute(sendMessage);
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setParseMode(ParseMode.MARKDOWN);
                    sendMessage.setText("Siz xato paroldan foydalandingiz \uD83D\uDEAB . Parolni qayta yuborishingiz mumkin \uD83D\uDD04");
                    botController.execute(sendMessage);
                }
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgAccept(Update update, DbUser user, boolean isAccept) throws TelegramApiException {
        Message message = getMessage(update);
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.equals("✔️")) {
                user.setBotState(BotState.FILES);
                userRepository.save(user);
                if (isAccept) {
                    Integer removeMessageSize = removeMessage.get(message.getChatId().toString());
                    for (int i = removeMessageSize + 1; i <= message.getMessageId(); i++) {
                        DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), i);
                        botController.execute(deleteMessage);
                    }
                }
                KeyboardButton button1 = new KeyboardButton();
                button1.setText("\uD83D\uDCE4Ma'lumot joylash");
                KeyboardButton button2 = new KeyboardButton();
                button2.setText("\uD83D\uDCE5Ma'lumot olish");
                KeyboardButton button3 = new KeyboardButton();
                button3.setText("\uD83D\uDEB6Kabinetdan chiqish\uD83D\uDEB6\u200D♀️");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Arrays.asList(button1, button2), Collections.singletonList(button3)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText((isAccept) ? "Autentifikatsiya jarayonidan muvaffaqiyatli o'tdingiz \uD83E\uDD1D" : "Tanlovdi amalga oshirishingiz mumkin ✅");
                botController.execute(sendMessage);
            } else if (data.equals("❌")) {
                if (user.getAnotherChatId() == null) {
                    user.setLogin(null);
                    user.setPassword(null);
                } else {
                    user.setAnotherChatId(null);
                }
                userRepository.save(user);
                message.setText("/start");
                update.setMessage(message);
                update.setCallbackQuery(null);
                tgStart(update, user, false);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgFails(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("\uD83D\uDCE4Ma'lumot joylash")) {
                user.setBotState(BotState.POST);
                userRepository.save(user);
                KeyboardButton button1 = new KeyboardButton("\uD83D\uDCF7Rasm joylash");
                KeyboardButton button2 = new KeyboardButton("\uD83C\uDFA5Video joylash");
                KeyboardButton button3 = new KeyboardButton("\uD83C\uDFA7Musiqa joylash");
                KeyboardButton button4 = new KeyboardButton("\uD83D\uDCDAHujjat joylash");
                KeyboardButton button5 = new KeyboardButton("\uD83D\uDCF1Dastur joylash");
                KeyboardButton button6 = new KeyboardButton("\uD83D\uDCDEKontakt joylash");
                KeyboardButton button7 = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Arrays.asList(button1, button2), Arrays.asList(button3, button4), Arrays.asList(button5, button6), Collections.singletonList(button7)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Ma'lumot joylashni amalga oshirishingiz mumkin \uD83D\uDDC2");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDCE5Ma'lumot olish")) {
                user.setBotState(BotState.GET);
                userRepository.save(user);
                KeyboardButton button1 = new KeyboardButton("\uD83D\uDCF7Rasm olish");
                KeyboardButton button2 = new KeyboardButton("\uD83C\uDFA5Video olish");
                KeyboardButton button3 = new KeyboardButton("\uD83C\uDFA7Musiqa olish");
                KeyboardButton button4 = new KeyboardButton("\uD83D\uDCDAHujjat olish");
                KeyboardButton button5 = new KeyboardButton("\uD83D\uDCF1Dastur olish");
                KeyboardButton button6 = new KeyboardButton("\uD83D\uDCDEKontakt olish");
                KeyboardButton button7 = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Arrays.asList(button1, button2), Arrays.asList(button3, button4), Arrays.asList(button5, button6), Collections.singletonList(button7)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Ma'lumot olishni amalga oshirishingiz mumkin \uD83D\uDDC2");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDEB6Kabinetdan chiqish\uD83D\uDEB6\u200D♀️")) {
                user.setAnotherChatId(null);
                userRepository.save(user);
                message.setText("/start");
                update.setMessage(message);
                tgStart(update, user, false);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgPost(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("⬅️Ortga qaytish")) {
                CallbackQuery callbackQuery = new CallbackQuery();
                callbackQuery.setMessage(message);
                callbackQuery.setData("✔️");
                update.setCallbackQuery(callbackQuery);
                update.setMessage(null);
                tgAccept(update, user, false);
            } else if (message.getText().equals("\uD83D\uDCF7Rasm joylash")) {
                user.setBotState(BotState.POST_PHOTO);
                userRepository.save(user);
                KeyboardButton button = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Rasm jo'natishingiz mumkin \uD83D\uDCE6");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83C\uDFA5Video joylash")) {
                user.setBotState(BotState.POST_VIDEO);
                userRepository.save(user);
                KeyboardButton button = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Video jo'natishingiz mumkin \uD83D\uDCE6");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83C\uDFA7Musiqa joylash")) {
                user.setBotState(BotState.POST_MUSIC);
                userRepository.save(user);
                KeyboardButton button = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Musiqa jo'natishingiz mumkin \uD83D\uDCE6");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDCDAHujjat joylash")) {
                user.setBotState(BotState.POST_DOCUMENT);
                userRepository.save(user);
                KeyboardButton button = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Hujjat jo'natishingiz mumkin \uD83D\uDCE6");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDCF1Dastur joylash")) {
                user.setBotState(BotState.POST_PROGRAM);
                userRepository.save(user);
                KeyboardButton button = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Dastur jo'natishingiz mumkin \uD83D\uDCE6");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDCDEKontakt joylash")) {
                user.setBotState(BotState.POST_CONTACT);
                userRepository.save(user);
                KeyboardButton button = new KeyboardButton("⬅️Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Kontakt jo'natishingiz mumkin \uD83D\uDCE6");
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgPostPhoto(Update update, DbUser user){

    }

    public void tgGet(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("⬅️Ortga qaytish")) {
                CallbackQuery callbackQuery = new CallbackQuery();
                callbackQuery.setMessage(message);
                callbackQuery.setData("✔️");
                update.setCallbackQuery(callbackQuery);
                update.setMessage(null);
                tgAccept(update, user, false);
            } else if (message.getText().equals("\uD83D\uDCF7Rasm olish")) {
                List<DbPhoto> photoList = photoRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (photoList.size() != 0) {
                    sendMessage.setText("Sizning rasmlaringiz \uD83D\uDCE6");
                    botController.execute(sendMessage);
                    for (DbPhoto photo : photoList) {
                        SendPhoto sendPhoto = new SendPhoto(message.getChatId().toString(), new InputFile(photo.getFieldId()));
                        botController.execute(sendPhoto);
                    }
                } else {
                    sendMessage.setText("Sizda rasmlar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83C\uDFA5Video olish")) {
                List<DbVideo> videoList = videoRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (videoList.size() != 0) {
                    sendMessage.setText("Sizning videolaringiz \uD83D\uDCE6");
                    botController.execute(sendMessage);
                    for (DbVideo video : videoList) {
                        SendVideo sendVideo = new SendVideo(message.getChatId().toString(), new InputFile(video.getFieldId()));
                        botController.execute(sendVideo);
                    }
                } else {
                    sendMessage.setText("Sizda videolar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83C\uDFA7Musiqa olish")) {
                List<DbMusic> musicList = musicRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (musicList.size() != 0) {
                    sendMessage.setText("Sizning musiqalaringiz \uD83D\uDCE6");
                    botController.execute(sendMessage);
                    for (DbMusic music : musicList) {
                        SendAudio sendAudio = new SendAudio(message.getChatId().toString(), new InputFile(music.getFieldId()));
                        botController.execute(sendAudio);
                    }
                } else {
                    sendMessage.setText("Sizda musiqalar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83D\uDCDAHujjat olish")) {
                List<DbDocument> documentList = documentRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (documentList.size() != 0) {
                    sendMessage.setText("Sizning hujjatlaringiz \uD83D\uDCE6");
                    botController.execute(sendMessage);
                    for (DbDocument document : documentList) {
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(document.getFieldId()));
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda hujjatlar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83D\uDCF1Dastur olish")) {
                List<DbProgram> programList = programRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (programList.size() != 0) {
                    sendMessage.setText("Sizning dasturlaringiz \uD83D\uDCE6");
                    botController.execute(sendMessage);
                    for (DbProgram program : programList) {
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(program.getFieldId()));
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda dasturlar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83D\uDCDEKontakt olish")) {
                List<DbContact> contactList = contactRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (contactList.size() != 0) {
                    sendMessage.setText("Sizning kontaktlaringiz \uD83D\uDCE6");
                    botController.execute(sendMessage);
                    for (DbContact contact : contactList) {
                        SendContact sendContact = new SendContact(message.getChatId().toString(), contact.getPhoneNumber(), contact.getFirstname());
                        botController.execute(sendContact);
                    }
                } else {
                    sendMessage.setText("Sizda kontaktlar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    private Message getMessage(Update update) {
        if (update.hasMessage()) {
            return update.getMessage();
        } else {
            return update.getCallbackQuery().getMessage();
        }
    }

    private ReplyKeyboardMarkup createMarkup(List<List<KeyboardButton>> rows) {
        List<KeyboardRow> rowList = new ArrayList<>();
        for (List<KeyboardButton> row : rows) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.addAll(row);
            rowList.add(keyboardRow);
        }
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(rowList);
        markup.setResizeKeyboard(true);
        return markup;
    }

    private InlineKeyboardMarkup createInlineMarkup(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
