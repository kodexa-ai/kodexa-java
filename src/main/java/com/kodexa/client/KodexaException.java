package com.kodexa.client;

/**
 * An exception that is thrown if issues occur within the Kodexa client
 */
public class KodexaException extends RuntimeException {

    /**
     * Create a new exception with the provided message and cause
     *
     * @param message
     * @param cause
     */
    public KodexaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create an exception with a message and no cause
     *
     * @param message
     */
    public KodexaException(String message) {
        super(message);
    }

}
