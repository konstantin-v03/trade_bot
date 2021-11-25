package com.tgbot;

import com.futures.Amount;
import com.futures.Coin;
import com.futures.GlobalVariables;
import com.log.TradeLogger;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.toggle.CustomToggle;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TradeBot extends AbilityBot {
    private static final String SUCCESS_STR = "✅ Success";
    private static final String ERROR_STR = "❌ Something went wrong...";

    private final long creatorId;

    private static final CustomToggle toggle = new CustomToggle()
            .turnOff("ban")
            .turnOff("demote")
            .turnOff("promote")
            .turnOff("stats")
            .turnOff("unban")
            .turnOff("backup")
            .turnOff("claim")
            .turnOff("recover")
            .turnOff("report")
            .toggle("commands", "start");

    public TradeBot(String botToken, String botUsername, long creatorId) {
        super(botToken, botUsername, toggle);
        this.creatorId = creatorId;
        TradeLogger.silentSender = silent;
        TradeLogger.chatId = creatorId;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability enableCoin() {
        return Ability.builder()
                .name("encoin")
                .info("Enables coin, requires 3 args: symbol, amount ({number}{$} or {number}{%}, leverage.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(3)
                .action(ctx -> {
                    Amount amount = null;
                    int leverage = 1;
                    boolean isOk = true;

                    try {
                        amount = new Amount(ctx.secondArg());
                        leverage = Integer.parseInt(ctx.thirdArg());
                    } catch (IllegalArgumentException illegalArgumentException) {
                        isOk = false;
                    }

                    if (isOk) {
                        GlobalVariables.enabledCoins.put(ctx.firstArg(), new Coin(ctx.firstArg(), amount, leverage));
                    }

                    silent.send(isOk ? SUCCESS_STR : ERROR_STR, ctx.chatId());
                })
                .build();
    }

    public Ability getCoin() {
        return Ability.builder()
                .name("getcoin")
                .info("Returns enabled coin by symbol, requires 1 arg: symbol.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> {
                    Coin coin = GlobalVariables.enabledCoins.get(ctx.firstArg());

                    silent.send(coin != null ? coin.toString() : ERROR_STR, ctx.chatId());
                })
                .build();
    }

    public Ability getCoins() {
        return Ability.builder()
                .name("getcoins")
                .info("Returns all enabled coins, requires no args.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> silent.send(GlobalVariables.enabledCoins.toString(), ctx.chatId()))
                .build();
    }

    public Ability disableCoin() {
        return Ability.builder()
                .name("discoin")
                .info("/discoin SYMBOL")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> silent.send(GlobalVariables.enabledCoins.remove(ctx.firstArg()) != null ? SUCCESS_STR : ERROR_STR, ctx.chatId()))
                .build();
    }

    public Ability getCoinLog() {
        return Ability.builder()
                .name("gcoinlog")
                .info("Returns coin log file, requires 1 arg: symbol.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> {
                    try {
                        execute(SendDocument
                                .builder()
                                .document(new InputFile().setMedia(TradeLogger.getOrderLogFile(ctx.firstArg())))
                                .chatId(String.valueOf(ctx.chatId()))
                                .build());
                    } catch (TelegramApiException telegramApiException) {
                        silent.send(ERROR_STR, ctx.chatId());
                    }
                })
                .build();
    }

    public Ability getCoinsLog() {
        return Ability.builder()
                .name("gcoinlogs")
                .info("Returns enabled coin log files, requires no args.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> {
                    for (String symbol : GlobalVariables.enabledCoins.keySet()) {
                        try {
                            execute(SendDocument
                                    .builder()
                                    .document(new InputFile().setMedia(TradeLogger.getOrderLogFile(symbol)))
                                    .chatId(String.valueOf(ctx.chatId()))
                                    .build());
                        } catch (TelegramApiException telegramApiException) {
                            silent.send(symbol + ": " + ERROR_STR, ctx.chatId());
                        }
                    }
                })
                .build();
    }

    public Ability getExceptions() {
        return Ability.builder()
                .name("gexcs")
                .info("Returns exceptions file, requires no args.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> {
                    try {
                        execute(SendDocument
                                .builder()
                                .document(new InputFile().setMedia(TradeLogger.getExceptionFile()))
                                .chatId(String.valueOf(ctx.chatId()))
                                .build());
                    } catch (TelegramApiException telegramApiException) {
                        silent.send(ERROR_STR, ctx.chatId());
                    }
                })
                .build();
    }

    public Ability getTotalProfit() {
        return Ability.builder()
                .name("getprofit")
                .info("Returns total profit based on logs, requires 1 arg: symbol.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> {
                    BigDecimal totalProfit = TradeLogger.getTotalProfit(ctx.firstArg());

                    silent.send(ctx.firstArg() + ": " + (totalProfit != null ? totalProfit + "$" : ERROR_STR), ctx.chatId());
                })
                .build();
    }

    public Ability getTotalProfits() {
        return Ability.builder()
                .name("getprofits")
                .info("Returns total profit of enabled coins based on logs, requires no args.")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> {
                    BigDecimal totalProfit;

                    for (String symbol : GlobalVariables.enabledCoins.keySet()) {
                        totalProfit = TradeLogger.getTotalProfit(symbol);

                        silent.send(symbol + ": " + (totalProfit != null ? totalProfit + "$" : ERROR_STR), ctx.chatId());
                    }
                })
                .build();
    }
}
