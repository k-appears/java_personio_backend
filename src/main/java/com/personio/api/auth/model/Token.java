package com.personio.api.auth.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Token implements Serializable {

    @Expose
    private String username;
    @Expose
    private long timestamp;

    public Token(String username, long timestamp) {
        super();
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
