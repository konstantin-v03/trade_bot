package com.strategies;

import com.futures.dualside.RequestSender;
import com.signal.ALARM_SIGNAL;
import com.signal.STRATEGY_ALARM;
import com.signal.Signal;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import com.utils.Scheduler;
import com.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AlarmHandler extends StrategyHandler {
    private final Scheduler scheduler;
    private Map<ALARM_SIGNAL.Indicator, Integer> dailyOncePerMinuteCount;

    public AlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        super(requestSender, strategyProps, asyncSender);

        dailyOncePerMinuteCount = new ConcurrentHashMap<>();
        String schedulerProperty = strategyProps.getProperties().getProperty(Constants.SCHEDULER);

        if (Boolean.parseBoolean(schedulerProperty)) {
            scheduler = Scheduler.scheduleEveryDayAtFixedTime(() -> {
                Map<ALARM_SIGNAL.Indicator, Integer> dailyOncePerMinuteCountTemp = dailyOncePerMinuteCount;
                dailyOncePerMinuteCount = new ConcurrentHashMap<>();

                Integer count;

                if (dailyOncePerMinuteCountTemp.keySet().size() > 0) {
                    for (ALARM_SIGNAL.Indicator indicator : dailyOncePerMinuteCountTemp.keySet()) {
                        count = dailyOncePerMinuteCountTemp.get(indicator);

                        logger.log$pinTgBot(I18nSupport.i18n_literals("alarm.once.per.minute.count",
                                strategyProps.getTicker(),
                                indicator.ordinal(),
                                indicator.alias(),
                                count));

                        try {
                            Utils.appendStrToFile(getOncePerMinuteCountLogFileName(indicator),
                                    LocalDate.now().minusDays(1).format(DateTimeFormatter
                                            .ofLocalizedDate(FormatStyle.SHORT)) + " " + count + "\n");
                        } catch (IOException ioException) {
                            logger.logException(ioException);
                        }
                    }
                } else {
                    logger.logTgBot(I18nSupport.i18n_literals("no.alarm.per.minute", strategyProps.getTicker()));
                }
            }, 0, 0, 0);
        } else {
            strategyProps.getProperties().put(Constants.SCHEDULER, Boolean.TRUE.toString());
            scheduler = null;
        }
    }

    @Override
    public void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        Class<?> signalClass = Signal.getSignalClass(inputSignal);

        if (signalClass == ALARM_SIGNAL.class) {
            ALARM_SIGNAL alarmSignal = new ALARM_SIGNAL(inputSignal);

            ALARM_SIGNAL.Indicator indicator = alarmSignal.getIndicator();
            ALARM_SIGNAL.Option option = alarmSignal.getOption();

            if (option.equals(ALARM_SIGNAL.Option.ONCE_PER_MINUTE)) {
                dailyOncePerMinuteCount.putIfAbsent(indicator, 0);
                logger.logTgBot(I18nSupport.i18n_literals("alarm.once.per.minute",
                        strategyProps.getTicker(),
                        alarmSignal.getExchange(),
                        indicator.ordinal(),
                        indicator.alias(),
                        Utils.intToInterval(alarmSignal.getInterval())));
                dailyOncePerMinuteCount.put(indicator, dailyOncePerMinuteCount.get(indicator) + 1);
            } else if (option.equals(ALARM_SIGNAL.Option.ONCE_PER_BAR_CLOSE)){
                logger.log$pinTgBot(I18nSupport.i18n_literals("alarm.once.per.bar.close",
                        strategyProps.getTicker(),
                        alarmSignal.getExchange(),
                        indicator.ordinal(),
                        indicator.alias(),
                        Utils.intToInterval(alarmSignal.getInterval())));
            } else {
                throw new IllegalArgumentException();
            }
        } else if (signalClass == STRATEGY_ALARM.class){
            STRATEGY_ALARM strategyAlarmSignal = new STRATEGY_ALARM(inputSignal);

            STRATEGY_ALARM.Indicator indicator = strategyAlarmSignal.getIndicator();
            STRATEGY_ALARM.Action action = strategyAlarmSignal.getAction();

            logger.logTgBot(I18nSupport.i18n_literals("strategy.alarm",
                    strategyProps.getTicker(),
                    strategyAlarmSignal.getExchange(),
                    indicator.alias(),
                    action.alias(),
                    strategyAlarmSignal.getClose()));
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }
    }

    @Override
    public void close() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public static String getOncePerMinuteCountLogFileName(ALARM_SIGNAL.Indicator indicator) {
        return Strategy.ALARM + "_" + ALARM_SIGNAL.Option.ONCE_PER_MINUTE + "_" + indicator + ".txt";
    }
}
