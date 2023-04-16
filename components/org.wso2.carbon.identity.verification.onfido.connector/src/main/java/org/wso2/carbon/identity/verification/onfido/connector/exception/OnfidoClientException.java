package org.wso2.carbon.identity.verification.onfido.connector.exception;

public class OnfidoClientException extends OnfidoException {

    public OnfidoClientException(String errorCode, String message) {

        super(errorCode, message);
    }

    public OnfidoClientException(String errorCode, String message, Throwable throwable) {

        super(errorCode, message, throwable);
    }

}
