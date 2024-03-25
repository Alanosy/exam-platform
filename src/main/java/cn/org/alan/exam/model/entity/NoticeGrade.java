package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@TableName("t_notice_grade")
public class NoticeGrade implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID   公告班级关联表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 公告id
     */
    private Integer noticeId;

    /**
     * 班级
     */
    private Integer gradeId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Integer noticeId) {
        this.noticeId = noticeId;
    }
    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    @Override
    public String toString() {
        return "NoticeGrade{" +
            "id=" + id +
            ", noticeId=" + noticeId +
            ", gradeId=" + gradeId +
        "}";
    }
}
