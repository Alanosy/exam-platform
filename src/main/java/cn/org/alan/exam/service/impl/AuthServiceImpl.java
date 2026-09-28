package cn.org.alan.exam.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.RoleMapper;
import cn.org.alan.exam.mapper.UserDailyLoginDurationMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Log;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserDailyLoginDuration;
import cn.org.alan.exam.model.form.auth.LoginForm;
import cn.org.alan.exam.model.form.auth.MiniprogramBindForm;
import cn.org.alan.exam.model.form.auth.MiniprogramLoginForm;
import cn.org.alan.exam.model.form.user.UserForm;
import cn.org.alan.exam.service.IAuthService;
import cn.org.alan.exam.service.ILogService;
import cn.org.alan.exam.utils.*;
import cn.org.alan.exam.utils.security.SysUserDetails;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;


/**
 * 权限管理服务实现类
 *
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:33 PM
 */
@Slf4j
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
    @Value("${online-exam.login.captcha.enabled}")
    private boolean captchaEnabled;
    @Value("${wx.miniprogram.appid}")
    private String wxAppid;
    @Value("${wx.miniprogram.secret}")
    private String wxSecret;
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    private ILogService logService;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 登录
     *
     * @param request
     * @param loginForm 入参
     * @return 响应
     */
    @SneakyThrows(JsonProcessingException.class)
    @Override
    public Result<String> login(HttpServletRequest request, LoginForm loginForm) {
        // 先判断用户是否通过校验
        String s = stringRedisTemplate.opsForValue().get("isVerifyCode" + request.getSession().getId());
        if (StringUtils.isBlank(s) && captchaEnabled) {
            throw new ServiceRuntimeException("请先验证验证码");
        }
        // 根据用户名获取用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, loginForm.getUsername());
        User user = userMapper.selectOne(wrapper);
        // 判读用户名是否存在
        if (Objects.isNull(user)) {
            throw new ServiceRuntimeException("该用户不存在");
        }
        if (user.getIsDeleted() == 1) {
            throw new ServiceRuntimeException("该用户已注销");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String userPassword = SecretUtils.desEncrypt(loginForm.getPassword());
        if (!encoder.matches(userPassword, user.getPassword())) {
            throw new ServiceRuntimeException("密码错误");
        }
        user.setPassword(null);
        // 根据用户角色代码
        List<String> permissions = roleMapper.selectCodeById(user.getRoleId());

        // 数据库获取的权限是字符串springSecurity需要实现GrantedAuthority接口类型，所有这里做一个类型转换
        List<SimpleGrantedAuthority> userPermissions = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("role_" + permission)).collect(java.util.stream.Collectors.toList());

        // 创建一个sysUserDetails对象，该类实现了UserDetails接口
        SysUserDetails sysUserDetails = new SysUserDetails(user);
        // 把转型后的权限放进sysUserDetails对象
        sysUserDetails.setPermissions(userPermissions);
        // 将用户序列化 转为字符串
        String userInfo = objectMapper.writeValueAsString(user);
        // 创建token
        String token = jwtUtil.createJwt(userInfo, userPermissions.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()));
        // 把token放到redis中
        stringRedisTemplate.opsForValue().set("token:" + token, token, 30, TimeUnit.MINUTES);

        // 封装用户的身份信息，为后续的身份验证和授权操作提供必要的输入
        // 创建UsernamePasswordAuthenticationToken  参数：用户信息，密码，权限列表
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(sysUserDetails, user.getPassword(), userPermissions);

        // 可选，添加Web认证细节
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 用户信息存放进上下文
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        // 用户信息放入
        // 清除redis通过校验表示
        stringRedisTemplate.delete("isVerifyCode" + request.getSession().getId());

        // 记录日志
        String device = httpServletRequest.getHeader("User-Agent");
        String ipRegion = Optional.ofNullable(IPUtils.getIPRegion(httpServletRequest)).orElse("暂无信息");
        Log log = Log.builder()
                .place(ipRegion)
                .device(extractDeviceType(device))
                .behavior("设备登录")
                .userId(user.getId()).build();
        logService.add(log);
        return Result.success("登录成功", token);
    }


    /**
     * 获取设备名称
     * @param userAgent
     * @return
     */
    public static String extractDeviceType(String userAgent) {
        // 定义正则表达式模式
        String pattern = "\\((.*?);";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(userAgent);
        if (m.find()) {
            // 返回匹配到的设备类型
            return m.group(1);
        }
        return null;
    }

    /**
     * 注销
     *
     * @param request request对象，需要清除session里面的内容
     * @return
     */
    @Override
    public Result<String> logout(HttpServletRequest request) {
        // 清除session
        HttpSession session = request.getSession(false);
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token) && session != null) {
            // 记录日志
            String device = httpServletRequest.getHeader("User-Agent");
            String ipRegion = Optional.ofNullable(IPUtils.getIPRegion(httpServletRequest)).orElse("暂无信息");
            Log log = Log.builder()
                    .place(ipRegion)
                    .device(extractDeviceType(device))
                    .behavior("设备登出")
                    .userId(SecurityUtil.getUserId()).build();
            logService.add(log);
            token = token.substring(7);
            stringRedisTemplate.delete("token:" + request.getSession().getId());
            session.invalidate();
        }
        return Result.success("退出成功");
    }

    /**
     * 获取图形验证码
     *
     * @param request  request对象，获取sessionId
     * @param response response对象，响应图片
     */
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

    /**
     * 验证验证码
     *
     * @param request request对象获取sessionId
     * @param code    用户输入的验证码
     * @return
     */
    @Override
    public Result<String> verifyCode(HttpServletRequest request, String code) {
        String key = "code" + request.getSession().getId();
        String rightCode = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(rightCode)) {
            throw new ServiceRuntimeException("验证码已过期");
        }
        if (!rightCode.equalsIgnoreCase(code)) {
            throw new ServiceRuntimeException("验证码错误");
        }
        // 验证码校验后redis清除验证码，避免重复使用
        stringRedisTemplate.delete(key);
        // 验证码校验后redis存入校验成功，避免用户登录和注册时不验证验证码直接提交
        stringRedisTemplate.opsForValue().set("isVerifyCode" + request.getSession().getId(), "1", 5, TimeUnit.MINUTES);
        return Result.success("验证码校验成功");
    }

    /**
     * 注册用户
     *
     * @param request  request对象，用于获取sessionId
     * @param userForm 用户信息
     * @return
     */
    @Override
    public Result<String> register(HttpServletRequest request, UserForm userForm) {
        // 判断验证码
        String s = stringRedisTemplate.opsForValue().get("isVerifyCode" + request.getSession().getId());
        if (StringUtils.isBlank(s)) {
            throw new ServiceRuntimeException("请先验证验证码");
        }
        // 判断两次密码是否一致
        if (!SecretUtils.desEncrypt(userForm.getPassword()).equals(SecretUtils.desEncrypt(userForm.getCheckedPassword()))) {
            throw new ServiceRuntimeException("两次密码不一致");
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
    public Result<String> sendHeartbeat(HttpServletRequest request) {
        Integer userId = SecurityUtil.getUserId();
        if (userId == null) {
            // 未登录用户，返回失败
            return Result.failed("用户未登录");
        }
        
        // 创建Redis键
        String key = HEARTBEAT_KEY_PREFIX + userId;
        if (SecurityUtil.getRoleCode() == 1) {
            // 删除该键值并获取上一次的心跳时间
            String lastHeartbeatStr = stringRedisTemplate.opsForValue().get(key);
            // 获取当前时间
            LocalDateTime utcTime = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime now = utcTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
            // 设置新的时间
            stringRedisTemplate.opsForValue().set(key, now.toString());
            LocalDateTime lastHeartbeat = null;
            // 将上次时间字符串转换为时间对象
            if (lastHeartbeatStr == null) {
                lastHeartbeat = now;
            } else {
                lastHeartbeat = LocalDateTime.parse(lastHeartbeatStr);
            }
            // 计算上次和现在过了多久 连个时间的时间差
            Duration durationSinceLastHeartbeat = Duration.between(lastHeartbeat, now);
            // 获取今天日期
            LocalDate date = DateTimeUtil.getDate();
            // 实现累加逻辑，比如更新数据库中的记录
            // 获取当前用户的今天的记录
            userId = SecurityUtil.getUserId();
            UserDailyLoginDuration userDailyLogin = userDailyLoginDurationMapper.getTodayRecord(userId, date);
            // 如果记录为空
            if (Objects.isNull(userDailyLogin)) {
                // 如果没记录
                UserDailyLoginDuration userDailyLoginDuration = new UserDailyLoginDuration();
                // 设置用户id
                userDailyLoginDuration.setUserId(userId);
                // 设置今天日期
                userDailyLoginDuration.setLoginDate(date);
                // 存入秒数
                userDailyLoginDuration.setTotalSeconds((int) durationSinceLastHeartbeat.getSeconds());
                userDailyLoginDurationMapper.insert(userDailyLoginDuration);
            } else {
                // 如果有记录
                // 累加今天的时长
                userDailyLogin.setTotalSeconds(userDailyLogin.getTotalSeconds()
                        + (int) durationSinceLastHeartbeat.getSeconds());
                userDailyLoginDurationMapper.updateById(userDailyLogin);
            }
        }
        return Result.success("请求成功");
    }

    /**
     * 小程序微信登录
     *
     * @param request
     * @param miniprogramLoginForm
     * @return
     */
    @SneakyThrows(IOException.class)
    @Override
    public Result<String> miniprogramLogin(HttpServletRequest request, MiniprogramLoginForm miniprogramLoginForm) {
        // 调用微信接口获取openid和session_key
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxAppid
                + "&secret=" + wxSecret
                + "&js_code=" + miniprogramLoginForm.getCode()
                + "&grant_type=authorization_code";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String responseBody = response.getBody();
        
        // 解析微信返回的JSON
        Map<String, Object> wxResponse = objectMapper.readValue(responseBody, Map.class);
        String openid = (String) wxResponse.get("openid");
        String sessionKey = (String) wxResponse.get("session_key");
        
        if (StringUtils.isBlank(openid)) {
            throw new ServiceRuntimeException("微信登录失败，获取openid失败");
        }
        
        // 根据openid查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getWxOpenid, openid);
        User user = userMapper.selectOne(wrapper);
        
        if (Objects.isNull(user)) {
            // 用户不存在，需要绑定账号
            return Result.failed("该微信未绑定账号，请先绑定");
        }
        
        if (user.getIsDeleted() == 1) {
            throw new ServiceRuntimeException("该用户已注销");
        }
        
        // 更新session_key
        user.setWxSessionKey(SecretUtils.desEncrypt(sessionKey));
        userMapper.updateById(user);

        // 生成token
        user.setPassword(null);
        List<String> permissions = roleMapper.selectCodeById(user.getRoleId());
        List<SimpleGrantedAuthority> userPermissions = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("role_" + permission)).collect(java.util.stream.Collectors.toList());

        log.info("小程序登录，用户: {}, 原始权限: {}, 存储权限: {}", user.getUserName(), permissions, userPermissions);

        SysUserDetails sysUserDetails = new SysUserDetails(user);
        sysUserDetails.setPermissions(userPermissions);
        String userInfo = objectMapper.writeValueAsString(user);
        String token = jwtUtil.createJwt(userInfo, userPermissions.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()));
        stringRedisTemplate.opsForValue().set("token:" + token, token, 30, TimeUnit.MINUTES);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(sysUserDetails, user.getPassword(), userPermissions);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        
        // 记录日志
        String device = httpServletRequest.getHeader("User-Agent");
        String ipRegion = Optional.ofNullable(IPUtils.getIPRegion(httpServletRequest)).orElse("暂无信息");
        Log log = Log.builder()
                .place(ipRegion)
                .device(extractDeviceType(device))
                .behavior("小程序登录")
                .userId(user.getId()).build();
        logService.add(log);
        
        return Result.success("登录成功", token);
    }

    /**
     * 小程序绑定已有账号
     *
     * @param request
     * @param miniprogramBindForm
     * @return
     */
    @SneakyThrows(IOException.class)
    @Override
    public Result<String> miniprogramBind(HttpServletRequest request, MiniprogramBindForm miniprogramBindForm) {
        // 调用微信接口获取openid和session_key
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxAppid
                + "&secret=" + wxSecret
                + "&js_code=" + miniprogramBindForm.getCode()
                + "&grant_type=authorization_code";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String responseBody = response.getBody();
        
        // 解析微信返回的JSON
        Map<String, Object> wxResponse = objectMapper.readValue(responseBody, Map.class);
        String openid = (String) wxResponse.get("openid");
        String sessionKey = (String) wxResponse.get("session_key");
        
        if (StringUtils.isBlank(openid)) {
            throw new ServiceRuntimeException("微信登录失败，获取openid失败");
        }
        
        // 验证账号密码
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, miniprogramBindForm.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        if (Objects.isNull(user)) {
            throw new ServiceRuntimeException("该用户不存在");
        }
        if (user.getIsDeleted() == 1) {
            throw new ServiceRuntimeException("该用户已注销");
        }
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (StringUtils.isBlank(miniprogramBindForm.getPassword())) {
            throw new ServiceRuntimeException("密码不能为空");
        }
        // 小程序发送明文密码，无需解密
        if (!encoder.matches(miniprogramBindForm.getPassword(), user.getPassword())) {
            throw new ServiceRuntimeException("密码错误");
        }
        
        // 检查该openid是否已被其他用户绑定
        LambdaQueryWrapper<User> openidWrapper = new LambdaQueryWrapper<>();
        openidWrapper.eq(User::getWxOpenid, openid);
        User existingUser = userMapper.selectOne(openidWrapper);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new ServiceRuntimeException("该微信已绑定其他账号");
        }
        
        // 绑定微信信息
        user.setWxOpenid(openid);
        user.setWxSessionKey(SecretUtils.desEncrypt(sessionKey));
        userMapper.updateById(user);
        
        // 生成token
        user.setPassword(null);
        List<String> permissions = roleMapper.selectCodeById(user.getRoleId());
        List<SimpleGrantedAuthority> userPermissions = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("role_" + permission)).collect(java.util.stream.Collectors.toList());
        
        SysUserDetails sysUserDetails = new SysUserDetails(user);
        sysUserDetails.setPermissions(userPermissions);
        String userInfo = objectMapper.writeValueAsString(user);
        String token = jwtUtil.createJwt(userInfo, userPermissions.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()));
        stringRedisTemplate.opsForValue().set("token:" + token, token, 30, TimeUnit.MINUTES);
        
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(sysUserDetails, user.getPassword(), userPermissions);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        
        // 记录日志
        String device = httpServletRequest.getHeader("User-Agent");
        String ipRegion = Optional.ofNullable(IPUtils.getIPRegion(httpServletRequest)).orElse("暂无信息");
        Log log = Log.builder()
                .place(ipRegion)
                .device(extractDeviceType(device))
                .behavior("小程序绑定账号")
                .userId(user.getId()).build();
        logService.add(log);
        
        return Result.success("绑定成功", token);
    }
}
