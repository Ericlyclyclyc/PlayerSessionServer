package org.lyc122.dev.playersessionserver.exception;

public class InvalidCredentialsException extends BusinessException {
    
    public InvalidCredentialsException(String message) {
        super(401, message);
    }
}