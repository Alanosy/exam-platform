package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 试题管理服务接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IQuestionService extends IService<Question> {

    /**
     * 单题添加
     *
     * @param questionFrom 传参
     * @return 响应
     */
    Result<String> addSingleQuestion(QuestionFrom questionFrom);

    /**
     * 批量删除试题
     *
     * @param ids 试题id
     * @return 响应
     */
    Result<String> deleteBatchByIds(String ids);


    /**
     * 分页查询试题
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param content  试题名
     * @param type     试题类型
     * @param repoId   题库id
     * @return 响应
     */
    Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String content, Integer type, Integer repoId);

    /**
     * 根据试题id获取单题详情
     *
     * @param id 试题id
     * @return 结果集
     */
    Result<QuestionVO> querySingle(Integer id);

    /**
     * 修改试题
     *
     * @param questionFrom 需要修改的试题
     * @return 结果
     */
    Result<String> updateQuestion(QuestionFrom questionFrom);

    /**
     * 批量导入试题
     *
     * @param id   题库Id
     * @param file Excel文件
     * @return 响应结果
     */
    Result<String> importQuestion(Integer id, MultipartFile file);

}
