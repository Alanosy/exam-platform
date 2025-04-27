package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Reply;
import cn.org.alan.exam.model.form.reply.ReplyForm;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/4 14:11
 */
@Component
@Mapper(componentModel = "spring")
public interface ReplyConverter {
    Reply formToEntity(ReplyForm replyForm);
}
