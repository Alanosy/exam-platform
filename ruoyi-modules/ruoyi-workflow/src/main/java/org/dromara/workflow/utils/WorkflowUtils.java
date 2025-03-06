package org.dromara.workflow.utils;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.dromara.warm.flow.core.constant.ExceptionCons;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.entity.Node;
import org.dromara.warm.flow.core.entity.User;
import org.dromara.warm.flow.core.enums.NodeType;
import org.dromara.warm.flow.core.enums.SkipType;
import org.dromara.warm.flow.core.service.NodeService;
import org.dromara.warm.flow.core.service.TaskService;
import org.dromara.warm.flow.core.service.UserService;
import org.dromara.warm.flow.core.utils.AssertUtil;
import org.dromara.warm.flow.orm.entity.FlowNode;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.dromara.warm.flow.orm.entity.FlowUser;
import org.dromara.warm.flow.orm.mapper.FlowNodeMapper;
import org.dromara.warm.flow.orm.mapper.FlowTaskMapper;
import org.dromara.workflow.service.IFlwTaskAssigneeService;
import org.dromara.workflow.service.IFlwTaskService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 工作流工具
 *
 * @author may
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowUtils {

    private static final IFlwTaskAssigneeService TASK_ASSIGNEE_SERVICE = SpringUtils.getBean(IFlwTaskAssigneeService.class);
    private static final IFlwTaskService FLW_TASK_SERVICE = SpringUtils.getBean(IFlwTaskService.class);
    private static final FlowNodeMapper FLOW_NODE_MAPPER = SpringUtils.getBean(FlowNodeMapper.class);
    private static final FlowTaskMapper FLOW_TASK_MAPPER = SpringUtils.getBean(FlowTaskMapper.class);
    private static final UserService USER_SERVICE = SpringUtils.getBean(UserService.class);
    private static final TaskService TASK_SERVICE = SpringUtils.getBean(TaskService.class);
    private static final NodeService NODE_SERVICE = SpringUtils.getBean(NodeService.class);

    /**
     * 获取工作流用户service
     */
    public static UserService getFlowUserService() {
        return USER_SERVICE;
    }

    /**
     * 构建工作流用户
     *
     * @param userList 办理用户
     * @param taskId   任务ID
     * @return 用户
     */
    public static Set<User> buildUser(List<User> userList, Long taskId) {
        if (CollUtil.isEmpty(userList)) {
            return Set.of();
        }
        Set<User> list = new HashSet<>();
        Set<String> processedBySet = new HashSet<>();
        for (User user : userList) {
            // 根据 processedBy 前缀判断处理人类型，分别获取用户列表
            List<RemoteUserVo> users = TASK_ASSIGNEE_SERVICE.fetchUsersByStorageId(user.getProcessedBy());
            // 转换为 FlowUser 并添加到结果集合
            if (CollUtil.isNotEmpty(users)) {
                users.forEach(dto -> {
                    String processedBy = String.valueOf(dto.getUserId());
                    if (!processedBySet.contains(processedBy)) {
                        FlowUser flowUser = new FlowUser();
                        flowUser.setType(user.getType());
                        flowUser.setProcessedBy(processedBy);
                        flowUser.setAssociated(taskId);
                        list.add(flowUser);
                        processedBySet.add(processedBy);
                    }
                });
            }
        }
        return list;
    }

    /**
     * 驳回
     *
     * @param message        审批意见
     * @param instanceId     流程实例id
     * @param targetNodeCode 目标节点
     * @param flowStatus     流程状态
     * @param flowHisStatus  节点操作状态
     */
    public static void backTask(String message, Long instanceId, String targetNodeCode, String flowStatus, String flowHisStatus) {
        List<FlowTask> list = FLW_TASK_SERVICE.selectByInstId(instanceId);
        if (CollUtil.isNotEmpty(list)) {
            List<FlowTask> tasks = StreamUtils.filter(list, e -> e.getNodeCode().equals(targetNodeCode));
            if (list.size() == tasks.size()) {
                return;
            }
        }
        for (FlowTask task : list) {
            List<RemoteUserVo> userList = FLW_TASK_SERVICE.currentTaskAllUser(task.getId());
            FlowParams flowParams = FlowParams.build();
            flowParams.nodeCode(targetNodeCode);
            flowParams.message(message);
            flowParams.skipType(SkipType.PASS.getKey());
            flowParams.flowStatus(flowStatus).hisStatus(flowHisStatus);
            flowParams.ignore(true);
            //解决会签没权限问题
            if (CollUtil.isNotEmpty(userList)) {
                flowParams.handler(userList.get(0).getUserId().toString());
            }
            TASK_SERVICE.skip(task.getId(), flowParams);
        }
        //解决会签多人审批问题
        backTask(message, instanceId, targetNodeCode, flowStatus, flowHisStatus);
    }

    /**
     * 申请人节点编码
     *
     * @param definitionId 流程定义id
     * @return 申请人节点编码
     */
    public static String applyNodeCode(Long definitionId) {
        //获取已发布的流程节点
        List<FlowNode> flowNodes = FLOW_NODE_MAPPER.selectList(new LambdaQueryWrapper<FlowNode>().eq(FlowNode::getDefinitionId, definitionId));
        AssertUtil.isTrue(CollUtil.isEmpty(flowNodes), ExceptionCons.NOT_PUBLISH_NODE);
        Node startNode = flowNodes.stream().filter(t -> NodeType.isStart(t.getNodeType())).findFirst().orElse(null);
        AssertUtil.isNull(startNode, ExceptionCons.LOST_START_NODE);
        Node nextNode = NODE_SERVICE.getNextNode(definitionId, startNode.getNodeCode(), null, SkipType.PASS.getKey());
        return nextNode.getNodeCode();
    }

    /**
     * 删除运行中的任务
     *
     * @param taskIds 任务id
     */
    public static void deleteRunTask(List<Long> taskIds) {
        if (CollUtil.isEmpty(taskIds)) {
            return;
        }
        USER_SERVICE.deleteByTaskIds(taskIds);
        FLOW_TASK_MAPPER.deleteByIds(taskIds);
    }
}
