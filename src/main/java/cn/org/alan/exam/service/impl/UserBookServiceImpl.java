package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.UserBookMapper;
import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.service.IUserBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class UserBookServiceImpl extends ServiceImpl<UserBookMapper, UserBook> implements IUserBookService {

}
