package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.RepoVO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/28 20:21
 */
@Component
@Mapper(componentModel = "spring")
public interface RepoConverter {

    List<RepoVO> listEntityToVo(List<Repo> list);
}
