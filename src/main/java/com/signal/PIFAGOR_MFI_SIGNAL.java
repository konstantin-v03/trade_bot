package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public final class PIFAGOR_MFI_SIGNAL extends Signal {
    public enum MFI_Action {
        BUY, STRONG_BUY
    }

    private final MFI_Action mfiAction;

    public PIFAGOR_MFI_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, MFI_Action mfiAction) {
        super(ticker, exchange, close, interval, instant);
        this.mfiAction = mfiAction;
    }

    public PIFAGOR_MFI_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, MFI_Action mfiAction) {
        super(ticker, exchange, close, interval, time);
        this.mfiAction = mfiAction;
    }

    public PIFAGOR_MFI_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        mfiAction = PIFAGOR_MFI_SIGNAL.MFI_Action.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("mfi_action")));
    }

    public PIFAGOR_MFI_SIGNAL.MFI_Action getAction() {
        return mfiAction;
    }

    @Override
    public String toString() {
        return "PIFAGOR_MFI_SIGNAL{" +
                "mfiAction=" + mfiAction +
                "} " + super.toString();
    }
}
