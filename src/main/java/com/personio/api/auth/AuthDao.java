package com.personio.api.auth;

public class AuthDao {

    private AuthDao() {
    }

    public static boolean isValid(String username, String password) {
        // Dynamically storing credentials in database username and password is out of scope
        return username.equals("test") && password.equals("test");
    }
}
