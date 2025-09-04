package org.dromara.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.api.RemoteDeptService;
import org.dromara.system.api.RemoteUserService;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.core.dto.NodeJson;
import org.dromara.warm.flow.core.dto.PromptContent;
import org.dromara.warm.flow.core.enums.NodeType;
import org.dromara.warm.flow.core.utils.MapUtil;
import org.dromara.warm.flow.orm.entity.FlowHisTask;
import org.dromara.warm.flow.orm.mapper.FlowHisTaskMapper;
import org.dromara.warm.flow.ui.service.ChartExtService;
import org.dromara.workflow.common.ConditionalOnEnable;
import org.dromara.workflow.common.constant.FlowConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 流程图提示信息
 *
 * @author AprilWind
 */
@ConditionalOnEnable
@Slf4j
@RequiredArgsConstructor
@Service
public class FlwChartExtServiceImpl implements ChartExtService {

    private final FlowHisTaskMapper flowHisTaskMapper;
    private final DictService dictService;
    @Value("${warm-flow.node-tooltip:true}")
    private boolean nodeTooltip;

    @DubboReference
    private RemoteUserService remoteUserService;
    @DubboReference
    private RemoteDeptService remoteDeptService;

    /**
     * 设置流程图提示信息
     *
     * @param defJson 流程定义json对象
     */
    @Override
    public void execute(DefJson defJson) {
        // 配置关闭，直接返回，不渲染悬浮窗
        if (!nodeTooltip) {
            return;
        }

        // 根据流程实例ID查询所有相关的历史任务列表
        List<FlowHisTask> flowHisTasks = this.getHisTaskGroupedByNode(defJson.getInstance().getId());
        if (CollUtil.isEmpty(flowHisTasks)) {
            return;
        }

        // 按节点编号（nodeCode）对历史任务进行分组
        Map<String, List<FlowHisTask>> groupedByNode = StreamUtils.groupByKey(flowHisTasks, FlowHisTask::getNodeCode);

        // 批量查询所有审批人的用户信息
        List<RemoteUserVo> userDTOList = remoteUserService.selectListByIds(StreamUtils.toList(flowHisTasks, e -> Convert.toLong(e.getApprover())));

        // 将查询到的用户列表转换为以用户ID为key的映射
        Map<Long, RemoteUserVo> userMap = StreamUtils.toIdentityMap(userDTOList, RemoteUserVo::getUserId);

        Map<String, String> dictType = dictService.getAllDictByDictType(FlowConstant.WF_TASK_STATUS);

        for (NodeJson nodeJson : defJson.getNodeList()) {
            List<FlowHisTask> taskList = groupedByNode.get(nodeJson.getNodeCode());
            if (CollUtil.isEmpty(taskList)) {
                continue;
            }

            // 按审批人分组去重，保留最新处理记录，最终转换成 List
            List<FlowHisTask> latestPerApprover = taskList.stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toMap(
                        FlowHisTask::getApprover,
                        Function.identity(),
                        (oldTask, newTask) -> newTask.getUpdateTime().after(oldTask.getUpdateTime()) ? newTask : oldTask,
                        LinkedHashMap::new
                    ),
                    map -> new ArrayList<>(map.values())
                ));

            // 处理当前节点的扩展信息
            this.processNodeExtInfo(nodeJson, latestPerApprover, userMap, dictType);
        }
    }

    /**
     * 初始化流程图提示信息
     *
     * @param defJson 流程定义json对象
     */
    @Override
    public void initPromptContent(DefJson defJson) {
        // 配置关闭，直接返回，不渲染悬浮窗
        if (!nodeTooltip) {
            return;
        }

        defJson.setTopText("流程名称: " + defJson.getFlowName());
        defJson.getNodeList().forEach(nodeJson -> {
            nodeJson.setPromptContent(
                new PromptContent()
                    // 提示信息
                    .setInfo(
                        CollUtil.newArrayList(
                            new PromptContent.InfoItem()
                                .setPrefix("任务名称: ")
                                .setContent(nodeJson.getNodeName())
                                .setContentStyle(Map.of(
                                    "border", "1px solid #d1e9ff",
                                    "backgroundColor", "#e8f4ff",
                                    "padding", "4px 8px",
                                    "borderRadius", "4px"
                                ))
                                .setRowStyle(Map.of(
                                    "fontWeight", "bold",
                                    "margin", "0 0 6px 0",
                                    "padding", "0 0 8px 0",
                                    "borderBottom", "1px solid #ccc"
                                ))
                        )
                    )
                    // 弹窗样式
                    .setDialogStyle(MapUtil.mergeAll(
                        "position", "absolute",
                        "backgroundColor", "#fff",
                        "border", "1px solid #ccc",
                        "borderRadius", "4px",
                        "boxShadow", "0 2px 8px rgba(0, 0, 0, 0.15)",
                        "padding", "8px 12px",
                        "fontSize", "14px",
                        "zIndex", "1000",
                        "maxWidth", "500px",
                        "maxHeight", "300px",
                        "overflowY", "auto",
                        "overflowX", "hidden",
                        "color", "#333",
                        "pointerEvents", "auto",
                        "scrollbarWidth", "thin"
                    ))
            );
        });
    }

    /**
     * 处理节点的扩展信息，构建用于流程图悬浮提示的内容
     *
     * @param nodeJson 当前流程节点对象，包含节点基础信息和提示内容容器
     * @param taskList 当前节点关联的历史审批任务列表，用于生成提示信息
     * @param userMap  用户信息映射表，key 为用户ID，value 为用户DTO对象，用于获取审批人信息
     * @param dictType 数据字典映射表，key 为字典项编码，value 为对应显示值，用于翻译审批状态等
     */
    private void processNodeExtInfo(NodeJson nodeJson, List<FlowHisTask> taskList, Map<Long, RemoteUserVo> userMap, Map<String, String> dictType) {

        // 获取节点提示内容对象中的 info 列表，用于追加提示项
        List<PromptContent.InfoItem> info = nodeJson.getPromptContent().getInfo();

        // 遍历所有任务记录，构建提示内容
        for (FlowHisTask task : taskList) {
            RemoteUserVo userDTO = userMap.get(Convert.toLong(task.getApprover()));
            if (ObjectUtil.isEmpty(userDTO)) {
                continue;
            }

            // 查询用户所属部门名称
            String deptName = remoteDeptService.selectDeptNameByIds(Convert.toStr(userDTO.getDeptId()));

            // 添加标题项，如：👤 张三（市场部）
            info.add(new PromptContent.InfoItem()
                .setPrefix(StringUtils.format("👥 {}（{}）", userDTO.getNickName(), deptName))
                .setPrefixStyle(Map.of(
                    "fontWeight", "bold",
                    "fontSize", "15px",
                    "color", "#333"
                ))
                .setRowStyle(Map.of(
                    "margin", "8px 0",
                    "borderBottom", "1px dashed #ccc"
                ))
            );

            // 添加具体信息项：账号、耗时、时间
            info.add(buildInfoItem("用户账号", userDTO.getUserName()));
            info.add(buildInfoItem("审批状态", dictType.get(task.getFlowStatus())));
            info.add(buildInfoItem("审批耗时", DateUtils.getTimeDifference(task.getUpdateTime(), task.getCreateTime())));
            info.add(buildInfoItem("办理时间", DateUtils.formatDateTime(task.getUpdateTime())));
        }
    }

    /**
     * 构建单条提示内容对象 InfoItem，用于悬浮窗显示（key: value）
     *
     * @param key   字段名（作为前缀）
     * @param value 字段值
     * @return 提示项对象
     */
    private PromptContent.InfoItem buildInfoItem(String key, String value) {
        return new PromptContent.InfoItem()
            // 前缀
            .setPrefix(key + ": ")
            // 前缀样式
            .setPrefixStyle(Map.of(
                "textAlign", "right",
                "color", "#444",
                "userSelect", "none",
                "display", "inline-block",
                "width", "100px",
                "paddingRight", "8px",
                "fontWeight", "500",
                "fontSize", "14px",
                "lineHeight", "24px",
                "verticalAlign", "middle"
            ))
            // 内容
            .setContent(value)
            // 内容样式
            .setContentStyle(Map.of(
                "backgroundColor", "#f7faff",
                "color", "#005cbf",
                "padding", "4px 8px",
                "fontSize", "14px",
                "borderRadius", "4px",
                "whiteSpace", "normal",
                "border", "1px solid #d0e5ff",
                "userSelect", "text",
                "lineHeight", "20px"
            ))
            // 行样式
            .setRowStyle(Map.of(
                "color", "#222",
                "alignItems", "center",
                "display", "flex",
                "marginBottom", "6px",
                "fontWeight", "400",
                "fontSize", "14px"
            ));
    }

    /**
     * 根据流程实例ID获取历史任务列表
     *
     * @param instanceId 流程实例ID
     * @return 历史任务列表
     */
    public List<FlowHisTask> getHisTaskGroupedByNode(Long instanceId) {
        LambdaQueryWrapper<FlowHisTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FlowHisTask::getInstanceId, instanceId)
            .eq(FlowHisTask::getNodeType, NodeType.BETWEEN.getKey())
            .orderByDesc(FlowHisTask::getUpdateTime);
        return flowHisTaskMapper.selectList(wrapper);
    }

}
