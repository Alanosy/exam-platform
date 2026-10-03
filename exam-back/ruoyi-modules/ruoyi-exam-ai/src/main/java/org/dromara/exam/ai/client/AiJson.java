package org.dromara.exam.ai.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * agent 返回 JSON 的取值工具
 *
 * <p><b>为什么不直接 convertValue</b>：agent 是 Python 服务，字段是蛇形
 * （question_type / knowledge_points / quality_score），Java VO 是驼峰。
 * 依赖 Jackson 的全局驼峰配置很脆——哪天有人改了 spring.jackson 的配置，
 * 这里就静默取不到值（不报错，全是 null），这种 bug 极难排查。
 * 手写映射啰嗦但可控。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public final class AiJson {

    private AiJson() {
    }

    /**
     * 取字符串，缺失或空节点返回默认值
     */
    public static String str(JsonNode node, String field) {
        return str(node, field, null);
    }

    public static String str(JsonNode node, String field, String def) {
        if (node == null || !node.hasNonNull(field)) {
            return def;
        }
        String v = node.get(field).asText("").trim();
        return v.isEmpty() ? def : v;
    }

    /**
     * 取数值，缺失返回默认值
     */
    public static BigDecimal bd(JsonNode node, String field) {
        return bd(node, field, null);
    }

    public static BigDecimal bd(JsonNode node, String field, BigDecimal def) {
        if (node == null || !node.hasNonNull(field)) {
            return def;
        }
        JsonNode v = node.get(field);
        return v.isNumber() ? v.decimalValue() : def;
    }

    /**
     * 取整数，缺失返回默认值
     */
    public static Integer i(JsonNode node, String field) {
        return i(node, field, null);
    }

    public static Integer i(JsonNode node, String field, Integer def) {
        if (node == null || !node.hasNonNull(field)) {
            return def;
        }
        JsonNode v = node.get(field);
        return v.isNumber() ? v.asInt() : def;
    }

    /**
     * 取布尔，缺失返回默认值
     */
    public static Boolean bool(JsonNode node, String field, Boolean def) {
        if (node == null || !node.hasNonNull(field)) {
            return def;
        }
        return node.get(field).asBoolean(def);
    }

    /**
     * 取数组节点，缺失返回空列表
     */
    public static List<JsonNode> list(JsonNode node, String field) {
        List<JsonNode> out = new ArrayList<>();
        if (node == null || !node.has(field) || !node.get(field).isArray()) {
            return out;
        }
        node.get(field).forEach(out::add);
        return out;
    }

    /**
     * 取对象节点，缺失返回 null
     */
    public static JsonNode obj(JsonNode node, String field) {
        if (node == null || !node.has(field) || !node.get(field).isObject()) {
            return null;
        }
        return node.get(field);
    }

    /**
     * 取字符串数组
     */
    public static List<String> strList(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        for (JsonNode item : list(node, field)) {
            String v = item.asText("").trim();
            if (!v.isEmpty()) {
                out.add(v);
            }
        }
        return out;
    }
}
