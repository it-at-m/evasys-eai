package de.muenchen.evasys.exception;

@SuppressWarnings("serial")
public class EvaSysException extends RuntimeException {

    public EvaSysException(final String message) {
        super(message);
    }

    public EvaSysException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
