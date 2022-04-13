package com.personio.api.auth;

import com.google.gson.JsonSyntaxException;
import com.personio.api.auth.model.UserLogin;
import com.personio.api.utils.Constants;
import com.personio.api.utils.JsonUtil;
import com.personio.api.utils.RequestErrorException;
import org.eclipse.jetty.http.HttpStatus;
import org.jose4j.lang.JoseException;
import spark.Request;
import spark.Response;
import spark.Route;

public class AuthController {

    public static final Route login = (Request request, Response response) -> {
        validateContentType(request);

        String payload = request.body();
        UserLogin userlogin;
        try {
            userlogin = JsonUtil.fromStringToObject(payload, UserLogin.class);
        } catch (JsonSyntaxException e) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, e.getMessage());
        }
        if (userlogin == null || userlogin.getUsername() == null || userlogin.getUsername().isEmpty()
                || userlogin.getPassword() == null || userlogin.getPassword().isEmpty()) {
            throw new RequestErrorException(HttpStatus.UNAUTHORIZED_401, "Required username and password in body");
        }
        return AuthService.login(userlogin.getUsername(), userlogin.getPassword());
    };

    private AuthController() {
    }

    public static void validateContentType(Request req) {
        if (req.contentType() == null || !req.contentType().toLowerCase().contains("application/json")) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, "Invalid content type: " + req.contentType());
        }
    }

    public static void validateLogin(Request req) {
        if (req.pathInfo().equalsIgnoreCase("/login")) {
            return;
        }

        // -- Get token
        String token = req.queryParams(Constants.URL_PARAM_TOKEN);
        if (token == null) {
            String authHeader = req.headers("Authorization");
            if (authHeader != null) {
                token = authHeader.contains("Bearer ") ? authHeader.substring(authHeader.indexOf(" ") + 1) : null;
            }
        }
        if (token == null) {
            throw new RequestErrorException(HttpStatus.FORBIDDEN_403, "Invalid/expired authentication");
        }
        String username;
        try {
            username = AuthService.getUsername(token);
        } catch (JoseException e) {
            throw new RequestErrorException(HttpStatus.FORBIDDEN_403, "Invalid token " + e.getMessage());
        }

        if (username == null) {
            throw new RequestErrorException(HttpStatus.FORBIDDEN_403, "Invalid/expired authentication");
        }

        // -- Add the username as attribute so that subsequent requests can know who is currently logged in
        req.attribute("principal", username);
    }
}
