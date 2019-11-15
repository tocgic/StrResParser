package kr.pe.tocgic.tools.strresparser.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.Date;

public class JsonUtil {
    public static String toJson(Object object) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(object);
    }

    public static <T>T fromJson(String jsonStr, Class<T> classOfT) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonStr, classOfT);
    }

    public static <T>T fromJson(String json, Type type) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, type);
    }
}
