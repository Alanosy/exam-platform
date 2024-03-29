package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/29 15:51
 */
@Component
@Mapper(componentModel = "spring")
public interface UserConverter {

    User fromToEntity(UserForm userForm);
}
