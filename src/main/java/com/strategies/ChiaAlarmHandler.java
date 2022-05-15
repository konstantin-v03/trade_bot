package com.strategies;

import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
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
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ChiaAlarmHandler extends StrategyHandler {
    private final Scheduler scheduler;

    private final String alias;
    private final String address;

    private final OkHttpClient client;
    private double lastXch = -1;

    public ChiaAlarmHandler(RequestSender requestSender, StrategyProps strategyProps, TradeLogger tradeLogger) throws IllegalArgumentException {
        super(requestSender, strategyProps, tradeLogger);
        address = strategyProps.getProperties().getProperty("address");
        alias = strategyProps.getProperties().getProperty("alias");
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
                    new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n")));

            double xch = responseJSON.getDouble("xch");

            if (lastXch == -1) {
                lastXch = xch;
            }

            if (xch != lastXch) {
                if (xch > lastXch) {
                    tradeLogger.logTgBot(I18nSupport.i18n_literals("chia.balance.changed",
                            alias,
                            String.format("%.1f", xch),
                            "+" + String.format("%.1f", xch - lastXch)));
                }

                if (xch < lastXch) {
                    if (xch == 0) {
                        tradeLogger.log$pinTgBot(I18nSupport.i18n_literals("chia.balance.changed",
                                alias,
                                String.format("%.1f", xch),
                                "-" + String.format("%.1f", lastXch - xch)));
                    } else {
                        tradeLogger.logTgBot(I18nSupport.i18n_literals("chia.balance.changed",
                                alias,
                                String.format("%.1f", xch),
                                "-" + String.format("%.1f", lastXch - xch)));
                    }
                }

                lastXch = xch;
            }
        });
    }

    @Override
    public void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        tradeLogger.logTgBot(I18nSupport.i18n_literals("unsupported.signal.exception"));
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
