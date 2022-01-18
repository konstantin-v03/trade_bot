package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class PIFAGOR_KHALIFA_Signal extends Signal {
    public static final String TYPE = I18nSupport.i18n_literals("pifagor.khalifa");

    private final int floor;

    public PIFAGOR_KHALIFA_Signal(String ticket, String exchange, BigDecimal close, Integer interval, String instant, int floor) {
        super(ticket, exchange, close, interval, instant);
        this.floor = floor;
    }

    public PIFAGOR_KHALIFA_Signal(String ticket, String exchange, BigDecimal close, Integer interval, Date time, int floor) {
        super(ticket, exchange, close, interval, time);
        this.floor = floor;
    }

    public PIFAGOR_KHALIFA_Signal(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        floor = jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getInt(I18nSupport.i18n_literals("floor"));
    }

    public int getFloor() {
        return floor;
    }
}
