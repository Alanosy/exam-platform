package cn.org.alan.exam.model.vo.exercise;

import cn.org.alan.exam.model.entity.Option;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/6/6 18:13
 */
@Data
public class AnswerInfoVO {
    private Integer id;
    private String content;
    private Integer repoId;
    private  String image;
    private String repoTitle;
    private Integer quType;
    private String analysis;
    private String answerContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    private List<Option> options;
}
