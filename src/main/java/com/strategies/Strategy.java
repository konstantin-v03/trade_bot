package com.strategies;

public enum Strategy {
    MFI_BIG_GUY;

    public Strategy valueOf(Class<?> strategyClass) {
        if (strategyClass.equals(MFI_BigGuyHandler.class)) {
            return MFI_BIG_GUY;
        }

        return null;
    }
}
