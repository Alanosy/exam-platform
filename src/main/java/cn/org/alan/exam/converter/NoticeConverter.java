package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.vo.NoticeVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 11:04 PM
 */
@Component
@Mapper(componentModel="spring")
public interface NoticeConverter {
    Notice formToEntity(NoticeForm noticeForm);
    Page<NoticeVO> pageEntityToVo(Page<Notice> noticePage);
}
