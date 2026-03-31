package org.lyc122.dev.playersessionserver.exception;

public class PlayerAlreadyExistsException extends BusinessException {
    
    public PlayerAlreadyExistsException(String message) {
        super(409, message);
    }
}