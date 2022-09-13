package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public final class PIFAGOR_ALTCOINS_SIGNAL extends Signal {
    private final Action action;

    public PIFAGOR_ALTCOINS_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, Action action) {
        super(ticker, exchange, close, interval, instant);
        this.action = action;
    }

    public PIFAGOR_ALTCOINS_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, Action action) {
        super(ticker, exchange, close, interval, time);
        this.action = action;
    }

    public PIFAGOR_ALTCOINS_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        action = Action.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("action")));
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "PIFAGOR_ALTCOINS_SIGNAL{" +
                "action=" + action +
                "} " + super.toString();
    }
}
