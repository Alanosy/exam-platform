package cn.org.alan.exam.model.form.count;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/4/15 9:40
 * 统计班级人数结果类
 */

public class ClassCountResult {
    //班级id
    private Integer gradeId;
    //班级名称
    private String gradeName;
    //班级人数
    private Integer count;

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
