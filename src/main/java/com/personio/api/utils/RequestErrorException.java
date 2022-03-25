package com.personio.api.utils;

public class RequestErrorException extends RuntimeException {
    private final Integer statusCode;
    private final String message;

    public RequestErrorException(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
