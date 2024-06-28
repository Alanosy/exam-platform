package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.vo.userbook.UserPageBookVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface UserBookMapper extends BaseMapper<UserBook> {

    /**
     * 删除用户的错题本
     * @param userIds 用户id列表
     * @return 影响记录数
     */
    Integer deleteByUserIds(List<Integer> userIds);


    int addUserBookList(List<UserBook> userBookArrayList);

    Page<UserPageBookVO> selectPageVo(Page<UserPageBookVO> page, String examName, Integer userId,Integer role);
}
