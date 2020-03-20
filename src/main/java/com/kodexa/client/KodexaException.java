package com.kodexa.client;

public class KodexaException extends RuntimeException {

    public KodexaException(String message, Throwable cause) {
        super(message, cause);
    }

    public KodexaException(String message) {
        super(message);
    }

}
