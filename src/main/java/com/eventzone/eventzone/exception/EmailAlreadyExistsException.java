package com.eventzone.eventzone.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String mensaje) {
        super(mensaje);
    }
}
