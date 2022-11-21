package com.strategies;

import com.futures.dualside.RequestSender;
import com.signal.Indicator;
import com.tgbot.AsyncSender;
import com.utils.I18nSupport;
import com.utils.Scheduler;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BalanceAlarmHandler extends StrategyHandler {
    private Scheduler scheduler;
    private OkHttpClient client;
    private BigDecimal lastBalance;

    protected HttpUrl httpUrl;
    protected RequestBody requestBody;
    protected final Map<String, String> httpRequestHeaders = new HashMap<>();

    public BalanceAlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        super(requestSender, strategyProps, asyncSender);
    }

    protected final void startScheduler() {
        client = new OkHttpClient();

        scheduler = Scheduler.scheduleEveryMinute(() -> {
            Response response;

            try {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(httpUrl);

                for (String key : httpRequestHeaders.keySet()) {
                    requestBuilder.addHeader(key, httpRequestHeaders.get(key));
                }

                if (requestBody != null) {
                    requestBuilder.post(requestBody);
                }

                response = client
                        .newCall(requestBuilder.build())
                        .execute();
            } catch (IOException ioException) {
//                logger.logException(ioException);
                return;
            }

            BigDecimal currBalance;

            try {
                currBalance = balanceFromJson(new JSONObject(
                        new BufferedReader(
                                new InputStreamReader(Objects.requireNonNull(response.body()).byteStream(),
                                        StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"))));
            } catch (JSONException jsonException) {
//                logger.logException(jsonException);
                return;
            }

            if (lastBalance == null) {
                lastBalance = currBalance;
            }

            if (currBalance.compareTo(lastBalance) > 0) {
                logBalanceIncreased(currBalance, lastBalance);
            } else if (currBalance.compareTo(lastBalance) < 0) {
                logBalanceDecreased(currBalance, lastBalance);
            }

            lastBalance = currBalance;
        }, 1);
    }

    public abstract BigDecimal balanceFromJson(JSONObject jsonObject);

    public abstract void logBalanceIncreased(BigDecimal currBalance, BigDecimal lastBalance);

    public abstract void logBalanceDecreased(BigDecimal currBalance, BigDecimal lastBalance);

    @Override
    public void process(Indicator indicator, JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        logger.logTgBot(I18nSupport.i18n_literals("unsupported.signal.exception"));
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return false;
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
