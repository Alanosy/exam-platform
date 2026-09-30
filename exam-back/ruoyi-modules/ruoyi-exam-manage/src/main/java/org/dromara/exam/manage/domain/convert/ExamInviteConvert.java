package org.dromara.exam.manage.domain.convert;

import io.github.linpeilie.BaseMapper;
import org.dromara.exam.manage.api.domain.RemoteExamInviteVo;
import org.dromara.exam.manage.domain.ExamInvite;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 考试邀请记录 -> 跨服务传输对象
 *
 * <p>RemoteExamInviteVo 在 ruoyi-api-exam-manage 模块里，不能反向依赖业务模块加
 * {@code @AutoMapper}，因此按项目约定在本模块声明 BaseMapper 转换器，
 * 由 MapStruct Plus 注册进 {@code Converter}，供 {@code MapstructUtils.convert} 使用。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamInviteConvert extends BaseMapper<ExamInvite, RemoteExamInviteVo> {

}
