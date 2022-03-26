package com.personio.api.utils;

public class Constants {

    public static final String STANDARD_RESPONSE_CONTENTTYPE = "application/json";
    public static final String URL_PARAM_TOKEN = "token";
    // -- In real application this should not be constant in source code like this.
    // -- This way of putting secret in constant is strictly only for demo purpose.
    public static final String TOKEN_SECRET = "572247f3042d4add9397333a172680143d7363ace4";
    public static final long TOKEN_TTL_MS = 15 * 60 * 1000L; // 15 min
    public static final String JDBC_H2_MEM = "jdbc:h2:mem:hierarchy";

    private Constants() {
    }
}

