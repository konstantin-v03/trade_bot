package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class PIFAGOR_SEVEN_DAYS_SIGNAL extends Signal {
    @Override
    public String toString() {
        return "PIFAGOR_SEVEN_DAYS_SIGNAL{" +
                "background=" + background +
                "} " + super.toString();
    }

    public enum Background {
        GREEN, RED
    }

    private final Background background;

    public PIFAGOR_SEVEN_DAYS_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, Background background) {
        super(ticker, exchange, close, interval, instant);
        this.background = background;
    }

    public PIFAGOR_SEVEN_DAYS_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, Background background) {
        super(ticker, exchange, close, interval, time);
        this.background = background;
    }

    public PIFAGOR_SEVEN_DAYS_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        background = Background.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("background")));
    }

    public Background getBackground() {
        return background;
    }

}
