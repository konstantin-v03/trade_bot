package com.strategies;

import com.futures.dualside.RequestSender;
import com.signal.ALARM_SIGNAL;
import com.signal.Indicator;
import com.signal.STRATEGY_ALARM_SIGNAL;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import com.utils.Scheduler;
import com.utils.Utils;
import org.checkerframework.checker.units.qual.C;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.abilitybots.api.util.Pair;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AlarmHandler extends StrategyHandler {
    private final Scheduler scheduler;
    private Map<Pair<Indicator, String>, Integer> dailyOncePerMinuteCount;
    private final boolean isLogOncePerMinute;

    public AlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender, long exceptionChatId) {
        super(requestSender, strategyProps, asyncSender, exceptionChatId);

        dailyOncePerMinuteCount = new ConcurrentHashMap<>();
        isLogOncePerMinute = Boolean.parseBoolean(strategyProps.getProperties().get(Constants.IS_LOG_ONCE_PER_MINUTE));

        if (Boolean.parseBoolean(strategyProps.getProperties().get(Constants.SCHEDULER_STR))) {
            scheduler = Scheduler.scheduleEveryDayAtFixedTime(() -> {
                Map<Pair<Indicator, String>, Integer> dailyOncePerMinuteCountTemp = dailyOncePerMinuteCount;
                dailyOncePerMinuteCount = new ConcurrentHashMap<>();

                Integer count;

                if (dailyOncePerMinuteCountTemp.keySet().size() > 0) {
                    for (Pair<Indicator, String> indicatorTickerPair : dailyOncePerMinuteCountTemp.keySet()) {
                        count = dailyOncePerMinuteCountTemp.get(indicatorTickerPair);

                        logger.logTgBot(I18nSupport.i18n_literals("alarm.once.per.minute.count",
                                indicatorTickerPair.b(),
                                indicatorTickerPair.a().ordinal(),
                                indicatorTickerPair.a().alias(),
                                count));

                        try {
                            Utils.appendStrToFile(getOncePerMinuteCountLogFileName(indicatorTickerPair.a()),
                                    LocalDate.now().minusDays(1).format(DateTimeFormatter
                                            .ofLocalizedDate(FormatStyle.SHORT)) + " " + count + "\n");
                        } catch (IOException ioException) {
                            logger.logException(ioException);
                        }
                    }
                } else {
                    logger.logTgBot(I18nSupport.i18n_literals("no.alarm.per.minute"));
                }
            }, 0, 0, 0);
        } else {
            scheduler = null;
        }
    }

    @Override
    public void process(Indicator indicator, JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        if (indicator.isStrategy()) {
            STRATEGY_ALARM_SIGNAL strategyAlarmSignal = new STRATEGY_ALARM_SIGNAL(inputSignal);
            STRATEGY_ALARM_SIGNAL.Action action = strategyAlarmSignal.getAction();

            logger.logTgBot(I18nSupport.i18n_literals("strategy.alarm",
                    strategyAlarmSignal.getTicker(),
                    strategyAlarmSignal.getExchange(),
                    indicator.alias(),
                    Utils.intToInterval(strategyAlarmSignal.getInterval()),
                    action.alias(),
                    strategyAlarmSignal.getClose()));
        } else {
            ALARM_SIGNAL alarmSignal = new ALARM_SIGNAL(inputSignal);

            ALARM_SIGNAL.Option option = alarmSignal.getOption();
            Pair<Indicator, String> indicatorTickerPair = Pair.of(indicator, alarmSignal.getTicker());

            if (option.equals(ALARM_SIGNAL.Option.ONCE_PER_MINUTE)) {
                dailyOncePerMinuteCount.putIfAbsent(indicatorTickerPair, 0);
                dailyOncePerMinuteCount.put(indicatorTickerPair,
                        dailyOncePerMinuteCount.get(indicatorTickerPair) + 1);

                if (isLogOncePerMinute) {
                    logger.logTgBot(I18nSupport.i18n_literals("alarm.once.per.minute",
                            alarmSignal.getTicker(),
                            alarmSignal.getExchange(),
                            indicator.ordinal(),
                            indicator.alias(),
                            Utils.intToInterval(alarmSignal.getInterval())));
                }
            } else {
                logger.log$pinTgBot(I18nSupport.i18n_literals("alarm.once.per.bar.close",
                        alarmSignal.getTicker(),
                        alarmSignal.getExchange(),
                        indicator.ordinal(),
                        indicator.alias(),
                        Utils.intToInterval(alarmSignal.getInterval())));
            }
        }
    }

    @Override
    public void close() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return !(strategyProps.getTickers().size() > 0 && !strategyProps.getTickers().contains(ticker))
                && (signal.equals(ALARM_SIGNAL.class) || signal.equals(STRATEGY_ALARM_SIGNAL.class));
    }

    public static String getOncePerMinuteCountLogFileName(Indicator indicator) {
        return Strategy.ALARM + "_" + ALARM_SIGNAL.Option.ONCE_PER_MINUTE + "_" + indicator + ".txt";
    }
}
