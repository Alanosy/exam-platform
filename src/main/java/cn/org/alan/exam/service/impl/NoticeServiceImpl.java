package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.NoticeConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.notice.NoticeForm;
import cn.org.alan.exam.model.vo.notice.NoticeVO;
import cn.org.alan.exam.service.INoticeService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 公告服务实现类
 *
 * @author Alan
 * @since 2024-03-21
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private NoticeConverter noticeConverter;
    @Resource
    private NoticeGradeMapper noticeGradeMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 新建公告
     * @param noticeForm
     * @return
     */
    @Override
    @Transactional
    public Result<String> addNotice(NoticeForm noticeForm) {
        String gradeIds = noticeForm.getGradeIds();
        // 设置创建人
        noticeForm.setUserId(SecurityUtil.getUserId());
        // 添加公告
        Notice notice = noticeConverter.formToEntity(noticeForm);
        // 管理员全部公开
        Integer roleCode = SecurityUtil.getRoleCode();
        if(roleCode==3){
            notice.setIsPublic(1);
        }
        // 判断是否不公开公告
        if(noticeForm.getIsPublic()==0){
            if("".equals(gradeIds)|| gradeIds==null){
                throw new ServiceRuntimeException("公开班级必须添入班级");
            }
            int addNotionRowOther = noticeMapper.insert(notice);
            if (addNotionRowOther == 0) {
                throw new ServiceRuntimeException("添加公告失败");
            }
            //添加公告和班级对应关系
            Integer noticeId = notice.getId();
            List<Integer> gradeIdList = Arrays.stream(gradeIds.split(","))
                    .map(Integer::parseInt)
                    .collect(java.util.stream.Collectors.toList());
            int addNoticeGradeRow = noticeGradeMapper.addNoticeGrade(noticeId,gradeIdList);
            if (addNoticeGradeRow == 0) {
                throw new ServiceRuntimeException("添加公告条数=0失败");
            }
        }else{
            int addNotionRowAdmin = noticeMapper.insert(notice);
            if (addNotionRowAdmin == 0) {
                throw new ServiceRuntimeException("添加公告失败");
            }
        }
        return Result.success("添加公告成功");
    }

    /**
     * 删除公告
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public Result<String> deleteNotice(String ids) {
        // 转换为集合
        List<Integer> noticeIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .collect(java.util.stream.Collectors.toList());
        // 删除公告
        noticeMapper.deleteBatchIds(noticeIds);
        noticeGradeMapper.deleteNoticeGrade(noticeIds);
        return Result.success("删除成功");
    }

    /**
     * 更新公告
     * @param noticeId
     * @param noticeForm
     * @return
     */
    @Override
    @Transactional
    public Result<String> updateNotice(Integer noticeId, NoticeForm noticeForm) {
        Integer isPublic =  noticeMapper.getIsPublic(noticeId);
        String gradeIds = noticeForm.getGradeIds();
        List<Integer> gradeIsList = null;
        if(!"".equals(gradeIds)&&gradeIds!=null){
            gradeIsList = Arrays.stream(gradeIds.split(","))
                    .map(Integer::parseInt)
                    .collect(java.util.stream.Collectors.toList());
        }
        if(isPublic!=noticeForm.getIsPublic()&&isPublic==0){
            // 从不公开改成公开，就是修改公告内容改变公开状态，删除公告班级对应关系
            noticeMapper.updateNotice(noticeId,noticeForm);
            noticeGradeMapper.delNoticeGrade(noticeId);
        }else if(isPublic!=noticeForm.getIsPublic()&&isPublic==1){
            // 从公开改成不公开和状态不变，就是修改公告内容改变公开状态，删除原有的关系添加新公告班级对应关系
            noticeMapper.updateNotice(noticeId,noticeForm);
            noticeGradeMapper.addNoticeGrade(noticeId,gradeIsList);
        }
        // 公开修改公开，不公开修改不公开
        if(isPublic==noticeForm.getIsPublic()&&isPublic==1){
            // 如果是公开的
            // 直接修改内容
            noticeMapper.updateNotice(noticeId,noticeForm);
        }else if (isPublic==noticeForm.getIsPublic()&&isPublic==0){
            // 如果是不同开的 可以修改内容和班级关系
            noticeMapper.updateNotice(noticeId,noticeForm);
            noticeGradeMapper.delNoticeGrade(noticeId);
            noticeGradeMapper.addNoticeGrade(noticeId,gradeIsList);
        }
        return Result.success("修改成功");
    }

    /**
     * 教师管理员获取公告
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    @Override
    public Result<IPage<NoticeVO>> getNotice(Integer pageNum, Integer pageSize, String title) {
        IPage<NoticeVO> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        // 分页查找公告
        List<NoticeVO> record = noticeMapper.getNotice(userId,title);
        // 设置班级列表
        for(NoticeVO temp: record){
            List<Integer> gradeList = noticeGradeMapper.getGradeList(temp.getId());
            temp.setGradeIds(gradeList);
        }
        page.setRecords(record);
        return Result.success("查询成功", page);

    }

    /**
     * 学生获取公告
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Result<IPage<NoticeVO>> getNewNotice(Integer pageNum, Integer pageSize) {
        // 创建分页对象
        Page<NoticeVO> page = new Page<>(pageNum, pageSize);
        // 获取班级的所有老师列表
        Integer gradeId = SecurityUtil.getGradeId();
        // 老师列表
        List<Integer> teachIdList= userGradeMapper.getUserListByGradeId(gradeId);
        // 获取班级和公告关联的公告idlist
        List<Integer> noticeIdList = noticeGradeMapper.getNoticeIdList(gradeId);
        // 查找这些老师非公开的和这个班级的关联的公告，和这些老师和管理员公开的公告
        List<Integer> adminIdList = userMapper.getAdminList();
        // 查找公告
        page = noticeMapper.getNewNotice(page,teachIdList,noticeIdList,adminIdList);
        return Result.success("查询成功", page);
    }
}
