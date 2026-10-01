package org.dromara.exam.proctor.domain.convert;

import io.github.linpeilie.BaseMapper;
import org.dromara.exam.proctor.api.domain.RemoteProctorSessionVo;
import org.dromara.exam.proctor.domain.ProctorSession;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 监考会话 -> 跨服务传输对象
 *
 * <p>RemoteProctorSessionVo 在 ruoyi-api-exam-proctor 模块里，不能反向依赖业务模块加
 * {@code @AutoMapper}，因此按项目约定在本模块声明 BaseMapper 转换器。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProctorSessionConvert extends BaseMapper<ProctorSession, RemoteProctorSessionVo> {

}
