package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class PIFAGOR_ALTCOINS_SIGNAL extends Signal {
    public static final String TYPE = I18nSupport.i18n_literals("pifagor.altcoins");

    public enum Action {
        BUY, SELL
    }

    private final PIFAGOR_ALTCOINS_SIGNAL.Action action;

    public PIFAGOR_ALTCOINS_SIGNAL(String ticket, String exchange, BigDecimal close, Integer interval, String instant, PIFAGOR_ALTCOINS_SIGNAL.Action action) {
        super(ticket, exchange, close, interval, instant);
        this.action = action;
    }

    public PIFAGOR_ALTCOINS_SIGNAL(String ticket, String exchange, BigDecimal close, Integer interval, Date time, PIFAGOR_ALTCOINS_SIGNAL.Action action) {
        super(ticket, exchange, close, interval, time);
        this.action = action;
    }

    public PIFAGOR_ALTCOINS_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        action = PIFAGOR_ALTCOINS_SIGNAL.Action.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("action")));
    }

    public PIFAGOR_ALTCOINS_SIGNAL.Action getAction() {
        return action;
    }
}
