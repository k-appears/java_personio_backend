package com.personio.api.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

import java.lang.reflect.Type;


public class JsonUtil {

    private static final Gson gson = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            setPrettyPrinting().
            disableHtmlEscaping().
            serializeNulls().
            create();

    private JsonUtil() {

    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromStringToObject(String jsonString, Class<T> cls) {
        return gson.fromJson(jsonString, cls);
    }

    public static <T> T fromStringToObject(String jsonString, Type type) {
        return gson.fromJson(jsonString, type);
    }

    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }
}

