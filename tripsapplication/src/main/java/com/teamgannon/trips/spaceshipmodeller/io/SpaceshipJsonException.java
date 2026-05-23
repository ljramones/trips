package com.teamgannon.trips.spaceshipmodeller.io;

/**
 * Unchecked exception raised when a spaceship design cannot be read from or written to JSON.
 */
public class SpaceshipJsonException extends RuntimeException {

    public SpaceshipJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpaceshipJsonException(String message) {
        super(message);
    }
}
