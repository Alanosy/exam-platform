package cn.org.alan.exam.converter;


import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.vo.userbook.ReUserExamBookVO;
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
public interface UserBookConverter {
    List<ReUserExamBookVO> listEntityToVo(List<UserBook> list);
}
