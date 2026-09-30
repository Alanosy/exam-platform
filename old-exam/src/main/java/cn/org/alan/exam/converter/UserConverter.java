package cn.org.alan.exam.converter;


import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.user.UserForm;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/29 15:51
 */
@Component
@Mapper(componentModel = "spring")
public interface UserConverter {

    User fromToEntity(UserForm userForm);

    List<User> listFromToEntity(List<UserForm> list);

}
