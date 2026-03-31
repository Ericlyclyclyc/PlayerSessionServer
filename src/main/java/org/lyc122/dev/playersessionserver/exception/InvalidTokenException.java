package org.lyc122.dev.playersessionserver.exception;

public class InvalidTokenException extends BusinessException {
    
    public InvalidTokenException(String message) {
        super(401, message);
    }
}