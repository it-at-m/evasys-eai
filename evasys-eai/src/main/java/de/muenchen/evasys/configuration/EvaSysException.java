package de.muenchen.evasys.configuration;

@SuppressWarnings("serial")
public class EvaSysException extends RuntimeException {

    public EvaSysException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
