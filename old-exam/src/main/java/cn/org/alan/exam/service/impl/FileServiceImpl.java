package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.IFileService;
import cn.org.alan.exam.utils.file.FileService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

/**
 * 说明：
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/3/21 10:44 PM
 */
@Service
public class FileServiceImpl implements IFileService {
    @Resource
    private FileService fileService;

    @SneakyThrows(IOException.class)
    @Override
    public Result<String> uploadImage(MultipartFile file) {
        if (!fileService.isImage(Objects.requireNonNull(file.getOriginalFilename()))) {
            throw new ServiceRuntimeException("上传头像到文件不是常用图片格式(png、jpg、jpeg、bmp)");
        }
        if (fileService.isOverSize(file)) {
            throw new ServiceRuntimeException("图片大小不能超过50KB");
        }
        String url = fileService.upload(file);
        if (StringUtils.isBlank(url)) {
            throw new ServiceRuntimeException("图片上传失败，url地址没有返回");
        }
        return Result.success("图片上传成功", url);
    }

}
