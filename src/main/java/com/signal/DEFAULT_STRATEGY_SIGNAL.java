package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class DEFAULT_STRATEGY_SIGNAL extends Signal {
    private final String comment;

    public DEFAULT_STRATEGY_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, String comment) {
        super(ticker, exchange, close, interval, instant);
        this.comment = comment;
    }

    public DEFAULT_STRATEGY_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, String comment) {
        super(ticker, exchange, close, interval, time);
        this.comment = comment;
    }

    public DEFAULT_STRATEGY_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        comment = jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("comment"));
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "DEFAULT_STRATEGY_SIGNAL{" +
                "comment='" + comment + '\'' +
                "} " + super.toString();
    }
}
