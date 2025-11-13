package com.tzy.mianshiyuan.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Gson 工具类，提供 JSON 序列化与反序列化的便捷方法。
 * <p>
 * 说明：
 * <ul>
 *     <li>单例 Gson 实例，线程安全</li>
 *     <li>支持空值序列化，禁用 HTML 转义，统一日期格式</li>
 * </ul>
 */
public final class GsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    private GsonUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取全局 Gson 实例。
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param src 待序列化对象
     * @return JSON 字符串（对象为 null 时返回 {@code null}）
     */
    public static String toJson(Object src) {
        if (Objects.isNull(src)) {
            return null;
        }
        return GSON.toJson(src);
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象。
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 目标对象（JSON 或类型为 null / 空白字符串时返回 {@code null}）
     * @throws JsonSyntaxException JSON 格式不正确时抛出
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (clazz == null || json == null || json.isBlank()) {
            return null;
        }
        return GSON.fromJson(json, clazz);
    }

    /**
     * 将 JSON 字符串反序列化为复杂泛型对象（如集合、Map）。
     *
     * @param json JSON 字符串
     * @param type Gson Type（可以通过 {@code com.google.gson.reflect.TypeToken} 获取）
     * @param <T>  泛型类型
     * @return 目标对象（JSON 或 type 为 null / 空白字符串时返回 {@code null}）
     * @throws JsonSyntaxException JSON 格式不正确时抛出
     */
    public static <T> T fromJson(String json, Type type) {
        if (type == null || json == null || json.isBlank()) {
            return null;
        }
        return GSON.fromJson(json, type);
    }
}


