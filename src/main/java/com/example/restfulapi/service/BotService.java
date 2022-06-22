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
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
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
    private ContactRepository contactRepository;

    private final Map<String, String> authLogin = new HashMap<>();

    private final Map<String, List<Integer>> removeMessage = new HashMap<>();

    private final Map<String, List<Integer>> otherRemoveMessage = new HashMap<>();

    public DbUser getUser(Update update) throws TelegramApiException {
        Message message = getMessage(update);
        Optional<DbUser> optionalDbUser = userRepository.findByChatId(message.getChatId().toString());
        if (optionalDbUser.isPresent()) {
            if (message.hasText() && message.getText().equals("/start")) {
                DbUser user = optionalDbUser.get();
                user.setAnotherChatId(null);
                user.setConfirmationChatId(null);
                user.setBotState(BotState.START);
                userRepository.save(user);
                return user;
            } else if (message.hasText() && message.getText().equals("/help")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("https://telegra.ph/Vertual-Memory--Shaxsiy-hotira-06-19");
                botController.execute(sendMessage);
                DbUser user = optionalDbUser.get();
                user.setBotState(BotState.HELP);
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
                button1.setText("📝 Yangi kabinet hosil qilish");
                KeyboardButton button2 = new KeyboardButton();
                button2.setText("\uD83D\uDD11 Shaxsiy kabinetga kirish");
                ReplyKeyboardMarkup markup;
                if (message.getChatId().toString().equals("2084579116")) {
                    KeyboardButton button3 = new KeyboardButton();
                    button3.setText("\uD83D\uDC65 Foydalanuvchilar soni");
                    markup = createMarkup(Arrays.asList(Collections.singletonList(button1), Collections.singletonList(button2), Collections.singletonList(button3)));
                } else {
                    markup = createMarkup(Arrays.asList(Collections.singletonList(button1), Collections.singletonList(button2)));
                }
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setReplyMarkup(markup);
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText((isStart) ? "➖ Salom " + message.getFrom().getFirstName() + ". Virtual xotira  [botdan](http://t.me/virtual_memorybot) foydalanishingiz uchun yangi *shaxsiy kabinet* xosil qiling.\n" + "\n" + "➖ Agar sizda oldin akkount ochilgan bolsa *shaxsiy kabinetga kirish* tugmasini bosib akkountingizga kirishingiz mumkin\n" + " \n" + "➖Привет " + message.getFrom().getFirstName() + ". Создайте новый *личный кабинет* для использования [бота](http://t.me/virtual_memorybot) виртуальной памяти.\n" + "\n" + "➖ Если у вас уже есть учетная запись, вы можете получить доступ к своей учетной записи, нажав на *личный кабинет*" : "Tanlovni amalga oshiring ✅");
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
            user.setConfirmationChatId(null);
            user.setAnotherChatId(null);
            userRepository.save(user);
            if (message.getText().equals("📝 Yangi kabinet hosil qilish")) {
                removeMessage.put(message.getChatId().toString(), new ArrayList<>());
                KeyboardButton button1 = new KeyboardButton();
                button1.setText("\uD83D\uDCDE Telefon raqam jo'natish");
                button1.setRequestContact(true);
                KeyboardButton button2 = new KeyboardButton();
                button2.setText("⬅️ Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Collections.singletonList(button1), Collections.singletonList(button2)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("*Yangi kabinet* hosil qilish uchun telefon raqamingizni jonating \uD83D\uDCF2");
                user.setBotState(BotState.NEW_LOGIN);
                userRepository.save(user);
                Message execute = botController.execute(sendMessage);
                List<Integer> messageIdList = removeMessage.get(message.getChatId().toString());
                messageIdList.add(execute.getMessageId());
            } else if (message.getText().equals("\uD83D\uDD11 Shaxsiy kabinetga kirish")) {
                removeMessage.put(message.getChatId().toString(), new ArrayList<>());
                KeyboardButton button = new KeyboardButton();
                button.setText("⬅️ Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Login kiriting(+9989xxxxxxxx) ✏️...");
                user.setBotState(BotState.LOGIN);
                userRepository.save(user);
                Message execute = botController.execute(sendMessage);
                List<Integer> messageIdList = removeMessage.get(message.getChatId().toString());
                messageIdList.add(execute.getMessageId());
            } else if (message.getText().equals("\uD83D\uDC65 Foydalanuvchilar soni") && message.getChatId().toString().equals("2084579116")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Sizning botingizda " + userRepository.countAllByChatIdNot(message.getChatId().toString()) + " ta foydalanuvchi ro'yxatdan o'tgan \uD83D\uDC4F");
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
        List<Integer> messageIdList = removeMessage.get(message.getChatId().toString());
        messageIdList.add(message.getMessageId());
        if (message.hasContact()) {
            String phoneNumber = (message.getContact().getPhoneNumber().contains("+")) ? message.getContact().getPhoneNumber() : "+" + message.getContact().getPhoneNumber();
            if (!userRepository.existsByLogin(phoneNumber)) {
                user.setBotState(BotState.NEW_PASSWORD);
                userRepository.save(user);
                authLogin.put(message.getChatId().toString(), phoneNumber);
                KeyboardButton button = new KeyboardButton();
                button.setText("⬅️ Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Parol hosil qiling ✏️...");
                Message execute = botController.execute(sendMessage);
                messageIdList.add(execute.getMessageId());
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Bu raqamga kabinet hosil qilingan \uD83D\uDEAB .");
                Message execute = botController.execute(sendMessage);
                messageIdList.add(execute.getMessageId());
            }
        } else if (message.hasText()) {
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                message.setText("/start");
                update.setMessage(message);
                tgStart(update, user, false);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                Message execute = botController.execute(sendMessage);
                messageIdList.add(execute.getMessageId());
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            Message execute = botController.execute(sendMessage);
            messageIdList.add(execute.getMessageId());
        }
    }

    public void tgLogin(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        List<Integer> messageIdList = removeMessage.get(message.getChatId().toString());
        messageIdList.add(message.getMessageId());
        if (message.hasText()) {
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                message.setText("/start");
                update.setMessage(message);
                tgStart(update, user, false);
            } else {
                user.setBotState(BotState.PASSWORD);
                userRepository.save(user);
                authLogin.put(message.getChatId().toString(), message.getText());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Parol kiriting ✏️...");
                Message execute = botController.execute(sendMessage);
                messageIdList.add(execute.getMessageId());
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            Message execute = botController.execute(sendMessage);
            messageIdList.add(execute.getMessageId());
        }
    }

    public void tgNewPassword(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        List<Integer> messageIdList = removeMessage.get(message.getChatId().toString());
        messageIdList.add(message.getMessageId());
        if (message.hasText()) {
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                message.setText("📝 Yangi kabinet hosil qilish");
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
                Message execute = botController.execute(sendMessage);
                messageIdList.add(execute.getMessageId());
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            Message execute = botController.execute(sendMessage);
            messageIdList.add(execute.getMessageId());
        }
    }

    public void tgPassword(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        List<Integer> messageIdList = removeMessage.get(message.getChatId().toString());
        messageIdList.add(message.getMessageId());
        if (message.hasText()) {
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                message.setText("\uD83D\uDD11 Shaxsiy kabinetga kirish");
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
                    Message execute = botController.execute(sendMessage);
                    messageIdList.add(execute.getMessageId());
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setParseMode(ParseMode.MARKDOWN);
                    sendMessage.setText("Siz xato login yoki parol kiritdingiz ❗️ .");
                    Message execute = botController.execute(sendMessage);
                    messageIdList.add(execute.getMessageId());
                }
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            Message execute = botController.execute(sendMessage);
            messageIdList.add(execute.getMessageId());
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
                    List<Integer> messageIdList = removeMessage.get(message.getChatId().toString());
                    for (Integer messageId : messageIdList) {
                        DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), messageId);
                        botController.execute(deleteMessage);
                    }
                }
                KeyboardButton button1 = new KeyboardButton();
                button1.setText("\uD83D\uDCE4 Ma'lumot joylash");
                KeyboardButton button2 = new KeyboardButton();
                button2.setText("\uD83D\uDCE5 Ma'lumot olish");
                KeyboardButton button3 = new KeyboardButton();
                button3.setText("\uD83D\uDEB6 Kabinetdan chiqish \uD83D\uDEB6\u200D♀️");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Arrays.asList(button1, button2), Collections.singletonList(button3)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText((isAccept) ? "Autentifikatsiya jarayonidan muvaffaqiyatli o'tdingiz \uD83E\uDD1D" : "Tanlovdi amalga oshirishingiz mumkin ✅");
                botController.execute(sendMessage);
                if (user.getAnotherChatId() != null && isAccept && !user.getAnotherChatId().equals(message.getChatId().toString())) {
                    Optional<DbUser> optionalDbUser = userRepository.findByChatId(user.getAnotherChatId());
                    if (optionalDbUser.isPresent()) {
                        if (!otherRemoveMessage.containsKey(user.getAnotherChatId())) {
                            otherRemoveMessage.put(user.getAnotherChatId(), new ArrayList<>());
                        }
                        DbUser otherUser = optionalDbUser.get();
                        otherUser.setConfirmationChatId(message.getChatId().toString());
                        otherUser.setBotState(BotState.CONFIRMATION);
                        userRepository.save(otherUser);
                        InlineKeyboardButton inlineButton1 = new InlineKeyboardButton();
                        inlineButton1.setText("✔️");
                        inlineButton1.setCallbackData("✔️");
                        InlineKeyboardButton inlineButton2 = new InlineKeyboardButton();
                        inlineButton2.setText("❌");
                        inlineButton2.setCallbackData("❌");
                        InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Arrays.asList(inlineButton1, inlineButton2)));
                        SendMessage otherSendMessage = new SendMessage();
                        otherSendMessage.setChatId(user.getAnotherChatId());
                        otherSendMessage.setParseMode(ParseMode.MARKDOWN);
                        otherSendMessage.setText("Sizning kabinetingizga [" + message.getChat().getFirstName() + "](tg://user?id=" + message.getChatId() + ") ismli shaxs kirdi \uD83D\uDD74\uD83C\uDFFB");
                        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove(true);
                        otherSendMessage.setReplyMarkup(keyboardRemove);
                        Message execute1 = botController.execute(otherSendMessage);
                        List<Integer> otherMessageIdList1 = otherRemoveMessage.get(user.getAnotherChatId());
                        otherMessageIdList1.add(execute1.getMessageId());
                        otherSendMessage.setText("Tasdiqlash uchun ✔️ , chiqarib yuborish uchun ❌ tugmasini bosing");
                        otherSendMessage.setReplyMarkup(inlineMarkup);
                        Message execute2 = botController.execute(otherSendMessage);
                        List<Integer> otherMessageIdList2 = otherRemoveMessage.get(user.getAnotherChatId());
                        otherMessageIdList2.add(execute2.getMessageId());
                    }
                }
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
        } else if (message.hasText()) {
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                message.setText("/start");
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

    public void tgFiles(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("\uD83D\uDCE4 Ma'lumot joylash")) {
                user.setBotState(BotState.POST);
                userRepository.save(user);
                KeyboardButton button1 = new KeyboardButton("⬅️ Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Collections.singletonList(Collections.singletonList(button1)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Xohlagan fayllaringizni \uD83D\uDDC2 jonatib ma'lumotlaringizni joylang \uD83D\uDDC4");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDCE5 Ma'lumot olish")) {
                user.setBotState(BotState.GET);
                userRepository.save(user);
                KeyboardButton button1 = new KeyboardButton("\uD83D\uDCF7 Rasm olish");
                KeyboardButton button2 = new KeyboardButton("\uD83C\uDFA5 Video olish");
                KeyboardButton button3 = new KeyboardButton("\uD83C\uDFA7 Musiqa olish");
                KeyboardButton button4 = new KeyboardButton("\uD83D\uDCDA Hujjat olish");
                KeyboardButton button5 = new KeyboardButton("\uD83D\uDCF1 Dastur olish");
                KeyboardButton button6 = new KeyboardButton("\uD83D\uDCDE Kontakt olish");
                KeyboardButton button7 = new KeyboardButton("⬅️ Ortga qaytish");
                ReplyKeyboardMarkup markup = createMarkup(Arrays.asList(Arrays.asList(button1, button2), Arrays.asList(button3, button4), Arrays.asList(button5, button6), Collections.singletonList(button7)));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyMarkup(markup);
                sendMessage.setText("Ma'lumotlaringizni olishni amalga oshirishingiz mumkin \uD83D\uDCCE");
                botController.execute(sendMessage);
            } else if (message.getText().equals("\uD83D\uDEB6 Kabinetdan chiqish \uD83D\uDEB6\u200D♀️")) {
                user.setConfirmationChatId(null);
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
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                CallbackQuery callbackQuery = new CallbackQuery();
                callbackQuery.setMessage(message);
                callbackQuery.setData("✔️");
                update.setCallbackQuery(callbackQuery);
                update.setMessage(null);
                tgAccept(update, user, false);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xato amal bajardingiz ❌");
                botController.execute(sendMessage);
            }
        } else if (message.hasPhoto()) {
            DbPhoto photo = new DbPhoto();
            photo.setFieldId(message.getPhoto().get(0).getFileId());
            Optional<DbUser> optionalDbUser = userRepository.findByChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
            if (optionalDbUser.isPresent()) {
                photo.setDbUser(optionalDbUser.get());
                photoRepository.save(photo);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyToMessageId(message.getMessageId());
                sendMessage.setText("Ma'lumot saqlandi ✅");
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xatolik sababli ma'lumotlar saqlanmadi ❗️");
                botController.execute(sendMessage);
            }
        } else if (message.hasVideo() || message.hasVideoNote()) {
            DbVideo video = new DbVideo();
            video.setFieldId((message.hasVideo())?message.getVideo().getFileId():message.getVideoNote().getFileId());
            Optional<DbUser> optionalDbUser = userRepository.findByChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
            if (optionalDbUser.isPresent()) {
                video.setDbUser(optionalDbUser.get());
                videoRepository.save(video);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyToMessageId(message.getMessageId());
                sendMessage.setText("Ma'lumot saqlandi ✅");
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xatolik sababli ma'lumotlar saqlanmadi ❗️");
                botController.execute(sendMessage);
            }
        } else if (message.hasAudio() || message.hasVoice()) {
            DbMusic music = new DbMusic();
            music.setFieldId((message.hasAudio())?message.getAudio().getFileId():message.getVoice().getFileId());
            Optional<DbUser> optionalDbUser = userRepository.findByChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
            if (optionalDbUser.isPresent()) {
                music.setDbUser(optionalDbUser.get());
                musicRepository.save(music);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyToMessageId(message.getMessageId());
                sendMessage.setText("Ma'lumot saqlandi ✅");
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xatolik sababli ma'lumotlar saqlanmadi ❗️");
                botController.execute(sendMessage);
            }
        } else if (message.hasDocument()) {
            DbDocument document = new DbDocument();
            document.setFieldId(message.getDocument().getFileId());
            document.setType((message.getDocument().getFileName().substring(message.getDocument().getFileName().lastIndexOf(".") + 1)).toLowerCase());
            Optional<DbUser> optionalDbUser = userRepository.findByChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
            if (optionalDbUser.isPresent()) {
                document.setDbUser(optionalDbUser.get());
                documentRepository.save(document);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyToMessageId(message.getMessageId());
                sendMessage.setText("Ma'lumot saqlandi ✅");
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xatolik sababli ma'lumotlar saqlanmadi ❗️");
                botController.execute(sendMessage);
            }
        } else if (message.hasContact()) {
            DbContact contact = new DbContact();
            contact.setFirstname(message.getContact().getFirstName());
            contact.setPhoneNumber(message.getContact().getPhoneNumber());
            Optional<DbUser> optionalDbUser = userRepository.findByChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
            if (optionalDbUser.isPresent()) {
                contact.setDbUser(optionalDbUser.get());
                contactRepository.save(contact);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setReplyToMessageId(message.getMessageId());
                sendMessage.setText("Ma'lumot saqlandi ✅");
                botController.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Xatolik sababli ma'lumotlar saqlanmadi ❗️");
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

    public void tgGet(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                CallbackQuery callbackQuery = new CallbackQuery();
                callbackQuery.setMessage(message);
                callbackQuery.setData("✔️");
                update.setCallbackQuery(callbackQuery);
                update.setMessage(null);
                tgAccept(update, user, false);
            } else if (message.getText().equals("\uD83D\uDCF7 Rasm olish")) {
                List<DbPhoto> photoList = photoRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (photoList.size() != 0) {
                    sendMessage.setText("Sizning rasmlaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbPhoto photo : photoList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + photo.getId());
                        SendPhoto sendPhoto = new SendPhoto(message.getChatId().toString(), new InputFile(photo.getFieldId()));
                        sendPhoto.setReplyMarkup(inlineMarkup);
                        botController.execute(sendPhoto);
                    }
                } else {
                    sendMessage.setText("Sizda rasmlar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83C\uDFA5 Video olish")) {
                List<DbVideo> videoList = videoRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (videoList.size() != 0) {
                    sendMessage.setText("Sizning videolaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbVideo video : videoList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + video.getId());
                        SendVideo sendVideo = new SendVideo(message.getChatId().toString(), new InputFile(video.getFieldId()));
                        sendVideo.setReplyMarkup(inlineMarkup);
                        botController.execute(sendVideo);
                    }
                } else {
                    sendMessage.setText("Sizda videolar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83C\uDFA7 Musiqa olish")) {
                List<DbMusic> musicList = musicRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (musicList.size() != 0) {
                    sendMessage.setText("Sizning musiqalaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbMusic music : musicList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + music.getId());
                        SendAudio sendAudio = new SendAudio(message.getChatId().toString(), new InputFile(music.getFieldId()));
                        sendAudio.setReplyMarkup(inlineMarkup);
                        botController.execute(sendAudio);
                    }
                } else {
                    sendMessage.setText("Sizda musiqalar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83D\uDCDA Hujjat olish")) {
                List<DbDocument> documentList = documentRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                boolean check = false;
                for (DbDocument document : documentList) {
                    if (!document.getType().equals("apk")) {
                        check = true;
                        break;
                    }
                }
                if (documentList.size() != 0 && check) {
                    user.setBotState(BotState.GET_DOCUMENT);
                    userRepository.save(user);
                    sendMessage.setText("Tanlovdi amalga oshiring ✅");
                    Map<String, KeyboardButton> keyboardButtonMap = new LinkedHashMap<>();
                    for (DbDocument document : documentList) {
                        switch (document.getType()) {
                            case "pdf":
                                if (!keyboardButtonMap.containsKey("pdf")) {
                                    keyboardButtonMap.put("pdf", new KeyboardButton("Pdf"));
                                }
                                break;
                            case "docx":
                            case "doc":
                                if (!keyboardButtonMap.containsKey("docx")) {
                                    keyboardButtonMap.put("docx", new KeyboardButton("Word"));
                                }
                                break;
                            case "pptx":
                            case "ppt":
                                if (!keyboardButtonMap.containsKey("pptx")) {
                                    keyboardButtonMap.put("pptx", new KeyboardButton("Power Point"));
                                }
                                break;
                            case "xlsx":
                            case "xls":
                                if (!keyboardButtonMap.containsKey("xlsx")) {
                                    keyboardButtonMap.put("xlsx", new KeyboardButton("Excel"));
                                }
                                break;
                            default:
                                if (!keyboardButtonMap.containsKey("others") && !document.getType().equals("apk")) {
                                    keyboardButtonMap.put("others", new KeyboardButton("Boshqa Fayllar"));
                                }
                                break;
                        }
                    }
                    keyboardButtonMap.put("back", new KeyboardButton("⬅️ Ortga qaytish"));
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    for (KeyboardButton keyboardButton : keyboardButtonMap.values()) {
                        KeyboardRow keyboardRow = new KeyboardRow(Collections.singletonList(keyboardButton));
                        keyboardRowList.add(keyboardRow);
                    }
                    ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
                    markup.setKeyboard(keyboardRowList);
                    markup.setResizeKeyboard(true);
                    sendMessage.setReplyMarkup(markup);
                    botController.execute(sendMessage);
                } else {
                    sendMessage.setText("Sizda hujjatlar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83D\uDCF1 Dastur olish")) {
                List<DbDocument> programList = documentRepository.findAllByDbUser_ChatIdAndType((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId(), "apk");
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (programList.size() != 0) {
                    sendMessage.setText("Sizning dasturlaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbDocument program : programList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + program.getId());
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(program.getFieldId()));
                        sendDocument.setReplyMarkup(inlineMarkup);
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda dasturlar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("\uD83D\uDCDE Kontakt olish")) {
                List<DbContact> contactList = contactRepository.findAllByDbUser_ChatId((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                if (contactList.size() != 0) {
                    sendMessage.setText("Sizning kontaktlaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    inlineButton.setCallbackData("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbContact contact : contactList) {
                        SendContact sendContact = new SendContact(message.getChatId().toString(), contact.getPhoneNumber(), contact.getFirstname());
                        sendContact.setReplyMarkup(inlineMarkup);
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
        } else if (update.hasCallbackQuery()) {
            delete(update);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgGetDocument(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (message.hasText()) {
            if (message.getText().equals("⬅️ Ortga qaytish")) {
                message.setText("\uD83D\uDCE5 Ma'lumot olish");
                update.setMessage(message);
                tgFiles(update, user);
            } else if (message.getText().equals("Pdf")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                List<DbDocument> documentList = documentRepository.findAllByDbUser_ChatIdAndType((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId(), "pdf");
                if (documentList.size() != 0) {
                    sendMessage.setText("Sizning pdf fayllaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbDocument document : documentList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + document.getId());
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(document.getFieldId()));
                        sendDocument.setReplyMarkup(inlineMarkup);
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda pdf fayllar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("Word")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                List<DbDocument> documentList = documentRepository.findAllByDbUser_ChatIdAndTypeOrType((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId(), "docx", "doc");
                if (documentList.size() != 0) {
                    sendMessage.setText("Sizning word fayllaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbDocument document : documentList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + document.getId());
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(document.getFieldId()));
                        sendDocument.setReplyMarkup(inlineMarkup);
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda  word fayllar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("Power Point")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                List<DbDocument> documentList = documentRepository.findAllByDbUser_ChatIdAndTypeOrType((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId(), "pptx", "ppt");
                if (documentList.size() != 0) {
                    sendMessage.setText("Sizning power point fayllaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbDocument document : documentList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + document.getId());
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(document.getFieldId()));
                        sendDocument.setReplyMarkup(inlineMarkup);
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda  power point fayllar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("Excel")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                List<DbDocument> documentList = documentRepository.findAllByDbUser_ChatIdAndTypeOrType((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId(), "xlsx", "xls");
                if (documentList.size() != 0) {
                    sendMessage.setText("Sizning excel fayllaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbDocument document : documentList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + document.getId());
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(document.getFieldId()));
                        sendDocument.setReplyMarkup(inlineMarkup);
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda  excel fayllar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            } else if (message.getText().equals("Boshqa Fayllar")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                List<DbDocument> documentList = documentRepository.findAllByDbUser_ChatIdAndTypesNot((user.getAnotherChatId() != null) ? user.getAnotherChatId() : user.getChatId(), Arrays.asList("pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "apk"));
                if (documentList.size() != 0) {
                    sendMessage.setText("Sizning boshqa turdagi fayllaringiz \uD83D\uDDC2");
                    botController.execute(sendMessage);
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText("\uD83D\uDDD1");
                    InlineKeyboardMarkup inlineMarkup = createInlineMarkup(Collections.singletonList(Collections.singletonList(inlineButton)));
                    for (DbDocument document : documentList) {
                        inlineButton.setCallbackData("\uD83D\uDDD1" + document.getId());
                        SendDocument sendDocument = new SendDocument(message.getChatId().toString(), new InputFile(document.getFieldId()));
                        sendDocument.setReplyMarkup(inlineMarkup);
                        botController.execute(sendDocument);
                    }
                } else {
                    sendMessage.setText("Sizda  boshqa turdagi fayllar mavjud emas \uD83D\uDEAB");
                    botController.execute(sendMessage);
                }
            }
        } else if (update.hasCallbackQuery()) {
            delete(update);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            botController.execute(sendMessage);
        }
    }

    public void tgConfirmation(Update update, DbUser user) throws TelegramApiException {
        Message message = getMessage(update);
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.equals("✔️")) {
                List<Integer> otherMessageIdList = otherRemoveMessage.get(message.getChatId().toString());
                for (Integer otherMessageId : otherMessageIdList) {
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), otherMessageId);
                    botController.execute(deleteMessage);
                }
                otherRemoveMessage.remove(message.getChatId().toString());
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Ruxsat berildi ✅");
                botController.execute(sendMessage);
                tgAccept(update, user, false);
            } else if (data.equals("❌")) {
                List<Integer> otherMessageIdList = otherRemoveMessage.get(message.getChatId().toString());
                for (Integer otherMessageId : otherMessageIdList) {
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), otherMessageId);
                    botController.execute(deleteMessage);
                }
                otherRemoveMessage.remove(message.getChatId().toString());
                Optional<DbUser> optionalDbUser = userRepository.findByChatId(user.getConfirmationChatId());
                if (optionalDbUser.isPresent()) {
                    DbUser confirmationUser = optionalDbUser.get();
                    confirmationUser.setAnotherChatId(null);
                    userRepository.save(confirmationUser);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(user.getConfirmationChatId());
                    sendMessage.setParseMode(ParseMode.MARKDOWN);
                    sendMessage.setText("Siz uchun " + message.getChat().getFirstName() + " ismli shaxsning kabinetiga kirishga ruxsat berilmadi \uD83D\uDEAB");
                    botController.execute(sendMessage);
                    Chat confirmationChat = new Chat();
                    confirmationChat.setId(Long.valueOf(confirmationUser.getChatId()));
                    Message confirmationMessage = new Message();
                    confirmationMessage.setChat(confirmationChat);
                    confirmationMessage.setText("/start");
                    Update confirmationUpdate = new Update();
                    confirmationUpdate.setMessage(confirmationMessage);
                    tgStart(confirmationUpdate, confirmationUser, false);
                }
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("Chiqarib yuborildi ✅");
                botController.execute(sendMessage);
                update.getCallbackQuery().setData("✔️");
                tgAccept(update, user, false);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setText("Xato amal bajardingiz ❌");
            Message execute = botController.execute(sendMessage);
            List<Integer> otherMessageIdList = otherRemoveMessage.get(message.getChatId().toString());
            otherMessageIdList.add(execute.getMessageId());
        }
    }

    private void delete(Update update) throws TelegramApiException {
        Message message = getMessage(update);
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.contains("\uD83D\uDDD1")) {
                if (message.hasPhoto()) {
                    photoRepository.deleteById(Long.valueOf(data.replace("\uD83D\uDDD1", "")));
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
                    botController.execute(deleteMessage);
                } else if (message.hasVideo()) {
                    videoRepository.deleteById(Long.valueOf(data.replace("\uD83D\uDDD1", "")));
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
                    botController.execute(deleteMessage);
                } else if (message.hasAudio()) {
                    musicRepository.deleteById(Long.valueOf(data.replace("\uD83D\uDDD1", "")));
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
                    botController.execute(deleteMessage);
                } else if (message.hasDocument()) {
                    documentRepository.deleteById(Long.valueOf(data.replace("\uD83D\uDDD1", "")));
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
                    botController.execute(deleteMessage);
                } else if (message.hasContact()) {
                    contactRepository.deleteByDbUser_ChatIdAndFirstnameAndPhoneNumber(message.getChatId().toString(), message.getContact().getFirstName(), message.getContact().getPhoneNumber());
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
                    botController.execute(deleteMessage);
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
