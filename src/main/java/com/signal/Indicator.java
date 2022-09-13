package com.signal;

import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Indicator {
    PIFAGOR_STRONG_LONG_ALARM("СТРОНГ ЛОНГ",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDCC8"),
    PIFAGOR_STRONG_SHORT_ALARM("СТРОНГ ШОРТ",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDCC9"),
    PIFAGOR_GREEN_DOT_ALARM("ЗЕЛЁНАЯ ТОЧКА",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDFE2"),
    PIFAGOR_RED_DOT_ALARM("КРАСНАЯ ТОЧКА",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDD34"),
    PIFAGOR_WARM_BUY_ALARM("WARM BUY",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83C\uDF89"),
    PIFAGOR_WARM_STRONG_BUY_ALARM("WARM STRONG BUY",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDD25"),
    PIFAGOR_MFI_BUY_ALARM("MFI BUY",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "⚪"),
    PIFAGOR_MFI_STRONG_BUY_ALARM("MFI STRONG BUY",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDFE2"),
    PIFAGOR_BIG_GUY_SELL_ALARM("BIG GUY SELL",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDD3B"),
    PIFAGOR_BIG_GUY_BIG_PUMP_ALARM("BIG GUY BIG PUMP",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDD36"),
    PIFAGOR_BIG_GUY_SMALL_PUMP_ALARM("BIG GUY SMALL PUMP",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "\uD83D\uDD38"),
    PIFAGOR_KHALIFA_1_ALARM("BIG GUY KHALIFA 1 FLOOR",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "1️⃣"),
    PIFAGOR_KHALIFA_2_ALARM("BIG GUY KHALIFA 2 FLOOR",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "2️⃣"),
    PIFAGOR_KHALIFA_3_ALARM("BIG GUY KHALIFA 3 FLOOR",
            Stream.of(ALARM_SIGNAL.class).collect(Collectors.toSet()), "3️⃣"),
    PIFAGOR_ALTS_3_5_ALARM("PIFAGOR ALTS 3.5",
            Stream.of(STRATEGY_ALARM_SIGNAL.class).collect(Collectors.toSet())),
    PIFAGOR_KHALIFA(
            Stream.of(PIFAGOR_KHALIFA_SIGNAL.class).collect(Collectors.toSet())),
    PIFAGOR_ALTCOINS(
            Stream.of(PIFAGOR_ALTCOINS_SIGNAL.class).collect(Collectors.toSet())),
    PIFAGOR_MFI(
            Stream.of(PIFAGOR_MFI_SIGNAL.class).collect(Collectors.toSet())),
    PIFAGOR_GLOBAL(
            Stream.of(PIFAGOR_GLOBAL_SIGNAL.class).collect(Collectors.toSet())),
    FMA(
            Stream.of(FMA_SIGNAL.class).collect(Collectors.toSet())),
    ADX_DI(
            Stream.of(ADX_DI_SIGNAL.class).collect(Collectors.toSet())),
    DEFAULT_STRATEGY(
            Stream.of(DEFAULT_STRATEGY_SIGNAL.class).collect(Collectors.toSet()));

    private final Set<Class<?>> signals;
    private final String alias;
    private final String emoji;

    Indicator(Set<Class<?>> signals) {
        this.signals = signals;
        alias = toString();
        emoji = "";
    }

    Indicator(String alias, Set<Class<?>> signals) {
        this.signals = signals;
        this.alias = alias;
        this.emoji = "";
    }

    Indicator(String alias, Set<Class<?>> signals, String emoji) {
        this.signals = signals;
        this.alias = alias;
        this.emoji = emoji;
    }

    public Set<Class<?>> getSignals() {
        return signals;
    }

    public String alias() {
        return alias;
    }

    public String getEmoji() {
        return emoji;
    }

    public boolean isStrategy() {
        return this == PIFAGOR_ALTS_3_5_ALARM;
    }

    public static Indicator from(JSONObject jsonObject) throws JSONException {
        return Indicator.valueOf(jsonObject.getJSONObject(I18nSupport.i18n_literals("indicator")).getString(I18nSupport.i18n_literals("type")));
    }
}
