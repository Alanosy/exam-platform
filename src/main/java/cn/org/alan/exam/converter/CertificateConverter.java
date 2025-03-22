package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.cretificate.CertificateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/5/11 14:40
 */
@Component
@Mapper(componentModel="spring")
public interface CertificateConverter {

    @Mappings({
            @Mapping(target = "certificateName",source = "certificateName")
    })
    Certificate fromToEntity(CertificateForm certificateForm);
}
