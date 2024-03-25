package cn.org.alan.exam.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应对象
 *
 * @author haoxr
 * @since 2023/03/24
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaResult {

    private String captchaKey;

    private String captchaBase64;

}
