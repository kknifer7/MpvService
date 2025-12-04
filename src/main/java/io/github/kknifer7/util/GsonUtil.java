package io.github.kknifer7.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * JSON工具类
 *
 * @author Knifer
 * @version 1.0.0
 */
public final class GsonUtil {

    private final static Gson gson = new Gson();

    private GsonUtil() {
        throw new AssertionError();
    }

    public static String toJson(Object object){
        return gson.toJson(object);
    }

    public static <T> T fromJson(String objectStr, Class<T> clazz){
        return gson.fromJson(objectStr, clazz);
    }

    public static <T> T fromJson(JsonElement jsonElement, Class<T> clazz) {
        return gson.fromJson(jsonElement, clazz);
    }

    public static <T> T fromJson(String objectStr, TypeToken<T> typeToken) {
        return gson.fromJson(objectStr, typeToken);
    }

    public static <T> T fromJson(JsonElement jsonElement, TypeToken<T> typeToken) {
        return gson.fromJson(jsonElement, typeToken);
    }

    public static JsonElement toJsonTree(Object object) {
        return gson.toJsonTree(object);
    }

}
