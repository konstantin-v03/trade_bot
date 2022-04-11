package com.strategies;

import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.ALARM_SIGNAL;
import com.signal.Signal;
import com.utils.I18nSupport;
import com.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlarmHandler extends StrategyHandler {
    private Map<ALARM_SIGNAL.Indicator, Integer> dailyOncePerMinuteCount;

    public AlarmHandler(RequestSender requestSender, StrategyProps strategyProps) {
        super(requestSender, strategyProps);
        dailyOncePerMinuteCount = new ConcurrentHashMap<>();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);

        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).getSeconds();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Map<ALARM_SIGNAL.Indicator, Integer> dailyOncePerMinuteCountTemp = dailyOncePerMinuteCount;
            dailyOncePerMinuteCount = new ConcurrentHashMap<>();

            Integer count;

            for (ALARM_SIGNAL.Indicator indicator : dailyOncePerMinuteCountTemp.keySet()) {
                count = dailyOncePerMinuteCountTemp.get(indicator);

                TradeLogger.logTgBot(I18nSupport.i18n_literals("alarm.once.per.minute.count",
                        strategyProps.getTicker(),
                        indicator.ordinal(),
                        indicator.alias(),
                        count));

                try {
                    Utils.appendStrToFile(getOncePerMinuteCountLogFileName(indicator),
                            LocalDate.now().minusDays(1).format(DateTimeFormatter
                                    .ofLocalizedDate(FormatStyle.SHORT)) + " " + count + "\n");
                } catch (IOException ignored) {

                }
            }
        },
                initialDelay,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
    }

    @Override
    public void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        Class<?> signalClass = Signal.getSignalClass(inputSignal);

        ALARM_SIGNAL alarmSignal;

        if (signalClass == ALARM_SIGNAL.class) {
            alarmSignal = new ALARM_SIGNAL(inputSignal);
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        ALARM_SIGNAL.Indicator indicator = alarmSignal.getIndicator();
        ALARM_SIGNAL.Option option = alarmSignal.getOption();

        if (option.equals(ALARM_SIGNAL.Option.ONCE_PER_MINUTE)) {
            dailyOncePerMinuteCount.putIfAbsent(indicator, 0);
            dailyOncePerMinuteCount.put(indicator, dailyOncePerMinuteCount.get(indicator) + 1);
        } else if (option.equals(ALARM_SIGNAL.Option.ONCE_PER_BAR_CLOSE)){
            TradeLogger.log$pinTgBot(I18nSupport.i18n_literals("alarm.once.per.bar.close",
                    strategyProps.getTicker(),
                    indicator.ordinal(),
                    indicator.alias()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void close() {

    }

    public static String getOncePerMinuteCountLogFileName(ALARM_SIGNAL.Indicator indicator) {
        return Strategy.ALARM + "_" + ALARM_SIGNAL.Option.ONCE_PER_MINUTE + "_" + indicator + ".txt";
    }
}
