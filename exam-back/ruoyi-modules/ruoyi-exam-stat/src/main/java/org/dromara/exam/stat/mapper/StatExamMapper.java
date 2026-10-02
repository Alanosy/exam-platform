package org.dromara.exam.stat.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.exam.stat.domain.StatExam;

/**
 * 考试表只读查询
 *
 * <p>只服务于首页统计，所有查询都走 MP 的方法，不需要 XML。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
public interface StatExamMapper extends BaseMapperPlus<StatExam, StatExam> {
}
