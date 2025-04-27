package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.IFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 说明：
 * 文件处理Controller
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/4/6 3:23 PM
 */
@RestController
@Api(tags = "文件服务接口")
@RequestMapping("/api/upload")
public class FileController {
    @Autowired
    private IFileService fileService;
    /**
     * 上传图片
     *
     * @param file 文件
     * @return 返回头像地址
     */
    @ApiOperation("上传图片")
    @PostMapping("/image")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return fileService.uploadImage(file);
    }
}
