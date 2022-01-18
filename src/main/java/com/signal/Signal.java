package com.signal;

import com.utils.I18nSupport;
import org.json.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

public abstract class Signal {
    private final String ticket;
    private final String exchange;
    private final BigDecimal close;
    private final Integer interval;
    private final Date time;

    public Signal(String ticket, String exchange, BigDecimal close, Integer interval, String instant) {
        this.ticket = ticket;
        this.exchange = exchange;
        this.close = close;
        this.interval = interval;
        this.time = Date.from(Instant.parse(instant));
    }

    public Signal(String ticket, String exchange, BigDecimal close, Integer interval, Date time) {
        this.ticket = ticket;
        this.exchange = exchange;
        this.close = close;
        this.interval = interval;
        this.time = time;
    }

    public Signal(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        ticket = jsonObject.getString(I18nSupport.i18n_literals("ticket"));
        exchange = jsonObject.getString(I18nSupport.i18n_literals("exchange"));
        close = jsonObject.getBigDecimal(I18nSupport.i18n_literals("close"));
        interval = jsonObject.getInt(I18nSupport.i18n_literals("interval"));
        time = Date.from(Instant.parse(jsonObject.getString(I18nSupport.i18n_literals("instant"))));
    }

    public String getTicket() {
        return ticket;
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

    public static Class<?> getSignalClass(JSONObject jsonObject) throws JSONException {
        String indicatorType = jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("type"));

        if (indicatorType.equals(PIFAGOR_KHALIFA_Signal.TYPE)) {
            return PIFAGOR_KHALIFA_Signal.class;
        } else if (indicatorType.equals(PIFAGOR_MFI_Signal.TYPE)) {
            return PIFAGOR_MFI_Signal.class;
        } else if (indicatorType.equals(PIFAGOR_ALTCOINS_SIGNAL.TYPE)) {
            return PIFAGOR_ALTCOINS_SIGNAL.class;
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }
    }
}