package com.strategies;

import com.futures.dualside.RequestSender;
import com.signal.Indicator;
import com.tgbot.AsyncSender;
import com.utils.I18nSupport;
import com.utils.Scheduler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChiaAlarmHandler extends StrategyHandler {
    private final Scheduler scheduler;

    private final String alias;
    private final String address;

    private final OkHttpClient client;
    private BigDecimal lastXch;

    public ChiaAlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) throws IllegalArgumentException, NullPointerException {
        super(requestSender, strategyProps, asyncSender);
        address = strategyProps.getProperties().get("address");
        alias = strategyProps.getProperties().get("alias");
        client = new OkHttpClient();

        scheduler = Scheduler.scheduleEveryMinute(() -> {
            Response response;

            try {
                response = client
                        .newCall(new Request.Builder().url("https://xchscan.com/api/account/balance?address=" + address).build())
                        .execute();
            } catch (IOException ignored) {
                return;
            }

            JSONObject responseJSON = new JSONObject(new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(response.body()).byteStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n")));

            BigDecimal xch = responseJSON.getBigDecimal("xch");

            if (lastXch == null) {
                lastXch = xch;
            }

            if (xch.compareTo(lastXch) > 0) {
                logger.logTgBot(I18nSupport.i18n_literals("chia.balance.changed",
                        alias,
                        String.format("%.1f", xch),
                        "+" + String.format("%.1f", xch.subtract(lastXch))));
            } else if (xch.compareTo(lastXch) < 0) {
                logger.log$pinTgBot(I18nSupport.i18n_literals("chia.balance.changed",
                        alias,
                        String.format("%.1f", xch),
                        "-" + String.format("%.1f", lastXch.subtract(xch))));
            }

            lastXch = xch;
        });
    }

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
