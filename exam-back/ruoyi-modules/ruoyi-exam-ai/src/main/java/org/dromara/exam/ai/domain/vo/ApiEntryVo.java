package org.dromara.exam.ai.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 给 AI 看的「系统能力清单」条目
 *
 * <p>AI 想了解这套系统能干什么，靠的就是这份清单：它列出了考试域各个接口的
 * 路径、方法、入参、语义以及**需要的权限码**。权限码有两个用处：
 * 1. 让模型在规划阶段就知道「这个动作要 system:exam:list 权限」；
 * 2. 真正调用时由网关和业务服务的 {@code @SaCheckPermission} 强制执行，
 *    模型说要调、没权限也调不通 —— 规划与执行用的是同一套权限。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class ApiEntryVo {

    /** 能力编码：模型在计划里引用的是它，不是裸路径 */
    private String code;

    /** 中文名 */
    private String name;

    /** GET / POST / PUT / DELETE */
    private String method;

    /** 网关路径（含前缀） */
    private String path;

    /** 这条接口是干什么的，直接喂给模型 */
    private String desc;

    /** 入参说明，key=参数名 value=说明 */
    private List<String> params = new ArrayList<>();

    /** read / write */
    private String risk = "read";

    /** 需要的权限码，空表示登录即可 */
    private String permission = "";

    /**
     * 适用人群：any / admin / student
     *
     * <p>角色隔离用：学生只能看到并调用 student 与 any 的条目，
     * 管理端的条目连清单都不下发给他的会话 —— 看不见就不会去规划。
     * 这只是「少吃一次 403」的优化，真正的拦截依旧在网关。
     */
    private String audience = "any";

    public ApiEntryVo() {
    }

    public ApiEntryVo(String code, String name, String method, String path, String desc) {
        this.code = code;
        this.name = name;
        this.method = method;
        this.path = path;
        this.desc = desc;
    }

    public ApiEntryVo param(String text) {
        this.params.add(text);
        return this;
    }

    public ApiEntryVo permission(String permission) {
        this.permission = permission;
        return this;
    }

    public ApiEntryVo write() {
        this.risk = "write";
        return this;
    }

    /** 标记为管理端能力（需要后台权限的人才能用） */
    public ApiEntryVo admin() {
        this.audience = "admin";
        return this;
    }

    /** 标记为考生本人视角能力 */
    public ApiEntryVo student() {
        this.audience = "student";
        return this;
    }
}
