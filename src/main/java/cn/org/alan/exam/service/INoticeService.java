package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.vo.NoticeVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface INoticeService extends IService<Notice> {

    Result<String> addNotice(Integer userId, NoticeForm noticeForm);

    Result<String> deleteNotice(String ids);

    Result<String> updateNotice(String id, NoticeForm noticeForm);

    Result<IPage<NoticeVO>> getNotice(Integer userId, Integer pageNum, Integer pageSize, String title);
}
