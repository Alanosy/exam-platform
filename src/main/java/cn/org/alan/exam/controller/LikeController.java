package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.like.LikeForm;
import cn.org.alan.exam.service.ILikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/16 22:20
 */
@RestController
@RequestMapping("/api/like")
@Api(tags = "点赞相关api")
public class LikeController {

    @Resource
    private ILikeService likeService;

    @PostMapping("/doLike")
    @ApiOperation("点赞或取消点赞")
    public Result<String> doLike(@Validated @RequestBody LikeForm likeForm) {
        return likeService.doLike(likeForm);
    }
}
