package cn.org.alan.exam.utils.file.impl;

import cn.org.alan.exam.utils.file.FileService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 *
 * @author WeiJin
 * @version 1.0
 */
@Service
@ConditionalOnProperty(name = "online-exam.storage.type", havingValue = "aliyun")
public class AliOSSUtil implements FileService {

    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.access-key-id}")
    private String accessKeyId;
    @Value("${oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${oss.bucket-name}")
    private String bucketName;

    /**
     * 实现上传图片到OSS
     */
    @Override
    public String upload(MultipartFile file) throws IOException {
        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null : "上传文件时获取文件名失败，为null";
        String fileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        //文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
        // 关闭ossClient
        ossClient.shutdown();
        // 把上传到oss的路径返回
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
