package org.dromara.exam.stat.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.stat.api.domain.HomeStatVo;
import org.dromara.exam.stat.service.IStatHomeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计
 *
 * <p>网关对 /stat/** 配了 StripPrefix=1，这里同时注册 "" 和 "/stat" 两个前缀，
 * 网关剥不剥前缀都能命中，跟证书 / 防作弊服务保持一致。
 *
 * <p>接口只服务登录后的首页，所以鉴权用考试菜单的查看权限：
 * 能看到考试列表的人才能看到全局总览，其他人（考生）前端直接走「我的」视图，
 * 不用再来打这个接口要一次 403。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"", "/stat"})
public class StatHomeController extends BaseController {

    private final IStatHomeService statHomeService;

    /**
     * 首页总览：考试规模 + 答卷情况 + 近7天趋势 + 今日考试安排 + 考试热度榜
     */
    @SaCheckPermission("system:exam:list")
    @GetMapping("/home/overview")
    public R<HomeStatVo> homeOverview() {
        return R.ok(statHomeService.homeOverview());
    }

}
