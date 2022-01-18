package com.futures.dualside;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.NewOrderRespType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.market.ExchangeInfoEntry;
import com.binance.client.model.market.PriceChangeTicker;
import com.binance.client.model.trade.*;
import com.futures.Amount;
import com.futures.FilterType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RequestSender {
    private final SyncRequestClient syncRequestClient;

    public RequestSender(SyncRequestClient syncRequestClient) {
        this.syncRequestClient = syncRequestClient;

        if (!Boolean.parseBoolean(syncRequestClient.getPositionSide().getString("dualSidePosition"))) {
            syncRequestClient.changePositionSide(true);
        }
    }

    public boolean openLongPositionMarket(String symbol, MarginType marginType, Amount amount, int leverage) throws NullPointerException {
        return openPositionMarket(symbol, OrderSide.BUY, marginType, PositionSide.LONG, amount, leverage);
    }

    public boolean openShortPositionMarket(String symbol, MarginType marginType, Amount amount, int leverage) throws NullPointerException {
        return openPositionMarket(symbol, OrderSide.SELL, marginType, PositionSide.SHORT, amount, leverage);
    }

    public Long closeLongPositionMarket(String symbol) {
        return closePositionMarket(symbol, PositionSide.LONG);
    }

    public Long closeShortPositionMarket(String symbol) {
        return closePositionMarket(symbol, PositionSide.SHORT);
    }

    public MyTrade getMyTrade(String symbol, Long orderId) {
        List<MyTrade> myTrades = syncRequestClient.getAccountTrades(symbol, null, null, null, null)
                .stream()
                .filter(myTrade -> myTrade.getOrderId().equals(orderId))
                .collect(Collectors.toList());

        return myTrades.size() > 0 ? myTrades.get(0) : null;
    }

    public Position getPosition(String symbol, PositionSide positionSide) {
        List<Position> positions = getOpenedPositions();

        positions = positions
                .stream()
                .filter(position -> position.getSymbol().equals(symbol) && position.getPositionSide().equals(positionSide.toString()))
                .collect(Collectors.toList());

        return positions.size() == 1 ? positions.get(0) : null;
    }

    public synchronized Long closePositionMarket(String symbol, PositionSide positionSide) {
        OrderSide orderSide = positionSide.equals(PositionSide.LONG) ? OrderSide.SELL : OrderSide.BUY;
        PositionRisk positionRisk = getPositionRisk(syncRequestClient.getPositionRisk(), symbol, positionSide);

        Order order = null;

        if (positionRisk != null && positionRisk.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
            order = syncRequestClient.postOrder(symbol, orderSide, positionSide, OrderType.MARKET, null,
                    String.valueOf(Math.abs(positionRisk.getPositionAmt().longValue())), null, null, null, null, null, NewOrderRespType.ACK);
        }

        return order != null ? order.getOrderId() : null;
    }

    public synchronized boolean openPositionMarket(String symbol, OrderSide orderSide, MarginType marginType, PositionSide positionSide, Amount amount, int leverage){
        ExchangeInfoEntry exchangeInfoEntry = getExchangeInfo(symbol);

        BigDecimal amountUSD = null;

        if (amount.getType().equals(Amount.TYPE.PERCENT)) {
            amountUSD = Amount.getAmountUSD(amount.getAmount(), Objects.requireNonNull(getAvailableBalance(getAssetBySymbol(symbol))));
        } else if (amount.getType().equals(Amount.TYPE.USD)){
            amountUSD = amount.getAmount();
        }

        BigDecimal quantity = Objects.requireNonNull(amountUSD).multiply(new BigDecimal(leverage)).divide(getLastPrice(symbol), new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(Objects.requireNonNull(exchangeInfoEntry).getFilters(),
                FilterType.MARKET_LOT_SIZE,
                "stepSize"))).scale(), RoundingMode.FLOOR);

        List<PositionRisk> positionRisks = syncRequestClient.getPositionRisk();
        PositionRisk positionRisk = getPositionRisk(positionRisks, symbol, positionSide);

        if (positionRisk != null && quantity.compareTo(new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                FilterType.MARKET_LOT_SIZE,
                "minQty")))) >= 0 &&
                quantity.compareTo(new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                        FilterType.MARKET_LOT_SIZE,
                        "maxQty")))) <= 0 &&
                quantity.multiply(getLastPrice(symbol)).compareTo(new BigDecimal(Objects.requireNonNull(getExchangeInfoFilterValue(exchangeInfoEntry.getFilters(),
                        FilterType.MIN_NOTIONAL,
                        "notional")))) >= 0) {

            if (positionRisk.getLeverage().compareTo(new BigDecimal(leverage)) != 0) {
                syncRequestClient.changeInitialLeverage(symbol, leverage);
            }

            if ((positionRisk.getMarginType().equals("cross") && marginType.isIsolated()) ||
                    (positionRisk.getMarginType().equals("isolated") && marginType.isCrossed()))  {
                syncRequestClient.changeMarginType(symbol, marginType.toString());
            }

            syncRequestClient.postOrder(symbol, orderSide, positionSide, OrderType.MARKET, null,
                    quantity.toString(), null, null, null, null, null, NewOrderRespType.ACK);

            return true;
        }

        return false;
    }

    private BigDecimal getAvailableBalance(String asset) {
        List<AccountBalance> accountBalances = syncRequestClient
                .getBalance()
                .stream()
                .filter(accountBalance -> accountBalance.getAsset().equals(asset))
                .collect(Collectors.toList());

        return accountBalances.size() == 1 ? accountBalances.get(0).getWithdrawAvailable() : null;
    }

    private BigDecimal getBalance(String asset) {
        List<AccountBalance> accountBalances = syncRequestClient
                .getBalance()
                .stream()
                .filter(accountBalance -> accountBalance.getAsset().equals(asset))
                .collect(Collectors.toList());

        return accountBalances.size() == 1 ? accountBalances.get(0).getBalance() : null;
    }

    private String getAssetBySymbol(String symbol) {
        if (symbol.matches(".*BUSD")) {
            return "BUSD";
        } else if (symbol.matches(".*USDT")) {
            return "USDT";
        } else {
            return null;
        }
    }

    private List<Position> getOpenedPositions() {
        return syncRequestClient.getAccountInformation()
                .getPositions()
                .stream()
                .filter(position -> new BigDecimal(position.getEntryPrice()).compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
    }

    private ExchangeInfoEntry getExchangeInfo(String symbol) {
        List<ExchangeInfoEntry> exchangeInfoEntries = syncRequestClient.getExchangeInformation()
                .getSymbols()
                .stream()
                .filter(exchangeInfoEntry -> exchangeInfoEntry.getSymbol().equals(symbol))
                .collect(Collectors.toList());

        return exchangeInfoEntries.size() == 1 ? exchangeInfoEntries.get(0) : null;
    }

    private BigDecimal getLastPrice(String symbol) {
        List<PriceChangeTicker> priceChangeTickers = syncRequestClient.get24hrTickerPriceChange(symbol);

        return priceChangeTickers != null && priceChangeTickers.size() == 1 ? priceChangeTickers.get(0).getLastPrice() : null;
    }

    private PositionRisk getPositionRisk(List<PositionRisk> positionRisks, String symbol, PositionSide positionSide) {
        positionRisks = positionRisks.stream()
                .filter(positionRisk -> positionRisk.getSymbol().equals(symbol) && positionRisk.getPositionSide().equals(positionSide.toString()))
                .collect(Collectors.toList());

        return positionRisks.size() == 1 ? positionRisks.get(0) : null;
    }

    private String getExchangeInfoFilterValue(List<List<Map<String, String>>> exchangeInfoEntryFilters,
                                              FilterType filterType,
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
}
