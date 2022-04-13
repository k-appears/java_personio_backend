package com.personio.api.utils;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil {
    private static final String INVALID_BODY = "Invalid JSON body %s, use a validator as https://jsonlint.com/ to check for errors";
    private static final String EMPTY_BODY = "Empty body";
    private static final String EMPTY_SUPERVISOR = "Empty supervisor";

    private RequestUtil() {
    }

    public static String getQueryName(Request request) {
        return request.queryParamOrDefault("name", "").replaceAll("\\P{Print}", "").trim();
    }

    public static Map<String, String> validateBody(String requestBody) {
        Map<String, String> input;
        try {
            Type listType = new TypeToken<HashMap<String, String>>() {
            }.getType();
            input = JsonUtil.fromStringToObject(requestBody, listType);
        } catch (JsonSyntaxException e) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, String.format(INVALID_BODY, requestBody));
        }
        if (input.isEmpty()) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, EMPTY_BODY);
        }
        if (input.containsValue(null) || input.containsValue("")) {
            throw new RequestErrorException(HttpStatus.BAD_REQUEST_400, EMPTY_SUPERVISOR);
        }
        return input;
    }
}
