package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public final class PIFAGOR_KHALIFA_SIGNAL extends Signal {
    public static final String TYPE = I18nSupport.i18n_literals("pifagor.khalifa");

    private final int floor;

    public PIFAGOR_KHALIFA_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, int floor) {
        super(ticker, exchange, close, interval, instant);
        this.floor = floor;
    }

    public PIFAGOR_KHALIFA_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, int floor) {
        super(ticker, exchange, close, interval, time);
        this.floor = floor;
    }

    public PIFAGOR_KHALIFA_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        floor = jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getInt(I18nSupport.i18n_literals("floor"));
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public String toString() {
        return "PIFAGOR_KHALIFA_Signal{" +
                "floor=" + floor +
                "} " + super.toString();
    }
}
