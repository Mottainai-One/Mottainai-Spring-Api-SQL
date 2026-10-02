package com.institutojf.mottainai.exception;

public class CustomerRecoveryDeliveryException extends RuntimeException {

    public CustomerRecoveryDeliveryException(Throwable cause) {
        super("The recovery email could not be delivered. Try again later.", cause);
    }

}
