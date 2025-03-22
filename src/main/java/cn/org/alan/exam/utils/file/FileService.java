package cn.org.alan.exam.utils.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件系统服务
 *
 * @author 赵浩森
 * @version 1.0
 * @since 2025/2/5 11:46
 */
public interface FileService {
    /**
     * 实现上传图片到OSS
     */
    public String upload(MultipartFile file) throws IOException;
    /**
     * 判断是否为常见图片格式
     *
     * @param filename 文件名
     * @return 结果
     */
    public boolean isImage(String filename);

    /**
     * 判断文件是否大于50KB
     *
     * @param file 文件
     * @return 结果
     */
    public boolean isOverSize(MultipartFile file);
}