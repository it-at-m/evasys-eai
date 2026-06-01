package de.muenchen.evasys.exception;

@SuppressWarnings("serial")
public class EvasysException extends RuntimeException {

    public EvasysException(final String message) {
        super(message);
    }

    public EvasysException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
