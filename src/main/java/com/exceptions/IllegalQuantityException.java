package com.exceptions;

public class IllegalQuantityException extends IllegalArgumentException {
    private static final long serialVersionUID = 4469373141932564968L;

    public IllegalQuantityException() {
    }

    public IllegalQuantityException(String s) {
        super(s);
    }
}
