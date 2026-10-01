package org.dromara.exam.cert.domain.convert;

import io.github.linpeilie.BaseMapper;
import org.dromara.exam.cert.api.domain.RemoteCertVo;
import org.dromara.exam.cert.domain.CertificateRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 证书颁发记录 → 跨服务传输对象
 *
 * <p>RemoteCertVo 在 ruoyi-api-exam-cert 模块里，不能反向依赖业务模块加
 * {@code @AutoMapper}，因此按项目约定在本模块声明 BaseMapper 转换器。
 *
 * <p>字段类型与实体逐一对应：MapStruct 不做隐式转换，类型写错（比如把 0/1 的
 * status 写成 Integer）会直接编译失败。sealUrl / bgUrl 是 VO 独有的展示字段，
 * 由服务层查文件服务后回填，转换器不管。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CertificateRecordConvert extends BaseMapper<CertificateRecord, RemoteCertVo> {
}
