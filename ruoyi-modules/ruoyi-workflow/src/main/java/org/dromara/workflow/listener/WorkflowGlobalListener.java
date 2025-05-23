package org.dromara.workflow.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.enums.BusinessStatusEnum;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.entity.Definition;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.entity.Task;
import org.dromara.warm.flow.core.listener.GlobalListener;
import org.dromara.warm.flow.core.listener.ListenerVariable;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.dromara.workflow.common.ConditionalOnEnable;
import org.dromara.workflow.common.constant.FlowConstant;
import org.dromara.workflow.common.enums.TaskStatusEnum;
import org.dromara.workflow.domain.bo.FlowCopyBo;
import org.dromara.workflow.handler.FlowProcessEventHandler;
import org.dromara.workflow.service.IFlwCommonService;
import org.dromara.workflow.service.IFlwInstanceService;
import org.dromara.workflow.service.IFlwTaskService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局任务办理监听
 *
 * @author may
 */
@ConditionalOnEnable
@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowGlobalListener implements GlobalListener {

    private final IFlwTaskService flwTaskService;
    private final IFlwInstanceService instanceService;
    private final FlowProcessEventHandler flowProcessEventHandler;
    private final IFlwCommonService flwCommonService;

    /**
     * 创建监听器，任务创建时执行
     *
     * @param listenerVariable 监听器变量
     */
    @Override
    public void create(ListenerVariable listenerVariable) {
        Instance instance = listenerVariable.getInstance();
        Definition definition = listenerVariable.getDefinition();
        FlowParams flowParams = listenerVariable.getFlowParams();
        Map<String, Object> variable = flowParams.getVariable();
        Task task = listenerVariable.getTask();
        if (task != null) {
            // 判断流程状态（发布审批中事件）
            flowProcessEventHandler.processCreateTaskHandler(definition.getFlowCode(), instance, task.getId());
        }
        Boolean submit = MapUtil.getBool(variable, FlowConstant.SUBMIT);
        if (submit != null && submit) {
            flowProcessEventHandler.processHandler(definition.getFlowCode(), instance, instance.getFlowStatus(), variable, true);
        }
        variable.remove(FlowConstant.SUBMIT);
        flowParams.variable(variable);
    }

    /**
     * 开始监听器，任务开始办理时执行
     *
     * @param listenerVariable 监听器变量
     */
    @Override
    public void start(ListenerVariable listenerVariable) {
    }

    /**
     * 分派监听器，动态修改代办任务信息
     *
     * @param listenerVariable 监听器变量
     */
    @Override
    public void assignment(ListenerVariable listenerVariable) {
        Map<String, Object> variable = listenerVariable.getVariable();
        List<Task> nextTasks = listenerVariable.getNextTasks();
        FlowParams flowParams = listenerVariable.getFlowParams();
        Definition definition = listenerVariable.getDefinition();
        Instance instance = listenerVariable.getInstance();
        String applyNodeCode = flwCommonService.applyNodeCode(definition.getId());
        for (Task flowTask : nextTasks) {
            // 如果办理或者退回并行存在需要指定办理人，则直接覆盖办理人
            if (variable.containsKey(flowTask.getNodeCode()) && (TaskStatusEnum.PASS.getStatus().equals(flowParams.getHisStatus())
                || TaskStatusEnum.BACK.getStatus().equals(flowParams.getHisStatus()))) {
                String userIds = variable.get(flowTask.getNodeCode()).toString();
                flowTask.setPermissionList(List.of(userIds.split(StringUtils.SEPARATOR)));
                variable.remove(flowTask.getNodeCode());
            } else {
                // 否则把所有的角色或者部门转成对应的用户
                List<String> permissionList = flowTask.getPermissionList();
                if (CollUtil.isNotEmpty(permissionList)) {
                    List<String> newUserList = flwCommonService.buildUser(permissionList);
                    flowTask.setPermissionList(newUserList);
                }
            }
            // 如果是申请节点，则把启动人添加到办理人
            if (flowTask.getNodeCode().equals(applyNodeCode)) {
                flowTask.setPermissionList(List.of(instance.getCreateBy()));
            }
        }
    }

    /**
     * 完成监听器，当前任务完成后执行
     *
     * @param listenerVariable 监听器变量
     */
    @Override
    public void finish(ListenerVariable listenerVariable) {
        Instance instance = listenerVariable.getInstance();
        Definition definition = listenerVariable.getDefinition();
        Map<String, Object> params = new HashMap<>();
        FlowParams flowParams = listenerVariable.getFlowParams();
        if (ObjectUtil.isNotNull(flowParams)) {
            // 历史任务扩展(通常为附件)
            params.put("hisTaskExt", flowParams.getHisTaskExt());
            // 办理人
            params.put("handler", flowParams.getHandler());
            // 办理意见
            params.put("message", flowParams.getMessage());
        }
        // 判断流程状态（发布：撤销，退回，作废，终止，已完成事件）
        String status = determineFlowStatus(instance);
        if (StringUtils.isNotBlank(status)) {
            flowProcessEventHandler.processHandler(definition.getFlowCode(), instance, status, params, false);
        }
        Map<String, Object> variable = listenerVariable.getVariable();
        // 只有办理或者退回的时候才执行消息通知和抄送
        if (TaskStatusEnum.PASS.getStatus().equals(flowParams.getHisStatus())
            || TaskStatusEnum.BACK.getStatus().equals(flowParams.getHisStatus())) {
            Task task = listenerVariable.getTask();
            List<FlowCopyBo> flowCopyList = (List<FlowCopyBo>) variable.get(FlowConstant.FLOW_COPY_LIST);
            List<String> messageType = (List<String>) variable.get(FlowConstant.MESSAGE_TYPE);
            String notice = (String) variable.get(FlowConstant.MESSAGE_NOTICE);

            // 添加抄送人
            flwTaskService.setCopy(task, flowCopyList);
            variable.remove(FlowConstant.FLOW_COPY_LIST);

            // 消息通知
            if (CollUtil.isNotEmpty(messageType)) {
                flwCommonService.sendMessage(definition.getFlowName(), instance.getId(), messageType, notice);
                variable.remove(FlowConstant.MESSAGE_TYPE);
                variable.remove(FlowConstant.MESSAGE_NOTICE);
            }
        }
    }

    /**
     * 根据流程实例确定最终状态
     *
     * @param instance 流程实例
     * @return 流程最终状态
     */
    private String determineFlowStatus(Instance instance) {
        String flowStatus = instance.getFlowStatus();
        if (StringUtils.isNotBlank(flowStatus) && BusinessStatusEnum.initialState(flowStatus)) {
            log.info("流程实例当前状态: {}", flowStatus);
            return flowStatus;
        } else {
            Long instanceId = instance.getId();
            List<FlowTask> flowTasks = flwTaskService.selectByInstId(instanceId);
            if (CollUtil.isEmpty(flowTasks)) {
                String status = BusinessStatusEnum.FINISH.getStatus();
                // 更新流程状态为已完成
                instanceService.updateStatus(instanceId, status);
                log.info("流程已结束，状态更新为: {}", status);
                return status;
            }
            return null;
        }
    }

}
