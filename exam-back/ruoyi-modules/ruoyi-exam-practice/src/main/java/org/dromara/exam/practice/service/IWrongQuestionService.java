package org.dromara.exam.practice.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.practice.api.domain.RemoteWrongQuestionBo;
import org.dromara.exam.practice.domain.bo.WrongNoteBo;
import org.dromara.exam.practice.domain.bo.WrongQuestionBo;
import org.dromara.exam.practice.domain.bo.WrongReviewBo;
import org.dromara.exam.practice.domain.bo.WrongSourceBo;
import org.dromara.exam.practice.domain.vo.WrongOverviewVo;
import org.dromara.exam.practice.domain.vo.WrongQuestionVo;
import org.dromara.exam.practice.domain.vo.WrongReviewRecordVo;
import org.dromara.exam.practice.domain.vo.WrongReviewResultVo;
import org.dromara.exam.practice.domain.vo.WrongSourceVo;

import java.util.List;

/**
 * 错题本Service接口
 *
 * @author ruoyi
 * @date 2026-09-30
 */
public interface IWrongQuestionService {

    /**
     * 我的错题（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     */
    TableDataInfo<WrongQuestionVo> listMyWrong(WrongQuestionBo bo, PageQuery pageQuery);

    /**
     * 错题本总览统计
     */
    WrongOverviewVo overview();

    /**
     * 按来源分组：每场考试 / 每份练习各错了多少题
     */
    List<WrongSourceVo> listSources();

    /**
     * 按来源分页：错题本首页只展示这一层，点进去才看具体错题
     *
     * @param bo        来源维度筛选条件
     * @param pageQuery 分页参数
     */
    TableDataInfo<WrongSourceVo> listSourcesPage(WrongSourceBo bo, PageQuery pageQuery);

    /**
     * 错题详情
     *
     * @param id 错题记录ID
     */
    WrongQuestionVo queryById(Long id);

    /**
     * 重做错题：判分 + 记录 + 刷新掌握状态
     *
     * @param id 错题记录ID
     * @param bo 本次作答
     */
    WrongReviewResultVo review(Long id, WrongReviewBo bo);

    /**
     * 某道错题的重做历史
     *
     * @param id 错题记录ID
     */
    List<WrongReviewRecordVo> listReviews(Long id);

    /**
     * 保存错题笔记
     *
     * @param id 错题记录ID
     * @param bo 笔记内容
     */
    void updateNote(Long id, WrongNoteBo bo);

    /**
     * 手动标记为已掌握
     *
     * @param id 错题记录ID
     */
    void markMastered(Long id);

    /**
     * 移出错题本（标记已忽略，可在「已忽略」里找回）
     *
     * @param id 错题记录ID
     */
    void markIgnored(Long id);

    /**
     * 恢复已忽略的错题，重新纳入错题本
     *
     * @param id 错题记录ID
     */
    void restore(Long id);

    /**
     * 彻底删除错题（软删除）
     *
     * @param id 错题记录ID
     */
    void deleteById(Long id);

    /**
     * 同步错题（供答题 / 阅卷服务跨服务调用）
     *
     * @param boList 错题清单
     */
    void syncWrong(List<RemoteWrongQuestionBo> boList);

}
