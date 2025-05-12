package org.dromara.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.resource.api.RemoteMailService;
import org.dromara.resource.api.RemoteMessageService;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.entity.Node;
import org.dromara.warm.flow.core.entity.Task;
import org.dromara.warm.flow.core.entity.User;
import org.dromara.warm.flow.core.enums.SkipType;
import org.dromara.warm.flow.core.service.NodeService;
import org.dromara.warm.flow.core.service.UserService;
import org.dromara.warm.flow.core.utils.MapUtil;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.dromara.warm.flow.orm.entity.FlowUser;
import org.dromara.workflow.common.ConditionalOnEnable;
import org.dromara.workflow.common.enums.MessageTypeEnum;
import org.dromara.workflow.common.enums.TaskAssigneeType;
import org.dromara.workflow.service.IFlwCommonService;
import org.dromara.workflow.service.IFlwTaskAssigneeService;
import org.dromara.workflow.service.IFlwTaskService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 工作流工具
 *
 * @author LionLi
 */
@ConditionalOnEnable
@Slf4j
@RequiredArgsConstructor
@Service
public class FlwCommonServiceImpl implements IFlwCommonService {

    private final UserService userService;
    private final NodeService nodeService;

    @DubboReference
    private RemoteMessageService remoteMessageService;
    @DubboReference
    private RemoteMailService remoteMailService;

    /**
     * 获取工作流用户service
     */
    @Override
    public UserService getFlowUserService() {
        return userService;
    }

    /**
     * 构建工作流用户
     *
     * @param userList 办理用户
     * @param taskId   任务ID
     * @return 用户
     */
    @Override
    public Set<User> buildUser(List<User> userList, Long taskId) {
        if (CollUtil.isEmpty(userList)) {
            return Set.of();
        }
        Set<User> list = new HashSet<>();
        Set<String> processedBySet = new HashSet<>();
        IFlwTaskAssigneeService taskAssigneeService = SpringUtils.getBean(IFlwTaskAssigneeService.class);
        Map<String, List<User>> userListMap = StreamUtils.groupByKey(userList, User::getType);
        for (Map.Entry<String, List<User>> entry : userListMap.entrySet()) {
            List<User> entryValue = entry.getValue();
            String processedBys = StreamUtils.join(entryValue, User::getProcessedBy);
            // 根据 processedBy 前缀判断处理人类型，分别获取用户列表
            List<RemoteUserVo> users = taskAssigneeService.fetchUsersByStorageId(processedBys);
            // 转换为 FlowUser 并添加到结果集合
            if (CollUtil.isNotEmpty(users)) {
                users.forEach(dto -> {
                    String processedBy = String.valueOf(dto.getUserId());
                    if (!processedBySet.contains(processedBy)) {
                        FlowUser flowUser = new FlowUser();
                        flowUser.setType(entry.getKey());
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
     * 构建工作流用户
     *
     * @param userIdList 办理用户
     * @param taskId     任务ID
     * @return 用户
     */
    @Override
    public Set<User> buildFlowUser(List<String> userIdList, Long taskId) {
        if (CollUtil.isEmpty(userIdList)) {
            return Set.of();
        }
        Set<User> list = new HashSet<>();
        Set<String> processedBySet = new HashSet<>();
        for (String userId : userIdList) {
            if (!processedBySet.contains(userId)) {
                FlowUser flowUser = new FlowUser();
                flowUser.setType(TaskAssigneeType.APPROVER.getCode());
                flowUser.setProcessedBy(String.valueOf(userId));
                flowUser.setAssociated(taskId);
                list.add(flowUser);
                processedBySet.add(String.valueOf(userId));
            }
        }
        return list;
    }

    /**
     * 发送消息
     *
     * @param flowName    流程定义名称
     * @param messageType 消息类型
     * @param message     消息内容，为空则发送默认配置的消息内容
     */
    @Override
    public void sendMessage(String flowName, Long instId, List<String> messageType, String message) {
        IFlwTaskService flwTaskService = SpringUtils.getBean(IFlwTaskService.class);
        List<RemoteUserVo> userList = new ArrayList<>();
        List<FlowTask> list = flwTaskService.selectByInstId(instId);
        if (StringUtils.isBlank(message)) {
            message = "有新的【" + flowName + "】单据已经提交至您，请您及时处理。";
        }
        for (Task task : list) {
            List<RemoteUserVo> users = flwTaskService.currentTaskAllUser(task.getId());
            if (CollUtil.isNotEmpty(users)) {
                userList.addAll(users);
            }
        }
        if (CollUtil.isNotEmpty(userList)) {
            for (String code : messageType) {
                MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByCode(code);
                if (ObjectUtil.isNotEmpty(messageTypeEnum)) {
                    switch (messageTypeEnum) {
                        case SYSTEM_MESSAGE:
                            List<Long> userIds = StreamUtils.toList(userList, RemoteUserVo::getUserId).stream().distinct().collect(Collectors.toList());
                            remoteMessageService.publishMessage(userIds, message);
                            break;
                        case EMAIL_MESSAGE:
                            remoteMailService.send(StreamUtils.join(userList, RemoteUserVo::getEmail), "单据审批提醒", message);
                            break;
                        case SMS_MESSAGE:
                            //todo 短信发送
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + messageTypeEnum);
                    }
                }
            }
        }
    }

    /**
     * 申请人节点编码
     *
     * @param definitionId 流程定义id
     * @return 申请人节点编码
     */
    @Override
    public String applyNodeCode(Long definitionId) {
        Node startNode = nodeService.getStartNode(definitionId);
        Node nextNode = nodeService.getNextNode(definitionId, startNode.getNodeCode(), null, SkipType.PASS.getKey());
        return nextNode.getNodeCode();
    }

    @Override
    public void mergeVariable(Instance instance, Map<String, Object> variable) {
        if (MapUtil.isNotEmpty(variable)) {
            String variableStr = instance.getVariable();
            Map<String, Object> deserialize = FlowEngine.jsonConvert.strToMap(variableStr);
            deserialize.putAll(variable);
            instance.setVariable(FlowEngine.jsonConvert.objToStr(deserialize));
        }
    }
}
