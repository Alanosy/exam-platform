package cn.org.alan.exam.model.vo;

import cn.org.alan.exam.model.entity.Option;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/2 20:27
 */
@Data
public class QuestionVO {
    private Integer id;
    private String content;
    private  String image;
    private String repoTitle;
    private String quType;
    private String analysis;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    private List<Option> options;
}
