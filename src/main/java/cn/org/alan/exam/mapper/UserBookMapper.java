package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.vo.userbook.UserPageBookVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 错题本表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface UserBookMapper extends BaseMapper<UserBook> {
    /**
     * 添加用户错题本
     * @param userBookArrayList
     * @return
     */
    int addUserBookList(List<UserBook> userBookArrayList);

    /**
     * 分页查询错题本
     * @param page 分页对象
     * @param examName 试卷名称
     * @param userId 用户ID
     * @return
     */
    Page<UserPageBookVO> selectPageVo(Page<UserPageBookVO> page, String examName, Integer userId);
}
