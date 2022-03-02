package com.strategies;

public enum Strategy {
    MFI_BIG_GUY, ALTCOINS;

    public Strategy valueOf(Class<?> strategyClass) {
        if (strategyClass.equals(MFI_BigGuyHandler.class)) {
            return MFI_BIG_GUY;
        } else if (strategyClass.equals(AltcoinsHandler.class)) {
            return ALTCOINS;
        }

        return null;
    }
}
