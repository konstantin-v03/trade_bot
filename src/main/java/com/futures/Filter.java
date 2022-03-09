package com.futures;

public class Filter {
    public enum Type {
        LOT_SIZE, PRICE_FILTER, MARKET_LOT_SIZE, MAX_NUM_ORDERS, MAX_NUM_ALGO_ORDERS, MIN_NOTIONAL, PERCENT_PRICE
    }

    public enum Key {
        STEP_SIZE("stepSize"),
        MIN_QTY("minQty"),
        MAX_QTY("maxQty"),
        NOTIONAL("notional");

        private final String string;

        Key(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
