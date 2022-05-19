package com.signal;

import com.utils.I18nSupport;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class ALARM_SIGNAL extends Signal {
    public enum Indicator {
        PIFAGOR_STRONG_LONG_ALARM("СТРОНГ ЛОНГ"),
        PIFAGOR_STRONG_SHORT_ALARM("СТРОНГ ШОРТ"),
        PIFAGOR_GREEN_DOT_ALARM("ЗЕЛЁНАЯ ТОЧКА"),
        PIFAGOR_RED_DOT_ALARM("КРАСНАЯ ТОЧКА"),
        PIFAGOR_WARM_BUY_ALARM("WARM BUY"),
        PIFAGOR_WARM_STRONG_BUY_ALARM("WARM STRONG BUY"),
        PIFAGOR_MFI_BUY("MFI BUY"),
        PIFAGOR_MFI_STRONG_BUY("MFI STRONG BUY"),
        PIFAGOR_BIG_GUY_SELL("BIG GUY SELL"),
        PIFAGOR_BIG_GUY_BIG_PUMP("BIG GUY BIG PUMP"),
        PIFAGOR_BIG_GUY_SMALL_PUMP("BIG GUY SMALL PUMP"),
        PIFAGOR_KHALIFA_1("BIG GUY KHALIFA 1 FLOOR"),
        PIFAGOR_KHALIFA_2("BIG GUY KHALIFA 2 FLOOR"),
        PIFAGOR_KHALIFA_3("BIG GUY KHALIFA 3 FLOOR");

        private final String alias;

        Indicator(String alias) {
            this.alias = alias;
        }

        public String alias() {
            return alias;
        }
    }

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
