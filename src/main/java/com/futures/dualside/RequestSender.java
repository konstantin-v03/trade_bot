package com.futures.dualside;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.ResponseResult;
import com.binance.client.model.enums.*;
import com.binance.client.model.market.ExchangeInfoEntry;
import com.binance.client.model.market.MarkPrice;
import com.binance.client.model.trade.*;
import com.futures.Amount;
import com.futures.Filter;
import com.futures.TP_SL;
import com.utils.Calculations;
import com.utils.I18nSupport;
import org.jetbrains.annotations.NonNls;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RequestSender {
    private final SyncRequestClient syncRequestClient;

    public RequestSender(@NonNls SyncRequestClient syncRequestClient) {
        this.syncRequestClient = syncRequestClient;

        if (!Boolean.parseBoolean(syncRequestClient.getPositionSide().getString("dualSidePosition"))) {
            syncRequestClient.changePositionSide("true");
        }
    }

    public synchronized Order openPositionPercentageOfMarginBalance(String symbol, PositionSide positionSide, MarginType marginType, int percentage, int leverage) throws NullPointerException, IllegalArgumentException {
        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("illegal.percentage", percentage));
        }

        Position position;

        if (percentage == 100 || (position = getPosition(symbol, positionSide)) == null) {
            return openPositionMarket(symbol, positionSide, marginType, new Amount(new BigDecimal(percentage), Amount.Type.PERCENT), leverage);
        }

        BigDecimal initialMargin = position.getInitialMargin();

        BigDecimal buyInAdditionMargin =
                Calculations.percentage(getAvailableBalance(RequestSender.getAssetBySymbol(symbol)).add(initialMargin),
                                new BigDecimal(percentage))
                        .subtract(initialMargin);

        if (buyInAdditionMargin.compareTo(BigDecimal.ZERO) > 0) {
            return openPositionMarket(symbol,
                    positionSide,
                    MarginType.ISOLATED,
                    new Amount(buyInAdditionMargin, Amount.Type.USD),
                    leverage);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public synchronized Order openPositionMarket(String symbol, PositionSide positionSide, MarginType marginType, Amount amount, int leverage) throws NullPointerException, IllegalArgumentException {
        OrderSide orderSide;

        if (positionSide.equals(PositionSide.LONG)) {
            orderSide = OrderSide.BUY;
        } else if (positionSide.equals(PositionSide.SHORT)) {
            orderSide = OrderSide.SELL;
        } else {
            throw new IllegalArgumentException();
        }

        ExchangeInfoEntry exchangeInfoEntry = getExchangeInfo(symbol);

        BigDecimal amountUSD = null;

        if (amount.getType().equals(Amount.Type.PERCENT)) {
            amountUSD = Amount.getAmountUSD(amount.getAmount(), Objects.requireNonNull(getAvailableBalance(getAssetBySymbol(symbol))));
        } else if (amount.getType().equals(Amount.Type.USD)){
            amountUSD = amount.getAmount();
        }

        BigDecimal quantity = Objects.requireNonNull(amountUSD).multiply(new BigDecimal(leverage)).divide(getLastPrice(symbol),
                new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(Objects.requireNonNull(exchangeInfoEntry).getFilters(),
                        Filter.Type.MARKET_LOT_SIZE,
                        Filter.Key.STEP_SIZE.toString()))).scale(), RoundingMode.FLOOR);

        List<PositionRisk> positionRisks = syncRequestClient.getPositionRisk(symbol);
        PositionRisk positionRisk = getPositionRisk(positionRisks, symbol, positionSide);

        if (positionRisk != null) {
            requireLegalQuantity(quantity, getLastPrice(symbol), exchangeInfoEntry);

            if (positionRisk.getLeverage().compareTo(new BigDecimal(leverage)) != 0) {
                syncRequestClient.changeInitialLeverage(symbol, leverage);
            }

            if ((positionRisk.getMarginType().equals("cross") && marginType.equals(MarginType.ISOLATED)) ||
                    (positionRisk.getMarginType().equals("isolated") && marginType.equals(MarginType.CROSSED)))  {
                syncRequestClient.changeMarginType(symbol, marginType);
            }

            return syncRequestClient.postOrder(symbol,
                    orderSide,
                    positionSide,
                    OrderType.MARKET,
                    null,
                    quantity.toString(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    NewOrderRespType.ACK);
        }

        return null;
    }

    public synchronized Order closePositionMarket(String symbol, PositionSide positionSide, int percentage) throws NullPointerException, IllegalArgumentException {
        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("illegal.percentage", percentage));
        }

        ExchangeInfoEntry exchangeInfoEntry = getExchangeInfo(symbol);
        String quantityStr;

        if (percentage == 100) {
            quantityStr = Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                    Filter.Type.MARKET_LOT_SIZE,
                    Filter.Key.MAX_QTY.toString()));
        } else {
            BigDecimal stepSize = new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                    Filter.Type.MARKET_LOT_SIZE,
                    Filter.Key.STEP_SIZE.toString())));

            BigDecimal quantity = Calculations.percentage(getPosition(symbol, positionSide).getPositionAmt(), new BigDecimal(percentage));

            quantity = quantity.divide(stepSize, 0, RoundingMode.FLOOR).multiply(stepSize);

            requireLegalQuantity(quantity, getLastPrice(symbol), exchangeInfoEntry);

            quantityStr = quantity.toString();
        }

        return syncRequestClient.postOrder(
                symbol,
                positionSide.equals(PositionSide.LONG) ? OrderSide.SELL : OrderSide.BUY,
                positionSide,
                OrderType.MARKET,
                null,
                quantityStr,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                NewOrderRespType.ACK);
    }

    public synchronized TP_SL postTP_SLOrders(String symbol, PositionSide positionSide, int takeProfitPercent, int stopLossPercent) {
        OrderSide orderSide = positionSide.equals(PositionSide.LONG) ? OrderSide.SELL : OrderSide.BUY;
        Position position = getPosition(symbol, positionSide);

        TP_SL tp_sl = null;

        if (position != null) {
            tp_sl = new TP_SL(positionSide, new BigDecimal(position.getEntryPrice()), takeProfitPercent, stopLossPercent);

            if (stopLossPercent != 0) {
                tp_sl.setStopLossOrder(syncRequestClient.postOrder(symbol,
                        orderSide,
                        positionSide,
                        OrderType.STOP_MARKET,
                        null,
                        null,
                        null,
                        null,
                        null,
                        tp_sl.getStopLossPrice().toString(),
                        "true",
                        null,
                        null,
                        null,
                        null,
                        NewOrderRespType.ACK));
            }

            if (takeProfitPercent != 0) {
                tp_sl.setTakeProfitOrder(syncRequestClient.postOrder(symbol,
                        orderSide,
                        positionSide,
                        OrderType.TAKE_PROFIT_MARKET,
                        null,
                        null,
                        null,
                        null,
                        null,
                        tp_sl.getTakeProfitPrice().toString(),
                        "true",
                        null,
                        null,
                        null,
                        null,
                        NewOrderRespType.ACK));
            }
        }

        return tp_sl;
    }

    public List<MyTrade> getMyTrades(String symbol, Long orderId) {
        List<MyTrade> myTrades = syncRequestClient.getAccountTrades(symbol, null, null, null, null)
                .stream()
                .filter(myTrade -> myTrade.getOrderId().equals(orderId))
                .collect(Collectors.toList());

        return myTrades.size() > 0 ? myTrades : null;
    }

    public Position getPosition(String symbol, PositionSide positionSide) {
        List<Position> positions = getOpenedPositions();

        positions = positions
                .stream()
                .filter(position -> position.getSymbol().equals(symbol) && position.getPositionSide().equals(positionSide.toString()))
                .collect(Collectors.toList());

        return positions.size() == 1 ? positions.get(0) : null;
    }

    public BigDecimal getAvailableBalance(String asset) {
        List<AccountBalance> accountBalances = syncRequestClient
                .getBalance()
                .stream()
                .filter(accountBalance -> accountBalance.getAsset().equals(asset))
                .collect(Collectors.toList());

        return accountBalances.size() == 1 ? accountBalances.get(0).getMaxWithdrawAmount() : null;
    }

    public ResponseResult cancelOrders(String symbol) {
        return syncRequestClient.cancelAllOpenOrder(symbol);
    }

    public List<Position> getOpenedPositions() {
        return syncRequestClient.getAccountInformation()
                .getPositions()
                .stream()
                .filter(position -> new BigDecimal(position.getEntryPrice()).compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
    }

    public ExchangeInfoEntry getExchangeInfo(String symbol) {
        List<ExchangeInfoEntry> exchangeInfoEntries = syncRequestClient.getExchangeInformation()
                .getSymbols()
                .stream()
                .filter(exchangeInfoEntry -> exchangeInfoEntry.getSymbol().equals(symbol))
                .collect(Collectors.toList());

        return exchangeInfoEntries.size() == 1 ? exchangeInfoEntries.get(0) : null;
    }

    public BigDecimal getLastPrice(String symbol) {
        List<MarkPrice> markPrices = syncRequestClient.getMarkPrice(symbol);

        return markPrices != null && markPrices.size() == 1 ? markPrices.get(0).getMarkPrice() : null;
    }

    public PositionRisk getPositionRisk(List<PositionRisk> positionRisks, String symbol, PositionSide positionSide) {
        positionRisks = positionRisks.stream()
                .filter(positionRisk -> positionRisk.getSymbol().equals(symbol) && positionRisk.getPositionSide().equals(positionSide.toString()))
                .collect(Collectors.toList());

        return positionRisks.size() == 1 ? positionRisks.get(0) : null;
    }

    public static String getExchangeInfoFilterValue(List<List<Map<String, String>>> exchangeInfoEntryFilters,
                                              Filter.Type filterType,
                                              String key) {
        List<Map<String, String>> temp;

        for (List<Map<String, String>> exchangeInfoFilter : exchangeInfoEntryFilters) {
            if (exchangeInfoFilter
                    .stream()
                    .filter(keyValue -> filterType.toString().equals(keyValue.get("filterType")))
                    .count() == 1) {
                temp = exchangeInfoFilter.stream().filter(map -> map.containsKey(key)).collect(Collectors.toList());
                return temp.size() == 1 ? temp.get(0).get(key) : null;
            }
        }

        return null;
    }

    public static void requireLegalQuantity(BigDecimal quantity, BigDecimal lastPrice, ExchangeInfoEntry exchangeInfoEntry) throws NullPointerException, IllegalArgumentException {
        BigDecimal minQuantity = new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                Filter.Type.MARKET_LOT_SIZE,
                Filter.Key.MIN_QTY.toString())));

        BigDecimal maxQuantity = new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                Filter.Type.MARKET_LOT_SIZE,
                Filter.Key.MAX_QTY.toString())));

        BigDecimal minNotional = new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                Filter.Type.MIN_NOTIONAL,
                Filter.Key.NOTIONAL.toString())));

        if (quantity.compareTo(minQuantity) < 0 || quantity.compareTo(maxQuantity) > 0) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("illegal.quantity",
                    quantity.toString(),
                    minQuantity.toString(),
                    maxQuantity.toString()));
        }

        BigDecimal notional = quantity.multiply(lastPrice);

        if (notional.compareTo(minNotional) < 0) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("illegal.notional",
                    notional,
                    minNotional));
        }
    }

    public static String getAssetBySymbol(String symbol) {
        if (symbol.matches(".*BUSD")) {
            return "BUSD";
        } else if (symbol.matches(".*USDT")) {
            return "USDT";
        } else {
            return null;
        }
    }
}
