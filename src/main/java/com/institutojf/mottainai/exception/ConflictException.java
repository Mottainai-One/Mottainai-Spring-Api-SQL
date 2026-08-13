package com.institutojf.mottainai.exception;

/**
 * Usada quando os dados enviados entram em conflito com dados já salvos
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
