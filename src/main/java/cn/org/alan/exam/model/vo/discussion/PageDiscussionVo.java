package cn.org.alan.exam.model.vo.discussion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/4 13:31
 */
@Data
public class PageDiscussionVo {
    private Integer id;
    private String title;
    private String sender;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
