package com.strategies;

import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.PIFAGOR_KHALIFA_Signal;
import com.signal.PIFAGOR_MFI_Signal;
import com.signal.Signal;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

public class MFI_BigGuyHandler extends StrategyHandler {
    public MFI_BigGuyHandler(RequestSender requestSender) {
        super(requestSender);
    }

    @Override
    public void process() {
        try {
            JSONObject jsonObject = new JSONObject(inputRequest);

            if (Signal.getSignalClass(jsonObject) == PIFAGOR_MFI_Signal.class) {
                PIFAGOR_MFI_Signal pifagorMfiSignal = new PIFAGOR_MFI_Signal(jsonObject);
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.signal", pifagorMfiSignal.getAction().toString()));
            } else if (Signal.getSignalClass(jsonObject) == PIFAGOR_KHALIFA_Signal.class) {
                PIFAGOR_KHALIFA_Signal pifagorKhalifaSignal = new PIFAGOR_KHALIFA_Signal(jsonObject);
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.khalifa.signal.floor", pifagorKhalifaSignal.getFloor()));
            } else {
                throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
            }
        } catch (JSONException|IllegalArgumentException exception) {
            TradeLogger.logException(exception);
        }
    }
}
