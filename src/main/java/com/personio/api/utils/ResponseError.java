package com.personio.api.utils;

import com.google.gson.annotations.Expose;

public class ResponseError {

    @Expose
    private final String message;

    public ResponseError(Exception e) {
        this.message = e.getMessage();
    }

    public String getMessage() {
        return message;
    }


}
