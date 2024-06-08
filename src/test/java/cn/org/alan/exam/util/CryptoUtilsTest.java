package cn.org.alan.exam.util;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import cn.org.alan.exam.util.CryptoUtils.Algorithm.Encryption;
import cn.org.alan.exam.util.CryptoUtils.Algorithm.Signing;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

/**
 * @Author Alan
 * @Version
 * @Date 2024/6/8 10:53 AM
 */
public class CryptoUtilsTest {
    @Resource
    private CryptoUtils cryptoUtils;

    @Test
    void getDate() throws Exception {
        String secretKey = CryptoUtils.generateSymmetricKey(Encryption.AES_ECB_PKCS5);
        String cipherText = CryptoUtils.encryptSymmetrically(secretKey, null, "测试中文加密", Encryption.AES_ECB_PKCS5);
        System.out.println("生成的密钥为：" + secretKey);
        System.out.println("加密后的密文为：" + cipherText);
        System.out.println("解密后的明文为：" + CryptoUtils.decryptSymmetrically(secretKey, null, cipherText, Encryption.AES_ECB_PKCS5));
    }
}
