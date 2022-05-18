package com.tgbot;

import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class AsyncSender {
    private final static String DEFAULT_PARSE_MODE = "HTML";

    private final AbsSender absSender;

    public AsyncSender(AbsSender absSender) {
        this.absSender = absSender;
    }

    public void sendTextMsgAsync(String text, Long chatId) {
        try {
            absSender.executeAsync(SendMessage
                    .builder()
                    .text(text)
                    .chatId(Long.toString(chatId))
                    .parseMode(DEFAULT_PARSE_MODE)
                    .build());
        } catch (TelegramApiException ignored) {

        }
    }

    public void sendTextMsgAsync(String text, List<Long> chatIds) {
        for (long chatId : chatIds) {
            sendTextMsgAsync(text, chatId);
        }
    }

    public void send$pinTextMsg(String text, Long chatId) {
        try {
            Integer messageId = absSender.execute(SendMessage
                    .builder()
                    .text(text)
                    .chatId(Long.toString(chatId))
                    .parseMode(DEFAULT_PARSE_MODE)
                    .build()).getMessageId();

            absSender.executeAsync(PinChatMessage
                    .builder()
                    .chatId(Long.toString(chatId))
                    .messageId(messageId)
                    .build());
        } catch (TelegramApiException ignored) {

        }
    }

    public void send$pinTextMsg(String text, List<Long> chatIds) {
        for (long chatId : chatIds) {
            send$pinTextMsg(text, chatId);
        }
    }
}
