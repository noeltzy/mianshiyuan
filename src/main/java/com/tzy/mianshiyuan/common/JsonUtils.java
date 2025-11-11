package com.tzy.mianshiyuan.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON工具类（基于Gson）
 */
public class JsonUtils {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * 将List<String>转换为JSON字符串
     * @param tagList 标签列表
     * @return JSON字符串，如果为null或空则返回"[]"
     */
    public static String listToString(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return "[]";
        }
        return gson.toJson(tagList);
    }

    /**
     * 将JSON字符串转换为List<String>
     * @param json JSON字符串
     * @return 标签列表，如果为null或空则返回空列表
     */
    public static List<String> stringToList(String json) {
        if (json == null || json.trim().isEmpty() || "null".equals(json)) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> result = gson.fromJson(json, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            // JSON解析失败时返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return gson.toJson(obj);
    }

    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}

