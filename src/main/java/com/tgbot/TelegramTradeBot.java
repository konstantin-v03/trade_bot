package com.tgbot;

import com.futures.dualside.RequestSender;
import com.signal.ALARM_SIGNAL;
import com.strategies.*;
import com.tradebot.TradeBot;
import com.utils.I18nSupport;
import com.utils.TgBotUtils;
import com.utils.Utils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.abilitybots.api.toggle.CustomToggle;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TelegramTradeBot extends AbilityBot {
    private final static String STRATEGY_DB = "strategies";
    private final RequestSender requestSender;
    private final Map<String, StrategyHandler> enabledStrategies;
    private final long creatorId;
    private final TradeBot tradeBot;

    public final AsyncSender asyncSender;

    public TelegramTradeBot(String botToken,
                            String botUsername,
                            long creatorId,
                            RequestSender requestSender,
                            Map<String, StrategyHandler> enabledStrategies, TradeBot tradeBot) {
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
        this.enabledStrategies = enabledStrategies;
        this.tradeBot = tradeBot;
        asyncSender = new AsyncSender(this);

        Map<String, StrategyProps> strategies = db.getMap(STRATEGY_DB);

        for (String ticker : strategies.keySet()) {
            try {
                enableStrategy(ticker, strategies.get(ticker));
            } catch (IllegalArgumentException|NullPointerException exception) {
                asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("error.occured", exception), creatorId);
            }
        }
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    @Override
    public void onUpdateReceived(Update update) {
        super.onUpdateReceived(update);
        saveEnabledStrategies();
    }

    public Ability enableStrategy_() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("enable.strategy"))
                .info(I18nSupport.i18n_literals("enable.strategy.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(4)
                .action(ctx -> {
                    try {
                        for (String ticker : ctx.secondArg().split(",")) {
                            enableStrategy(ticker, new StrategyProps(Strategy.valueOf(ctx.firstArg()),
                                    ticker,
                                    Boolean.parseBoolean(ctx.arguments()[2]),
                                    ctx.arguments()[3], Stream.of(ctx.chatId()).collect(Collectors.toList())));
                        }

                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("strategy.enabled"), ctx.chatId());
                    } catch (IllegalArgumentException|NullPointerException exception) {
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("error.occured", exception), ctx.chatId());
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
                    StrategyHandler strategyHandler;

                    if ((strategyHandler = enabledStrategies.remove(ctx.firstArg())) != null) {
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("strategy.disabled"), ctx.chatId());
                        strategyHandler.close();
                    } else {
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("strategy.not.found"), ctx.chatId());
                    }
                })
                .build();
    }

    public Ability getSupportedStrategies() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("get.supported.strategies"))
                .info(I18nSupport.i18n_literals("get.supported.strategies.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("supported.strategies",
                        Arrays.stream(Strategy.values())
                                .map(str -> I18nSupport.i18n_literals("supported.strategy", str))
                                .collect(Collectors.joining("\n"))), ctx.chatId()))
                .build();
    }

    public Ability getEnabledStrategies() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("get.enabled.strategies"))
                .info(I18nSupport.i18n_literals("get.enabled.strategies.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> {
                    int i = 1;
                    StringBuilder stringBuilder = new StringBuilder()
                            .append(I18nSupport.i18n_literals("enabled.strategies", i))
                            .append("\n\n");
                    String enabledStrategyStr;

                    for (StrategyProps strategyProps : enabledStrategies
                            .values()
                            .stream()
                            .map(StrategyHandler::getStrategyProps)
                            .collect(Collectors.toList())) {
                        enabledStrategyStr = I18nSupport.i18n_literals("enabled.strategy",
                                strategyProps.getTicker(),
                                strategyProps.getStrategy(),
                                strategyProps.getLogChatIds().stream().map(chatId ->
                                        "<code>" + chatId + "</code>").collect(Collectors.joining("\n")),
                                strategyProps.isDebugMode() ? 0 : 1,
                                strategyProps.getProperties()) + "\n\n";

                        if (stringBuilder.toString().length() + enabledStrategyStr.length() > 4096) {
                            asyncSender.sendTextMsgAsync(stringBuilder.toString(), ctx.chatId());
                            stringBuilder.setLength(0);
                            stringBuilder.append(I18nSupport.i18n_literals("enabled.strategies", ++i)).append("\n\n");
                        }

                        stringBuilder.append(enabledStrategyStr);
                    }

                    asyncSender.sendTextMsgAsync(stringBuilder.toString(), ctx.chatId());
                })
                .build();
    }

    public Ability getLog() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("get.log"))
                .info(I18nSupport.i18n_literals("get.log.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(2)
                .action(ctx -> {
                    Strategy strategy;

                    try {
                        strategy = Strategy.valueOf(ctx.firstArg());
                    } catch (IllegalArgumentException illegalArgumentException) {
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("error.occured", illegalArgumentException), ctx.chatId());
                        return;
                    }

                    List<String> logFileNames;

                    if (strategy.equals(Strategy.ALARM)) {
                        logFileNames = Arrays.stream(ALARM_SIGNAL.Indicator.values())
                                .map(AlarmHandler::getOncePerMinuteCountLogFileName)
                                .collect(Collectors.toList());
                    } else {
                        logFileNames = Utils.getLogFileNames(strategy, ctx.secondArg());
                    }

                    for (String logFileName : logFileNames) {
                        executeAsync(SendDocument
                                .builder()
                                .chatId(String.valueOf(ctx.chatId()))
                                .document(new InputFile().setMedia(new File(logFileName)))
                                .build());
                    }
                })
                .build();
    }

    public Ability processSignal() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("process.signal"))
                .info(I18nSupport.i18n_literals("process.signal.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> silent.forceReply(I18nSupport.i18n_literals("process.signal.response"), ctx.chatId()))
                .reply((baseAbilityBot, update) -> tradeBot.process(update.getMessage().getText()),
                        Flag.MESSAGE,
                        Flag.REPLY,
                        TgBotUtils.isReplyToBot(getBotUsername()),
                        TgBotUtils.isReplyToMessage(I18nSupport.i18n_literals("process.signal.response"))
                )
                .build();
    }

    public Ability initLogChat() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("init.log.chat"))
                .info(I18nSupport.i18n_literals("init.log.chat.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.ALL)
                .input(1)
                .action(ctx -> {
                    StrategyHandler strategyHandler = enabledStrategies.get(ctx.firstArg());

                    if (strategyHandler != null) {
                        strategyHandler.getStrategyProps().addLogChatId(ctx.chatId());
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("log.chat.added",
                                strategyHandler.getStrategyProps().getTicker(),
                                strategyHandler.getStrategyProps().getStrategy()), ctx.chatId());
                    } else {
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("log.chat.not.added"), ctx.chatId());
                    }
                })
                .build();
    }

    public Ability setLogChatIds() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("set.log.chat"))
                .info(I18nSupport.i18n_literals("set.log.chat.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.ALL)
                .input(2)
                .action(ctx -> {
                    StrategyHandler strategyHandler = enabledStrategies.get(ctx.firstArg());

                    try {
                        List<Long> logChatIds =
                                Arrays.stream(ctx.secondArg().split(",")).map(Long::parseLong).collect(Collectors.toList());

                        if (strategyHandler != null) {
                            strategyHandler.getStrategyProps()
                                    .setLogChatId(logChatIds);
                            asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("log.chat.added",
                                    strategyHandler.getStrategyProps().getTicker(),
                                    strategyHandler.getStrategyProps().getStrategy()), logChatIds);
                        } else {
                            throw new NullPointerException();
                        }
                    } catch (NumberFormatException|NullPointerException numberFormatException) {
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("log.chat.not.added"), ctx.chatId());
                    }
                })
                .build();
    }

    public void enableStrategy(String ticker, StrategyProps strategyProps) throws IllegalArgumentException, NullPointerException {
        Strategy strategy = strategyProps.getStrategy();

        if (strategy.equals(Strategy.MFI_BIG_GUY)) {
            enabledStrategies.put(ticker,
                    new MFI_BigGuyHandler(requestSender, strategyProps, asyncSender));
        } else if (strategy.equals(Strategy.ALTCOINS_1h_4h)) {
            enabledStrategies.put(ticker,
                    new Altcoins1h4hHandler(requestSender, strategyProps, asyncSender));
        } else if (strategy.equals(Strategy.ALTCOINS)) {
            enabledStrategies.put(ticker,
                    new AltcoinsHandler(requestSender, strategyProps, asyncSender));
        } else if (strategy.equals(Strategy.ALARM)) {
            enabledStrategies.put(ticker,
                    new AlarmHandler(requestSender, strategyProps, asyncSender));
        } else if (strategy.equals(Strategy.CHIA_BALANCE_ALARM)) {
            enabledStrategies.put(ticker,
                    new ChiaAlarmHandler(requestSender, strategyProps, asyncSender));
        } else {
            throw new IllegalArgumentException("Strategy is not supported!");
        }
    }

    public void saveEnabledStrategies() {
        Map<String, StrategyProps> strategies = db.getMap(STRATEGY_DB);

        strategies.clear();

        for (String ticker : enabledStrategies.keySet()) {
            strategies.putIfAbsent(ticker, enabledStrategies.get(ticker).getStrategyProps());
        }

        db.commit();
    }
}