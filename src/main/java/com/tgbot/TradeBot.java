package com.tgbot;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import com.futures.Amount;
import com.futures.TP_SL;
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
        TradeLogger.asyncSender = new AsyncSender(this);
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

                        if (takeProfitPercent < 0
                                || stopLossPercent < 0
                                || takeProfitPercent > 99
                                || stopLossPercent > 99
                                || leverage <= 0) {
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

                        tp_sl = requestSender.postTP_SLOrders(ctx.firstArg(), positionSide,
                                takeProfitPercent != 0 ? new BigDecimal(takeProfitPercent) : null,
                                stopLossPercent != 0 ? new BigDecimal(stopLossPercent) : null);
                    }

                    if (isOk) {
                        TradeLogger.logOpenPosition(requestSender.getPosition(ctx.firstArg(), positionSide));
                        TradeLogger.logTP_SLOrders(tp_sl);
                    } else {
                        silent.send(I18nSupport.i18n_literals("position.not.open"), ctx.chatId());
                    }
                })
                .build();
    }

    public Ability closePosition() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("closepos"))
                .info(I18nSupport.i18n_literals("closepos.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(2)
                .action(ctx -> {
                    PositionSide positionSide = null;
                    boolean isOk = true;

                    try {
                        positionSide = PositionSide.valueOf(ctx.secondArg());
                    } catch (IllegalArgumentException illegalArgumentException) {
                        isOk = false;
                    }

                    Order order = null;

                    if (isOk) {
                        order = requestSender.closePositionMarket(ctx.firstArg(), positionSide);
                    }

                    TradeLogger.logClosePosition(order != null ? requestSender.getMyTrade(ctx.firstArg(), order.getOrderId()) : null);
                    TradeLogger.logTgBot(requestSender.cancelOrders(ctx.firstArg()).getMsg());
                })
                .build();
    }
}
