package org.lyc122.dev.playersessionserver.exception;

public class PlayerConflictException extends BusinessException {
    
    public PlayerConflictException(String message) {
        super(409, message);
    }
}