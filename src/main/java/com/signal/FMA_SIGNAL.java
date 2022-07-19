package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class FMA_SIGNAL extends Signal {
    public enum SMA_COLOR {
        GREEN, YELLOW, BLACK, ORANGE, RED
    }

    private final SMA_COLOR smaColor;

    public FMA_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, SMA_COLOR smaColor) {
        super(ticker, exchange, close, interval, instant);
        this.smaColor = smaColor;
    }

    public FMA_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, SMA_COLOR smaColor) {
        super(ticker, exchange, close, interval, time);
        this.smaColor = smaColor;
    }

    public FMA_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        smaColor = SMA_COLOR.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("sma_color")));
    }

    public SMA_COLOR getSmaColor() {
        return smaColor;
    }
}
