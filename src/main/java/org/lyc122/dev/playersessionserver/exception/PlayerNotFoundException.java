package org.lyc122.dev.playersessionserver.exception;

public class PlayerNotFoundException extends BusinessException {
    
    public PlayerNotFoundException(String message) {
        super(404, message);
    }
}