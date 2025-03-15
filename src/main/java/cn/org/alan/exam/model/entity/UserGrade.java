package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Alan
 * @Version
 * @Date 2025/3/14 6:57 PM
 */
@TableName("t_user_grade")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGrade {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    // 用户id
    private Integer uId;
    // 班级id
    private Integer gId;
}
