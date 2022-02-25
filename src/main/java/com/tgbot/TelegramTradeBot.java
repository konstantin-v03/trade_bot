package com.tgbot;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import com.futures.Amount;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.strategies.MFI_BigGuyHandler;
import com.strategies.StrategyProps;
import com.tradebot.TradeBot;
import com.utils.I18nSupport;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.toggle.CustomToggle;

import java.util.stream.Collectors;

public class TelegramTradeBot extends AbilityBot {
    private final RequestSender requestSender;
    private final TradeBot tradeBot;

    private final long creatorId;

    public TelegramTradeBot(String botToken, String botUsername, long creatorId, RequestSender requestSender, TradeBot tradeBot) {
        super(botToken, botUsername, new CustomToggle()
                .turnOff("ban")
                .turnOff("demote")
                .turnOff("promote")
                .turnOff("stats")
                .turnOff("unban")
                .turnOff("backup")
                .turnOff("claim")
                .turnOff("recover")
                .turnOff("report")
                .toggle("commands", "start"));
        this.creatorId = creatorId;
        this.requestSender = requestSender;
        this.tradeBot = tradeBot;
        TradeLogger.asyncSender = new AsyncSender(this);
        TradeLogger.chatId = creatorId;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability openPosition() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("open.position"))
                .info(I18nSupport.i18n_literals("open.position.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(6)
                .action(ctx -> {
                    Amount amount;
                    PositionSide positionSide;

                    int leverage;
                    int takeProfitPercent;
                    int stopLossPercent;

                    try {
                        positionSide = PositionSide.valueOf(ctx.secondArg());
                        amount = new Amount(ctx.thirdArg());
                        leverage = Integer.parseInt(ctx.arguments()[3]);

                        takeProfitPercent = Integer.parseInt(ctx.arguments()[4]);
                        stopLossPercent = Integer.parseInt(ctx.arguments()[5]);

                        if (takeProfitPercent < 0 || stopLossPercent < 0 || takeProfitPercent > 99 || stopLossPercent > 99 || leverage <= 0) {
                            throw new IllegalArgumentException();
                        }
                    } catch (IllegalArgumentException illegalArgumentException) {
                        TradeLogger.logTgBot(I18nSupport.i18n_literals("position.not.open",
                                illegalArgumentException.getMessage()));
                        return;
                    }

                    if (positionSide.equals(PositionSide.LONG)) {
                        requestSender.openLongPositionMarket(ctx.firstArg(), MarginType.ISOLATED, amount, leverage);
                    } else {
                        requestSender.openShortPositionMarket(ctx.firstArg(), MarginType.ISOLATED, amount, leverage);
                    }

                    TradeLogger.logOpenPosition(requestSender.getPosition(ctx.firstArg(), positionSide));
                    TradeLogger.logTP_SLOrders(requestSender.postTP_SLOrders(ctx.firstArg(), positionSide, takeProfitPercent, stopLossPercent));
                })
                .build();
    }

    public Ability closePosition() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("close.position"))
                .info(I18nSupport.i18n_literals("close.position.info"))
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

    public Ability enableStrategy() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("enable.strategy"))
                .info(I18nSupport.i18n_literals("enable.strategy.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(8)
                .action(ctx -> {
                    try {
                        if (ctx.firstArg().equals(MFI_BigGuyHandler.NAME)) {
                            tradeBot.enabledStrategies.put(ctx.firstArg(), new MFI_BigGuyHandler(requestSender,
                                    new StrategyProps(ctx.secondArg(),
                                            new Amount(ctx.thirdArg()),
                                            Integer.parseInt(ctx.arguments()[3]),
                                            Integer.parseInt(ctx.arguments()[4]),
                                            Integer.parseInt(ctx.arguments()[5]),
                                            Integer.parseInt(ctx.arguments()[6]),
                                            Boolean.parseBoolean(ctx.arguments()[7]))));
                            TradeLogger.logTgBot(I18nSupport.i18n_literals("strategy.enabled"));
                        } else {
                            throw new IllegalArgumentException("Strategy is not supported!");
                        }
                    } catch (IllegalArgumentException illegalArgumentException) {
                        TradeLogger.logTgBot(illegalArgumentException.getMessage());
                    }
                })
                .build();
    }

    public Ability disableStrategy() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("disable.strategy"))
                .info(I18nSupport.i18n_literals("disable.strategy.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> {
                    if (tradeBot.enabledStrategies.remove(ctx.firstArg()) != null) {
                        TradeLogger.logTgBot(I18nSupport.i18n_literals("strategy.disabled"));
                    } else {
                        TradeLogger.logTgBot(I18nSupport.i18n_literals("strategy.not.found"));
                    }
                })
                .build();
    }

    public Ability getHandlers() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("get.enabled.handlers"))
                .info(I18nSupport.i18n_literals("get.enabled.handlers.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> TradeLogger.logTgBot(tradeBot.enabledStrategies.values().stream().map(object -> object.getClass().getCanonicalName()).collect(Collectors.joining("\n"))))
                .build();
    }
}
