package com.strategies;

import com.futures.dualside.RequestSender;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class InterlayBalanceAlarmHandler extends BalanceAlarmHandler {
    private final String alias;

    public InterlayBalanceAlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        super(requestSender, strategyProps, asyncSender);

        alias = Objects.requireNonNull(strategyProps.getProperties().get(Constants.ALIAS.getKey()));
        httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("interlay.api.subscan.io")
                .addPathSegment("api")
                .addPathSegment("scan")
                .addPathSegment("account")
                .addPathSegment("tokens")
                .build();

        httpRequestHeaders.put("Content-Type", "application/json");
        httpRequestHeaders.put("X-API-Key",
                Objects.requireNonNull(strategyProps.getProperties().get(Constants.API_KEY.getKey())));

        requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                new JSONObject().put("address",
                        Objects.requireNonNull(strategyProps.getProperties().get(Constants.ADDRESS.getKey()))).toString());

        startScheduler();
    }

    @Override
    public BigDecimal balanceFromJson(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("native");
        JSONObject jsonObjectI;

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObjectI = jsonArray.getJSONObject(i);

            if (jsonObjectI.get("symbol").equals("INTR")) {
                return jsonObjectI
                        .getBigDecimal("balance")
                        .divide(BigDecimal.valueOf(Math.pow(10,
                                        jsonObjectI.getInt("decimals"))),
                                RoundingMode.FLOOR);
            }
        }

        return null;
    }

    @Override
    public void logBalanceIncreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("interlay.balance.changed",
                alias,
                String.format("%.1f", currBalance),
                "+" + String.format("%.1f", currBalance.subtract(lastBalance))));
    }

    @Override
    public void logBalanceDecreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("interlay.balance.changed",
                alias,
                String.format("%.1f", currBalance),
                "-" + String.format("%.1f", lastBalance.subtract(currBalance))));
    }
}
