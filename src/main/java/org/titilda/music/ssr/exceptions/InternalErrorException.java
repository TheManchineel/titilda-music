package org.titilda.music.ssr.exceptions;

public final class InternalErrorException extends Exception {
    private final String redirect;

    public InternalErrorException(String message) {
        super(message);
        this.redirect = null;
    }
    public InternalErrorException(String message, String redirect) {
        super(message);
        this.redirect = redirect;
    }

    public String getRedirect() {
        return redirect;
    }
}
