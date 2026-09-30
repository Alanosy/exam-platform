package org.dromara.exam.manage.domain.convert;

import io.github.linpeilie.BaseMapper;
import org.dromara.exam.manage.api.domain.RemoteExamVo;
import org.dromara.exam.manage.domain.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 考试主 -> 跨服务传输对象
 *
 * <p>RemoteExamVo 在 ruoyi-api-exam-manage 模块里，不能反向依赖业务模块加
 * {@code @AutoMapper}，因此按项目约定在本模块声明 BaseMapper 转换器。
 * 主键 id -> examId 由调用方手动回填，这里不做映射。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamConvert extends BaseMapper<Exam, RemoteExamVo> {

}
