package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class STRATEGY_ALARM_SIGNAL extends Signal {
    public enum Action {
        BUY("ПОКУПКА"),
        SELL("ПРОДАЖА");

        private final String alias;

        Action(String alias) {
            this.alias = alias;
        }

        public String alias() {
            return alias;
        }
    }

    private final Indicator indicator;
    private final Action action;

    public STRATEGY_ALARM_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, Indicator indicator, Action action) {
        super(ticker, exchange, close, interval, instant);
        this.indicator = indicator;
        this.action = action;
    }

    public STRATEGY_ALARM_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, Indicator indicator, Action action) {
        super(ticker, exchange, close, interval, time);
        this.indicator = indicator;
        this.action = action;
    }

    public STRATEGY_ALARM_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        indicator = Indicator.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator"))
                .getString(I18nSupport.i18n_literals("type")).toUpperCase());
        action = Action.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator"))
                .getString(I18nSupport.i18n_literals("action")).toUpperCase());
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "STRATEGY_ALARM{" +
                "action=" + action +
                "} " + super.toString();
    }
}
