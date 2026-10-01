package org.dromara.exam.answer.domain.convert;

import io.github.linpeilie.BaseMapper;
import org.dromara.exam.answer.api.domain.RemoteAnswerVo;
import org.dromara.exam.answer.domain.ExamAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 逐题作答 -> 跨服务传输对象
 *
 * <p>RemoteAnswerVo 在 ruoyi-api-exam-answer 模块里，不能反向依赖业务模块加
 * {@code @AutoMapper}，因此按项目约定在本模块声明 BaseMapper 转换器。
 * 主键 id -> answerId 由调用方手动回填，这里不做映射。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamAnswerConvert extends BaseMapper<ExamAnswer, RemoteAnswerVo> {

}
