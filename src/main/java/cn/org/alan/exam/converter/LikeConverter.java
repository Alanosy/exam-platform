package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Like;
import cn.org.alan.exam.model.form.like.LikeForm;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/16 22:18
 */
@Component
@Mapper(componentModel = "spring")
public interface LikeConverter {
    Like formToEntity(LikeForm likeForm);
}
