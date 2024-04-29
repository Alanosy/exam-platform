package cn.org.alan.exam.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 *     统计管理
 * </p>
 * @Author Alan
 * @Version
 * @Date 2024/3/25 11:22 AM
 */
@RestController
@RequestMapping("/stat")
public class StatController {

    //各班试卷统计信息   添加一个自定义的ExamGradeCount统计方法接受教师角色ID作为参数，返回包含各班试卷统计信息的PaperStatistics列表。具体的SQL查询逻辑将在对应的Mapper XML文件（如ExamGradeMapper.xml）中实现。

}
