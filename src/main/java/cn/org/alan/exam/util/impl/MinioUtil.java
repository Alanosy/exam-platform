package cn.org.alan.exam.util.impl;

import cn.org.alan.exam.util.FileService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.management.RuntimeErrorException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Minio文件服务
 *
 * @author 赵浩森
 * @version 1.0
 * @since 2025/2/5 11:49
 */
@Service
@ConditionalOnProperty(name = "online-exam.storage.type", havingValue = "minio")
public class MinioUtil implements FileService {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accesskey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String accessKeySecret;
    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public String upload(MultipartFile file) throws IOException {
        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null : "上传文件时获取文件名失败，为null";
        String fileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        //上传文件到 MINIO
        try {
            MinioClient client = new MinioClient(endpoint, accessKey, accessKeySecret);

            client.putObject(bucketName, fileName, inputStream, new PutObjectOptions(inputStream.available(), -1));
        } catch (Exception e) {
            // 打印异常
            e.printStackTrace();
            return "";
        } finally {
            // 确保输入流被关闭
            inputStream.close();
        }


        //文件访问路径
        String url = endpoint + "/" + bucketName + "/" + fileName;

        // 把上传到MINIO的路径返回
        return url;
    }

    /**
     * 判断是否为常见图片格式
     *
     * @param filename 文件名
     * @return 结果
     */
    @Override
    public boolean isImage(String filename) {
        String lastName = filename.substring(filename.lastIndexOf(".") + 1);
        String[] lastnames = {"png", "jpg", "jpeg", "bmp"};
        return Arrays.asList(lastnames).contains(lastName);
    }

    /**
     * 判断文件是否大于50KB
     *
     * @param file 文件
     * @return 结果
     */
    @Override
    public boolean isOverSize(MultipartFile file) {
        return file.getSize() > 20 * 1024 * 1024;
    }
}