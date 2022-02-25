package com.tgbot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class AsyncSender {
    private final AbsSender absSender;

    public AsyncSender(AbsSender absSender) {
        this.absSender = absSender;
    }

    public void sendTextMsgAsync(String text, Long chatId, String parseMode) {
        try {
            absSender.executeAsync(SendMessage
                    .builder()
                    .text(text)
                    .chatId(Long.toString(chatId))
                    .parseMode(parseMode)
                    .build());
        } catch (TelegramApiException ignored) {

        }
    }
}