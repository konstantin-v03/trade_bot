package com.signal;

import com.utils.I18nSupport;
import com.utils.Utils;
import org.json.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

public abstract class Signal {
    private final String ticker;
    private final String exchange;
    private final BigDecimal close;
    private final Integer interval;
    private final Date time;

    public Signal(String ticker, String exchange, BigDecimal close, Integer interval, String instant) {
        this.ticker = ticker;
        this.exchange = exchange;
        this.close = close;
        this.interval = interval;
        this.time = Date.from(Instant.parse(instant));
    }

    public Signal(String ticker, String exchange, BigDecimal close, Integer interval, Date time) {
        this.ticker = ticker;
        this.exchange = exchange;
        this.close = close;
        this.interval = interval;
        this.time = time;
    }

    public Signal(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        ticker = jsonObject.getString(I18nSupport.i18n_literals("ticker"));
        exchange = jsonObject.getString(I18nSupport.i18n_literals("exchange"));
        close = jsonObject.getBigDecimal(I18nSupport.i18n_literals("close"));
        interval = Utils.intervalToInt(jsonObject.getString(I18nSupport.i18n_literals("interval")));
        time = Date.from(Instant.parse(jsonObject.getString(I18nSupport.i18n_literals("instant"))));
    }

    public String getTicker() {
        return ticker;
    }

    public String getExchange() {
        return exchange;
    }

    public BigDecimal getClose() {
        return close;
    }

    public Integer getInterval() {
        return interval;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Signal{" +
                "ticker='" + ticker + '\'' +
                ", exchange='" + exchange + '\'' +
                ", close=" + close +
                ", interval=" + interval +
                ", time=" + time +
                ", candlestick index=" + Utils.getCandlestickIndex(time, interval) +
                '}';
    }
}
