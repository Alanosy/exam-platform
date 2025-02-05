package cn.org.alan.exam.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.RoleMapper;
import cn.org.alan.exam.mapper.UserDailyLoginDurationMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserDailyLoginDuration;
import cn.org.alan.exam.model.form.Auth.LoginForm;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.security.SysUserDetails;
import cn.org.alan.exam.service.IAuthService;
import cn.org.alan.exam.util.impl.DateTimeUtil;
import cn.org.alan.exam.util.impl.JwtUtil;
import cn.org.alan.exam.util.impl.SecretUtils;
import cn.org.alan.exam.util.impl.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:33 PM
 */
@Service
public class AuthServiceImpl implements IAuthService {
    private static final String HEARTBEAT_KEY_PREFIX = "user:heartbeat:";
    private static final long HEARTBEAT_INTERVAL_MILLIS = 10 * 60 * 1000; // 10分钟

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserConverter userConverter;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;

    /**
     * 登录
     * @param request
     * @param loginForm 入参
     * @return 响应
     */
    @SneakyThrows(JsonProcessingException.class)
    @Override
    public Result<String> login(HttpServletRequest request, LoginForm loginForm) {
        // 先判断用户是否通过校验
        String s = stringRedisTemplate.opsForValue().get("isVerifyCode" + request.getSession().getId());
        if (StringUtils.isBlank(s)) {
            return Result.failed("请先验证验证码");
        }
        // 根据用户名获取用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, loginForm.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 判读用户名是否存在
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("该用户不存在");
        }
        if(user.getIsDeleted() == 1){
            return Result.failed("该用户已注销");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(SecretUtils.desEncrypt(loginForm.getPassword()), user.getPassword())) {
            return Result.failed("密码错误");
        }
        user.setPassword(null);
        // 根据用户Id获取权限
        List<String> permissions = roleMapper.selectCodeById(user.getRoleId());

        // 数据库获取的权限是字符串springSecurity需要实现GrantedAuthority接口类型，所有这里做一个类型转换
        List<SimpleGrantedAuthority> userPermissions = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("role_" + permission)).toList();

        // 创建一个sysUserDetails对象，该类实现了UserDetails接口
        SysUserDetails sysUserDetails = new SysUserDetails(user);
        // 把转型后的权限放进sysUserDetails对象
        sysUserDetails.setPermissions(userPermissions);

        // 将用户信息转为字符串
        String userInfo = objectMapper.writeValueAsString(user);

        String token = jwtUtil.createJwt(userInfo, userPermissions.stream().map(String::valueOf).toList());
        // 把token放到redis中
        stringRedisTemplate.opsForValue().set("token" + request.getSession().getId(), token, 30, TimeUnit.MINUTES);

        // 封装用户的身份信息，为后续的身份验证和授权操作提供必要的输入
        // 创建UsernamePasswordAuthenticationToken  参数：用户信息，密码，权限列表
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(sysUserDetails, user.getPassword(), userPermissions);

        // 可选，添加Web认证细节
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 用户信息存放进上下文
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        //用户信息放入

        // 清除redis通过校验表示
        stringRedisTemplate.delete("isVerifyCode" + request.getSession().getId());
        return Result.success("登录成功", token);
    }


    @Override
    public Result<String> logout(HttpServletRequest request) {

        // 清除session
        HttpSession session = request.getSession(false);

        if (session != null) {
            // 清除redis
            stringRedisTemplate.delete("token" + session.getId());
            session.invalidate();
        }

        return Result.success("退出成功");
    }


    @SneakyThrows(IOException.class)
    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 生成线性图形验证码的静态方法，参数：图片宽，图片高，验证码字符个数 干扰个数
        LineCaptcha captcha = CaptchaUtil
                .createLineCaptcha(200, 100, 4, 300);

        // 把验证码存放进redis
        // 获取验证码
        String code = captcha.getCode();
        String key = "code" + request.getSession().getId();
        stringRedisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        // 把图片响应到输出流
        response.setContentType("image/jpeg");
        ServletOutputStream os = response.getOutputStream();
        captcha.write(os);
        os.close();
    }

    @Override
    public Result<String> verifyCode(HttpServletRequest request, String code) {
        String key = "code" + request.getSession().getId();
        String rightCode = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(rightCode)) {
            return Result.failed("验证码已过期");
        }
        if (!rightCode.equalsIgnoreCase(code)) {
            return Result.failed("验证码错误");
        }
        // 验证码校验后redis清除验证码，避免重复使用
        stringRedisTemplate.delete(key);
        // 验证码校验后redis存入校验成功，避免用户登录和注册时不验证验证码直接提交
        stringRedisTemplate.opsForValue().set("isVerifyCode" + request.getSession().getId(), "1", 5, TimeUnit.MINUTES);
        return Result.success("验证码校验成功");
    }

    @Override
    public Result<String> register(HttpServletRequest request, UserForm userForm) {

        String s = stringRedisTemplate.opsForValue().get("isVerifyCode" + request.getSession().getId());
        if (StringUtils.isBlank(s)) {
            return Result.failed("请先验证验证码");
        }
        if (!SecretUtils.desEncrypt(userForm.getPassword()).equals(SecretUtils.desEncrypt(userForm.getCheckedPassword()))) {
            return Result.failed("两次密码不一致");
        }

        User user = userConverter.fromToEntity(userForm);

        user.setPassword(new BCryptPasswordEncoder().encode(SecretUtils.desEncrypt(user.getPassword())));
        user.setRoleId(1);
        userMapper.insert(user);
        // 注册成功把redis的是否通过校验验证码删除，防止用户注册后立马登录，还可以使用
        stringRedisTemplate.delete("isVerifyCode" + request.getSession().getId());
        return Result.success("注册成功");
    }



    /**
     * 用户发送心跳，更新最后活跃时间。
     *
     * @return
     */
    @Override
    @SneakyThrows(value = JsonProcessingException.class)
    public Result<String> sendHeartbeat(HttpServletRequest request) {

        String key = HEARTBEAT_KEY_PREFIX + SecurityUtil.getUserId();
        String lastHeartbeatStr = stringRedisTemplate.opsForValue().getAndDelete(key);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        stringRedisTemplate.opsForValue().set(key, now.toString());
        if (lastHeartbeatStr != null) {
            LocalDateTime lastHeartbeat = LocalDateTime.parse(lastHeartbeatStr);
            Duration durationSinceLastHeartbeat = Duration.between(lastHeartbeat, LocalDateTime.now(ZoneOffset.UTC));
            LocalDate date = DateTimeUtil.getDate();
            // 实现累加逻辑，比如更新数据库中的记录
            LambdaQueryWrapper<UserDailyLoginDuration> userDailyLoginDurationLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userDailyLoginDurationLambdaQueryWrapper.eq(UserDailyLoginDuration::getUserId,SecurityUtil.getUserId())
                    .eq(UserDailyLoginDuration::getLoginDate, date);
            List<UserDailyLoginDuration> userDailyLoginDurations =
                    userDailyLoginDurationMapper.selectList(userDailyLoginDurationLambdaQueryWrapper);
            if(userDailyLoginDurations.isEmpty()){
                UserDailyLoginDuration userDailyLoginDuration = new UserDailyLoginDuration();
                userDailyLoginDuration.setUserId(SecurityUtil.getUserId());
                userDailyLoginDuration.setLoginDate(date);
                userDailyLoginDuration.setTotalSeconds(0);
                userDailyLoginDurationMapper.insert(userDailyLoginDuration);
            }else {
                UserDailyLoginDuration userDailyLoginDuration = new UserDailyLoginDuration();
                userDailyLoginDuration.setTotalSeconds(userDailyLoginDurations.get(0)
                        .getTotalSeconds()+(int)durationSinceLastHeartbeat.getSeconds());
                userDailyLoginDuration.setId(userDailyLoginDurations.get(0).getId());
                userDailyLoginDurationMapper.updateById(userDailyLoginDuration);
            }
        }
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(SecurityUtil.getRole());
        SysUserDetails principal = (SysUserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        User user = principal.getUser();
        String string = objectMapper.writeValueAsString(user);
        String jwt = jwtUtil.createJwt(string, permissions);
        stringRedisTemplate.opsForValue().set("token" + request.getSession().getId(), jwt, 30, TimeUnit.MINUTES);
        return Result.success("请求成功",jwt);
    }
}
