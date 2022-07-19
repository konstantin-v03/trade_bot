package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class PIFAGOR_GLOBAL_SIGNAL extends Signal {
    public enum Global_Action {
        STRONG_LONG, STRONG_SHORT, GREEN_DOT, RED_DOT
    }

    private final Global_Action globalAction;

    public PIFAGOR_GLOBAL_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, Global_Action globalAction) {
        super(ticker, exchange, close, interval, instant);
        this.globalAction = globalAction;
    }

    public PIFAGOR_GLOBAL_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, Global_Action globalAction) {
        super(ticker, exchange, close, interval, time);
        this.globalAction = globalAction;
    }

    public PIFAGOR_GLOBAL_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        globalAction = Global_Action.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("global_action")));
    }

    public Global_Action getGlobalAction() {
        return globalAction;
    }

    @Override
    public String toString() {
        return "PIFAGOR_GLOBAL_SIGNAL{" +
                "globalAction=" + globalAction +
                "} " + super.toString();
    }
}
