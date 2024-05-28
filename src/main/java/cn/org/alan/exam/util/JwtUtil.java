package cn.org.alan.exam.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.org.alan.exam.model.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
     * 创建jwt
     *
     * @return
     */
    public String createJwt(String userInfo, List<String> authList) {
        Date issDate = new Date();//签发时间
       Date expireDate = new Date(issDate.getTime() + 1000 * 30);
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



    /**
     * 校验token
     *
     * @param token
     * @return
     */
    public boolean verifyToken(String token) {
        //构建jwt校验器
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
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
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
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
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("authList").asList(String.class);
        } catch (JWTVerificationException e) {
            log.error("权限列表+获取失败");
            return null;
        }
    }

}
