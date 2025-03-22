package cn.org.alan.exam.utils;
import cn.org.alan.exam.utils.CryptoUtils.Algorithm.Encryption;
import org.junit.Test;

import javax.annotation.Resource;

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
