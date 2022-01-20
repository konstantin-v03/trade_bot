package com.tgbot;

import com.binance.client.model.enums.PositionSide;
import com.futures.Amount;
import com.futures.Coin;
import com.futures.GlobalVariables;
import com.futures.TP_SL;
import com.futures.dualside.MarginType;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.utils.I18nSupport;
import org.jetbrains.annotations.NonNls;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.toggle.CustomToggle;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;

public class TradeBot extends AbilityBot {
    private final RequestSender requestSender;

    private static final String SUCCESS_STR = I18nSupport.i18n_literals("success");
    private static final String ERROR_STR = I18nSupport.i18n_literals("error");

    private final long creatorId;

    @NonNls
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

    public TradeBot(String botToken, String botUsername, long creatorId, RequestSender requestSender) {
        super(botToken, botUsername, toggle);
        this.creatorId = creatorId;
        this.requestSender = requestSender;
        TradeLogger.silentSender = silent;
        TradeLogger.chatId = creatorId;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability openPosition() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("openpos"))
                .info(I18nSupport.i18n_literals("openpos.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(6)
                .action(ctx -> {
                    Amount amount = null;
                    PositionSide positionSide = null;
                    int leverage = 0;
                    int takeProfitPercent = 0;
                    int stopLossPercent = 0;
                    boolean isOk = true;

                    try {
                        String takeProfitPercentStr = ctx.arguments()[4];
                        String stopLossPercentStr = ctx.arguments()[5];

                        positionSide = PositionSide.valueOf(ctx.secondArg());
                        amount = new Amount(ctx.thirdArg());
                        leverage = Integer.parseInt(ctx.arguments()[3]);

                        if (takeProfitPercentStr.matches("\\d+%") && stopLossPercentStr.matches("\\d+%")) {
                            takeProfitPercent = Integer.parseInt(takeProfitPercentStr.substring(0, takeProfitPercentStr.length() - 1));
                            stopLossPercent = Integer.parseInt(stopLossPercentStr.substring(0, stopLossPercentStr.length() - 1));
                        } else {
                            throw new IllegalArgumentException();
                        }

                        if (takeProfitPercent < 0 || stopLossPercent < 0 || takeProfitPercent > 99 || stopLossPercent > 99) {
                            throw new IllegalArgumentException();
                        }
                    } catch (IllegalArgumentException illegalArgumentException) {
                        isOk = false;
                    }

                    TP_SL tp_sl = null;

                    if (isOk) {
                        if (positionSide.equals(PositionSide.LONG)) {
                            requestSender.openLongPositionMarket(ctx.firstArg(), MarginType.ISOLATED, amount, leverage);
                        } else {
                            requestSender.openShortPositionMarket(ctx.firstArg(), MarginType.ISOLATED, amount, leverage);
                        }

                        if (takeProfitPercent > 0 && stopLossPercent > 0) {
                            tp_sl = requestSender.postTP_SLOrders(ctx.firstArg(), positionSide, new BigDecimal(takeProfitPercent), new BigDecimal(stopLossPercent));
                        }
                    }

                    if (isOk) {
                        silent.send(I18nSupport.i18n_literals("position.open"), ctx.chatId());
                        silent.send(I18nSupport.i18n_literals(tp_sl != null ? "tp.sl.open" : "tp.sl.not.open"), ctx.chatId());
                    } else {
                        silent.send(I18nSupport.i18n_literals("position.not.open"), ctx.chatId());
                    }
                })
                .build();
    }

    public Ability enableCoin() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("encoin"))
                .info(I18nSupport.i18n_literals("encoin.info"))
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
                .name(I18nSupport.i18n_literals("getcoin"))
                .info(I18nSupport.i18n_literals("getcoin.info"))
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
                .name(I18nSupport.i18n_literals("getcoins"))
                .info(I18nSupport.i18n_literals("getcoins.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> silent.send(GlobalVariables.enabledCoins.toString(), ctx.chatId()))
                .build();
    }

    public Ability disableCoin() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("discoin"))
                .info(I18nSupport.i18n_literals("discoin.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> silent.send(GlobalVariables.enabledCoins.remove(ctx.firstArg()) != null ? SUCCESS_STR : ERROR_STR, ctx.chatId()))
                .build();
    }

    public Ability getCoinLog() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("gcoinlog"))
                .info(I18nSupport.i18n_literals("gcoinlog.info"))
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
                .name(I18nSupport.i18n_literals("gcoinlogs"))
                .info(I18nSupport.i18n_literals("gcoinlogs.info"))
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
                .name(I18nSupport.i18n_literals("gexcs"))
                .info(I18nSupport.i18n_literals("gexcs.info"))
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
                .name(I18nSupport.i18n_literals("getprofit"))
                .info(I18nSupport.i18n_literals("getprofit.info"))
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
                .name(I18nSupport.i18n_literals("getprofits"))
                .info(I18nSupport.i18n_literals("getprofits.info"))
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
