package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.KnowledgePoint;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 知识点视图对象 exam_knowledge_point
 *
 * <p>树接口会组装好 {@code children} 直接返回，前端不用自己构树；
 * 列表 / 详情接口是平铺的（不带 children）。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
@AutoMapper(target = KnowledgePoint.class)
public class KnowledgePointVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 父级ID，0 表示章节（根节点）
     */
    private Long parentId;

    /**
     * 知识点名称
     */
    private String name;

    /**
     * 排序
     */
    private Long sort;

    /**
     * 子节点（仅树接口返回）
     */
    private List<KnowledgePointVo> children;

}
