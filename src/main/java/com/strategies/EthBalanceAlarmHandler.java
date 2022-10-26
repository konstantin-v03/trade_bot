package com.strategies;

import com.futures.dualside.RequestSender;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import okhttp3.HttpUrl;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class EthBalanceAlarmHandler extends BalanceAlarmHandler {
    private final String symbol;
    private final Integer decimals;
    private final String alias;

    public EthBalanceAlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        super(requestSender, strategyProps, asyncSender);

        symbol = Optional
                .ofNullable(strategyProps.getProperties().get(Constants.TOKEN_SYMBOL.getKey()))
                .orElse("ETH");
        alias = Objects.requireNonNull(strategyProps.getProperties().get(Constants.ALIAS.getKey()));
        decimals = Integer.parseInt(Optional
                .ofNullable(strategyProps.getProperties().get(Constants.DECIMALS.getKey()))
                .orElse("18"));

        HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host("api.etherscan.io")
                .addPathSegment("api")
                .addQueryParameter("module", "account")
                .addQueryParameter("address",
                        Objects.requireNonNull(strategyProps.getProperties().get(Constants.ADDRESS.getKey())))
                .addQueryParameter("apikey",
                        Objects.requireNonNull(strategyProps.getProperties().get(Constants.API_KEY.getKey())));

        String contractAddress = strategyProps.getProperties().get(Constants.CONTRACT_ADDRESS.getKey());

        if (contractAddress != null) {
            httpUrlBuilder
                    .addQueryParameter("contractaddress", contractAddress)
                    .addQueryParameter("action", "tokenbalance");
        } else {
            httpUrlBuilder
                    .addQueryParameter("action", "balance");
        }

        httpUrl = httpUrlBuilder.build();

        startScheduler();
    }

    @Override
    public BigDecimal balanceFromJson(JSONObject jsonObject) {
        return jsonObject.getBigDecimal("result")
                .divide(BigDecimal.valueOf(Math.pow(10, decimals)), RoundingMode.FLOOR);
    }

    @Override
    public void logBalanceIncreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("erc20.balance.changed",
                symbol,
                alias,
                String.format("%.1f", currBalance),
                "+" + String.format("%.1f", currBalance.subtract(lastBalance))));
    }

    @Override
    public void logBalanceDecreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("erc20.balance.changed",
                symbol,
                alias,
                String.format("%.1f", currBalance),
                "-" + String.format("%.1f", lastBalance.subtract(currBalance))));
    }
}
