package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * 说明：
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/3/21 10:43 PM
 */
public interface IFileService {

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 返回上传后的地址
     */
    Result<String> uploadImage(MultipartFile file);
}
