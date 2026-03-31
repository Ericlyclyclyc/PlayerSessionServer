package org.lyc122.dev.playersessionserver.exception;

public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(403, message);
    }
}