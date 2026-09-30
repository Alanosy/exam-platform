package cn.org.alan.exam.model.vo.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 分类VO
 *
 * @author Moxuec
 * @since 2025-04-09
 */
@Data
@ApiModel(value = "分类VO", description = "分类视图对象")
public class CategoryVO {

    @ApiModelProperty(value = "分类ID")
    private Integer id;

    @ApiModelProperty(value = "分类名称")
    private String name;

    @ApiModelProperty(value = "父分类ID")
    private Integer parentId;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "子分类列表")
    private List<CategoryVO> children;
}