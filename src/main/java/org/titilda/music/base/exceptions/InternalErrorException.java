package org.titilda.music.base.exceptions;

public class InternalErrorException extends Exception {
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
