package org.gabsferreira.exception;

public class SenhaInvalidaException extends RuntimeException {

    public SenhaInvalidaException() {
        super("Senha incorreta!");
    }
}
