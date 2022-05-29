package com.tgbot;

import com.futures.dualside.RequestSender;
import com.signal.Indicator;
import com.strategies.*;
import com.tradebot.TradeBot;
import com.utils.Constants;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TelegramTradeBot extends AbilityBot {
    private final static String STRATEGY_DB = "strategies";
    private final RequestSender requestSender;
    private final List<StrategyHandler> enabledStrategies;
    private final long creatorId;
    private final TradeBot tradeBot;

    public final AsyncSender asyncSender;

    public TelegramTradeBot(String botToken,
                            String botUsername,
                            long creatorId,
                            RequestSender requestSender,
                            List<StrategyHandler> enabledStrategies, TradeBot tradeBot) {
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

        List<StrategyProps> strategyPropsList = db.getList(STRATEGY_DB);

        for (StrategyProps strategyProps : strategyPropsList) {
            try {
                enableStrategy(strategyProps);
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

        if (enabledStrategies.size() > 0) {
            db().getList(STRATEGY_DB).clear();

            db.getList(STRATEGY_DB).addAll(enabledStrategies.stream()
                    .map(StrategyHandler::getStrategyProps)
                    .collect(Collectors.toList()));
        }
    }

    public Ability enableStrategy_() {
        return Ability.builder()
                .name(I18nSupport.i18n_literals("enable.strategy"))
                .info(I18nSupport.i18n_literals("enable.strategy.info"))
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(3)
                .action(ctx -> {
                    try {
                        String[] tickers = ctx.secondArg().split(",");

                        enableStrategy(new StrategyProps(Strategy.valueOf(ctx.firstArg()),
                                tickers.length == 1 && tickers[0].equalsIgnoreCase(Constants.NULL) ? null : Arrays.asList(tickers),
                                ctx.thirdArg().equalsIgnoreCase(Constants.NULL) ? null : ctx.thirdArg(), ctx.chatId()));

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
                    try {
                        StrategyHandler strategyHandler = enabledStrategies.remove(Integer.parseInt(ctx.firstArg()));
                        strategyHandler.close();
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("strategy.disabled"), ctx.chatId());
                    } catch (NumberFormatException|IndexOutOfBoundsException ignored) {
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
                    StringBuilder stringBuilder =
                            new StringBuilder(I18nSupport.i18n_literals("enabled.strategies"))
                            .append("\n\n");
                    String enabledStrategyStr;

                    List<StrategyProps> strategyPropsList = enabledStrategies.stream()
                            .map(StrategyHandler::getStrategyProps)
                            .collect(Collectors.toList());

                    StrategyProps strategyProps;

                    for (int i = 0; i < strategyPropsList.size(); i++) {
                        strategyProps = strategyPropsList.get(i);

                        enabledStrategyStr = I18nSupport.i18n_literals("enabled.strategy",
                                i,
                                strategyProps.getStrategy(),
                                strategyProps.getTickers().size() > 0 ?
                                        String.join("\n", strategyProps.getTickers()) :
                                        I18nSupport.i18n_literals("any.ticker"),
                                Stream.of(strategyProps.getLogChatIds())
                                        .map(String::valueOf).collect(Collectors.joining(",")),
                                strategyProps.getProperties()) + "\n\n";

                        if (stringBuilder.toString().length() + enabledStrategyStr.length() > 4096) {
                            asyncSender.sendTextMsgAsync(stringBuilder.toString(), ctx.chatId());
                            stringBuilder.setLength(0);
                            stringBuilder.append(I18nSupport.i18n_literals("enabled.strategies")).append("\n\n");
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
                        logFileNames = Arrays.stream(Indicator.values())
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
                    try {
                        StrategyHandler strategyHandler = enabledStrategies.get(Integer.parseInt(ctx.firstArg()));

                        strategyHandler.getStrategyProps().addLogChatId(ctx.chatId());
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("log.chat.added",
                                String.join(" ", strategyHandler.getStrategyProps().getTickers()),
                                strategyHandler.getStrategyProps().getStrategy()), ctx.chatId());
                    } catch (IndexOutOfBoundsException|NumberFormatException ignored) {
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
                    try {
                        StrategyHandler strategyHandler = enabledStrategies.get(Integer.parseInt(ctx.firstArg()));

                        List<Long> logChatIds =
                                Arrays.stream(ctx.secondArg().split(",")).map(Long::parseLong).collect(Collectors.toList());

                        strategyHandler.getStrategyProps()
                                .setLogChatId(logChatIds);
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("log.chat.added",
                                String.join(" ", strategyHandler.getStrategyProps().getTickers()),
                                strategyHandler.getStrategyProps().getStrategy()), logChatIds);
                    } catch (NumberFormatException|IndexOutOfBoundsException ignored) {
                        asyncSender.sendTextMsgAsync(I18nSupport.i18n_literals("log.chat.not.added"), ctx.chatId());
                    }
                })
                .build();
    }

    public void enableStrategy(StrategyProps strategyProps) throws IllegalArgumentException, NullPointerException {
        if (strategyProps.getStrategy().equals(Strategy.MFI_BIG_GUY)) {
            enabledStrategies.add(new MFI_BigGuyHandler(requestSender, strategyProps, asyncSender));
        } else if (strategyProps.getStrategy().equals(Strategy.ALTCOINS_1h_4h)) {
            enabledStrategies.add(new Altcoins1h4hHandler(requestSender, strategyProps, asyncSender));
        } else if (strategyProps.getStrategy().equals(Strategy.ALTCOINS)) {
            enabledStrategies.add(new AltcoinsHandler(requestSender, strategyProps, asyncSender));
        } else if (strategyProps.getStrategy().equals(Strategy.ALARM)) {
            enabledStrategies.add(new AlarmHandler(requestSender, strategyProps, asyncSender));
        } else if (strategyProps.getStrategy().equals(Strategy.CHIA_BALANCE_ALARM)) {
            enabledStrategies.add(new ChiaAlarmHandler(requestSender, strategyProps, asyncSender));
        } else {
            throw new IllegalArgumentException("Strategy is not supported!");
        }
    }
}