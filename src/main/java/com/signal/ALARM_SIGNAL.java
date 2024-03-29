package com.signal;

import com.utils.I18nSupport;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class ALARM_SIGNAL extends Signal {
    public enum Option {
        ONCE_PER_MINUTE, ONCE_PER_BAR_CLOSE
    }

    private final Indicator indicator;
    private final Option option;

    public ALARM_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, Indicator indicator, Option option) {
        super(ticker, exchange, close, interval, instant);
        this.indicator = indicator;
        this.option = option;
    }

    public ALARM_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, Indicator indicator, Option option) {
        super(ticker, exchange, close, interval, time);
        this.indicator = indicator;
        this.option = option;
    }

    public ALARM_SIGNAL(JSONObject jsonObject) {
        super(jsonObject);
        indicator = Indicator.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator"))
                .getString(I18nSupport.i18n_literals("type")));
        option = Option.valueOf(jsonObject.getJSONObject("indicator")
                .getString(I18nSupport.i18n_literals("option")));
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public Option getOption() {
        return option;
    }

    @Override
    public String toString() {
        return "ALARM_SIGNAL{" +
                "indicator=" + indicator +
                '}';
    }
}
