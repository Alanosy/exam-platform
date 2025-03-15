package cn.org.alan.exam.util.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WeiJin
 * @version 1.0
 */
@SuppressWarnings("all")
@Component
@Slf4j
public class JwtUtil {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * jwt密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * jwt过期时间
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 续签阈值，单位为毫秒
     */
    @Value("${jwt.refreshThreshold}")
    private long refreshThreshold;

    /**
     * 创建jwt
     *
     * @return
     */
    public String createJwt(String userInfo, List<String> authList) {
        Date issDate = new Date();//签发时间
        Date expireDate = new Date(issDate.getTime() + expiration);
        //定义头部信息
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("alg", "HS256");//算法
        headerClaims.put("typ", "JWT");//类型只能是jwt
        return JWT.create().withHeader(headerClaims)
                .withIssuer("wj")//签发人
                .withIssuedAt(issDate)//签发时间
               .withExpiresAt(expireDate)//过期时间
                .withClaim("userInfo", userInfo)//自定义声明
                .withClaim("authList", authList)
                .sign(Algorithm.HMAC256(secret));//使用HS256作为签名，SECRET作为密钥
    }

    private JWTVerifier getVerifier() {
        return JWT.require(Algorithm.HMAC256(secret)).build();
    }

    /**
     * 校验token并尝试续签
     *
     * @param token
     * @return 若不需要续签返回原 Token，若需要续签返回新 Token，若验证失败返回 null
     */
    public String verifyAndRefreshToken(String token) {
        JWTVerifier verifier = getVerifier();
        try {
            DecodedJWT jwt = verifier.verify(token);
            // 检查是否需要续签
            if (shouldRefresh(jwt)) {
                String userInfo = jwt.getClaim("userInfo").asString();
                List<String> authList = jwt.getClaim("authList").asList(String.class);
                return createJwt(userInfo, authList);
            }
            return token;
        } catch (JWTVerificationException e) {
            log.error("校验失败", e);
            return null;
        }
    }

    /**
     * 判断是否需要续签
     *
     * @param jwt
     * @return
     */
    private boolean shouldRefresh(DecodedJWT jwt) {
        Date expirationDate = jwt.getExpiresAt();
        long currentTime = System.currentTimeMillis();
        long remainingTime = expirationDate.getTime() - currentTime;
        return remainingTime < refreshThreshold;
    }

    /**
     * 校验token
     *
     * @param token
     * @return
     */
    public boolean verifyToken(String token) {
        //构建jwt校验器
        JWTVerifier verifier = getVerifier();
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("校验失败");
            return false;
        }
    }

    /**
     * 根据token获取用户信息
     *
     * @param token
     * @return
     */
    public String getUser(String token) {
        //构建jwt校验器
        JWTVerifier verifier = getVerifier();
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userInfo").asString();
        } catch (JWTVerificationException e) {
            log.error("用户获取失败");
            return null;
        }
    }

    /**
     * 根据token获取权限
     *
     * @param token
     * @return
     */
    public List<String> getAuthList(String token) {
        //构建jwt校验器
        JWTVerifier verifier = getVerifier();
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("authList").asList(String.class);
        } catch (JWTVerificationException e) {
            log.error("权限列表+获取失败");
            return null;
        }
    }

}
