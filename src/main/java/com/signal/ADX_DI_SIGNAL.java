package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

public class ADX_DI_SIGNAL extends Signal {
    public enum Background {
        GREEN("\uD83D\uDFE2"),
        RED("\uD83D\uDD34");

        private final String emoji;

        Background(String emoji) {
            this.emoji = emoji;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    private final Background background;

    public ADX_DI_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, String instant, Background background) {
        super(ticker, exchange, close, interval, instant);
        this.background = background;
    }

    public ADX_DI_SIGNAL(String ticker, String exchange, BigDecimal close, Integer interval, Date time, Background background) {
        super(ticker, exchange, close, interval, time);
        this.background = background;
    }

    public ADX_DI_SIGNAL(JSONObject jsonObject) throws JSONException, IllegalArgumentException {
        super(jsonObject);
        background = Background.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("background")));
    }

    public Background getBackground() {
        return background;
    }

    @Override
    public String toString() {
        return "ADX_DI_SIGNAL{" +
                "background=" + background +
                "} " + super.toString();
    }

}
