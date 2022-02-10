package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public final class PIFAGOR_MFI_Signal extends Signal {
    public static final String TYPE = I18nSupport.i18n_literals("pifagor.mfi");

    public enum Action {
        BUY, STRONG_BUY
    }

    private final Action action;

    public PIFAGOR_MFI_Signal(String ticker, String exchange, BigDecimal close, Integer interval, String instant, PIFAGOR_MFI_Signal.Action action) {
        super(ticker, exchange, close, interval, instant);
        this.action = action;
    }

    public PIFAGOR_MFI_Signal(String ticker, String exchange, BigDecimal close, Integer interval, Date time, PIFAGOR_MFI_Signal.Action action) {
        super(ticker, exchange, close, interval, time);
        this.action = action;
    }

    public PIFAGOR_MFI_Signal(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        action = PIFAGOR_MFI_Signal.Action.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("action")));
    }

    public PIFAGOR_MFI_Signal.Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "PIFAGOR_MFI_Signal{" +
                "action=" + action +
                "} " + super.toString();
    }
}
