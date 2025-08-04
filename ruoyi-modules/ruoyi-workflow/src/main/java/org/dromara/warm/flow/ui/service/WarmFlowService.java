/*
 *    Copyright 2024-2025, Warm-Flow (290631660@qq.com).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.dromara.warm.flow.ui.service;

import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.config.WarmFlow;
import org.dromara.warm.flow.core.dto.*;
import org.dromara.warm.flow.core.entity.Form;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.enums.FormCustomEnum;
import org.dromara.warm.flow.core.enums.ModeEnum;
import org.dromara.warm.flow.core.exception.FlowException;
import org.dromara.warm.flow.core.invoker.FrameInvoker;
import org.dromara.warm.flow.core.utils.ExceptionUtil;
import org.dromara.warm.flow.core.utils.StreamUtils;
import org.dromara.warm.flow.core.utils.StringUtils;
import org.dromara.warm.flow.ui.dto.HandlerFeedBackDto;
import org.dromara.warm.flow.ui.dto.HandlerQuery;
import org.dromara.warm.flow.ui.utils.TreeUtil;
import org.dromara.warm.flow.ui.vo.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设计器Controller 可选择是否放行，放行可与业务系统共享权限，主要是用来访问业务系统数据
 *
 * @author warm
 */
@Slf4j
public class WarmFlowService {

    /**
     * 返回流程定义的配置
     * @return ApiResult<WarmFlowVo>
     */
    public static ApiResult<WarmFlowVo> config() {
        WarmFlowVo warmFlowVo = new WarmFlowVo();
        WarmFlow warmFlow = FlowEngine.getFlowConfig();
        // 获取tokenName
        String tokenName = warmFlow.getTokenName();
        if (StringUtils.isEmpty(tokenName)) {
            return ApiResult.fail("未配置tokenName");
        }
        String[] tokenNames = tokenName.split(",");
        List<String> tokenNameList = Arrays.stream(tokenNames).filter(StringUtils::isNotEmpty)
                .map(String::trim).collect(Collectors.toList());
        warmFlowVo.setTokenNameList(tokenNameList);

        return ApiResult.ok(warmFlowVo);
    }

    /**
     * 保存流程json字符串
     *
     * @param defJson 流程数据集合
     * @return ApiResult<Void>
     * @throws Exception 异常
     * @author xiarg
     * @since 2024/10/29 16:31
     */
    public static ApiResult<Void> saveJson(DefJson defJson) throws Exception {
        FlowEngine.defService().saveDef(defJson);
        return ApiResult.ok();
    }

    /**
     * 获取流程定义数据(包含节点和跳转)
     *
     * @param id 流程定义id
     * @return ApiResult<DefVo>
     * @author xiarg
     * @since 2024/10/29 16:31
     */
    public static ApiResult<DefJson> queryDef(Long id) {
        try {
            DefJson defJson;
            if (id == null) {
                defJson = new DefJson()
                        .setModelValue(ModeEnum.CLASSICS.name())
                        .setFormCustom(FormCustomEnum.N.name());
            } else {
                defJson = FlowEngine.defService().queryDesign(id);
            }
            CategoryService categoryService = FrameInvoker.getBean(CategoryService.class);
            if (categoryService != null) {
                List<Tree> treeList = categoryService.queryCategory();
                defJson.setCategoryList(TreeUtil.buildTree(treeList));
            }
            return ApiResult.ok(defJson);
        } catch (Exception e) {
            log.error("获取流程json字符串", e);
            throw new FlowException(ExceptionUtil.handleMsg("获取流程json字符串失败", e));
        }
    }

    /**
     * 获取流程图
     *
     * @param id 流程实例id
     * @return ApiResult<DefJson>
     */
    public static ApiResult<DefJson> queryFlowChart(Long id) {
        try {
            Instance instance = FlowEngine.insService().getById(id);
            String defJsonStr = instance.getDefJson();
            DefJson defJson = FlowEngine.jsonConvert.strToBean(defJsonStr, DefJson.class);
            if (StringUtils.isEmpty(defJson.getModelValue())) {
                defJson.setModelValue(ModeEnum.CLASSICS.name());
            }
            defJson.setInstance(instance);

            // 获取流程图三原色
            defJson.setChartStatusColor(FlowEngine.chartService().getChartRgb());
            // 是否显示流程图顶部文字
            defJson.setTopTextShow(FlowEngine.getFlowConfig().isTopTextShow());
            // 需要业务系统实现该接口
            ChartExtService chartExtService = FrameInvoker.getBean(ChartExtService.class);
            if (chartExtService != null) {
                chartExtService.initPromptContent(defJson);
                chartExtService.execute(defJson);
            }

            return ApiResult.ok(defJson);
        } catch (Exception e) {
            log.error("获取流程图", e);
            throw new FlowException(ExceptionUtil.handleMsg("获取流程图失败", e));
        }
    }

    /**
     * 办理人权限设置列表tabs页签
     * @return List<String>
     */
    public static ApiResult<List<String>> handlerType() {
        try {
            // 需要业务系统实现该接口
            HandlerSelectService handlerSelectService = FrameInvoker.getBean(HandlerSelectService.class);
            if (handlerSelectService == null) {
                return ApiResult.ok(Collections.emptyList());
            }
            List<String> handlerType = handlerSelectService.getHandlerType();
            return ApiResult.ok(handlerType);
        } catch (Exception e) {
            log.error("办理人权限设置列表tabs页签异常", e);
            throw new FlowException(ExceptionUtil.handleMsg("办理人权限设置列表tabs页签失败", e));
        }
    }

    /**
     * 办理人权限设置列表结果
     * @return HandlerSelectVo
     */
    public static ApiResult<HandlerSelectVo> handlerResult(HandlerQuery query) {
        try {
            // 需要业务系统实现该接口
            HandlerSelectService handlerSelectService = FrameInvoker.getBean(HandlerSelectService.class);
            if (handlerSelectService == null) {
                return ApiResult.ok(new HandlerSelectVo());
            }
            HandlerSelectVo handlerSelectVo = handlerSelectService.getHandlerSelect(query);
            return ApiResult.ok(handlerSelectVo);
        } catch (Exception e) {
            log.error("办理人权限设置列表结果异常", e);
            throw new FlowException(ExceptionUtil.handleMsg("办理人权限设置列表结果失败", e));
        }
    }

    /**
     * 办理人权限名称回显
     * @return HandlerSelectVo
     */
    public static ApiResult<List<HandlerFeedBackVo>> handlerFeedback(HandlerFeedBackDto handlerFeedBackDto) {
        try {
            // 需要业务系统实现该接口
            HandlerSelectService handlerSelectService = FrameInvoker.getBean(HandlerSelectService.class);
            if (handlerSelectService == null) {
                List<HandlerFeedBackVo> handlerFeedBackVos = StreamUtils.toList(handlerFeedBackDto.getStorageIds(),
                        storageId -> new HandlerFeedBackVo(storageId, null));
                return ApiResult.ok(handlerFeedBackVos);
            }
            List<HandlerFeedBackVo> handlerFeedBackVos = handlerSelectService.handlerFeedback(handlerFeedBackDto.getStorageIds());
            return ApiResult.ok(handlerFeedBackVos);
        } catch (Exception e) {
            log.error("办理人权限名称回显", e);
            throw new FlowException(ExceptionUtil.handleMsg("办理人权限名称回显", e));
        }
    }

    /**
     * 办理人选择项
     * @return List<Dict>
     */
    public static ApiResult<List<Dict>> handlerDict() {
        try {
            // 需要业务系统实现该接口
            HandlerDictService handlerDictService = FrameInvoker.getBean(HandlerDictService.class);
            if (handlerDictService == null) {
                List<Dict> dictList = new ArrayList<>();
                Dict dict = new Dict();
                dict.setLabel("默认表达式");
                dict.setValue("${handler}");
                Dict dict1 = new Dict();
                dict1.setLabel("spel表达式");
                dict1.setValue("#{@user.evalVar(#handler)}");
                Dict dict2 = new Dict();
                dict2.setLabel("其他");
                dict2.setValue("");
                dictList.add(dict);
                dictList.add(dict1);
                dictList.add(dict2);

                return ApiResult.ok(dictList);
            }
            return ApiResult.ok(handlerDictService.getHandlerDict());
        } catch (Exception e) {
            log.error("办理人权限设置列表结果异常", e);
            throw new FlowException(ExceptionUtil.handleMsg("办理人权限设置列表结果失败", e));
        }
    }

    /**
     * 已发布表单列表 该接口不需要业务系统实现
     */
    public static ApiResult<List<Form>> publishedForm() {
        try {
            return ApiResult.ok(FlowEngine.formService().list(FlowEngine.newForm().setIsPublish(1)));
        } catch (Exception e) {
            log.error("已发布表单列表异常", e);
            throw new FlowException(ExceptionUtil.handleMsg("已发布表单列表异常", e));
        }
    }

    /**
     * 读取表单内容
     * @param id
     * @return
     */
    public static ApiResult<String> getFormContent(Long id) {
        try {return ApiResult.ok(FlowEngine.formService().getById(id).getFormContent());
        } catch (Exception e) {
            log.error("获取表单内容字符串", e);
            throw new FlowException(ExceptionUtil.handleMsg("获取表单内容字符串失败", e));
        }
    }

    /**
     * 保存表单内容,该接口不需要系统实现
     * @param flowDto
     * @return
     */
    public static ApiResult<Void> saveFormContent(FlowDto flowDto) {
        FlowEngine.formService().saveContent(flowDto.getId(), flowDto.getFormContent());
        return ApiResult.ok();
    }


    /**
     * 根据任务id获取待办任务表单及数据
     *
     * @param taskId 当前任务id
     * @return {@link ApiResult<FlowDto>}
     * @author liangli
     * @date 2024/8/21 17:08
     **/
    public static ApiResult<FlowDto> load(Long taskId) {
        FlowParams flowParams = FlowParams.build();

        return ApiResult.ok(FlowEngine.taskService().load(taskId, flowParams));
    }

    /**
     * 根据任务id获取已办任务表单及数据
     *
     * @param hisTaskId
     * @return
     */
    public static ApiResult<FlowDto> hisLoad(Long hisTaskId) {
        FlowParams flowParams = FlowParams.build();

        return ApiResult.ok(FlowEngine.taskService().hisLoad(hisTaskId, flowParams));
    }

    /**
     * 通用表单流程审批接口
     *
     * @param formData
     * @param taskId
     * @param skipType
     * @param message
     * @param nodeCode
     * @return
     */
    public static ApiResult<Instance> handle(Map<String, Object> formData, Long taskId, String skipType
            , String message, String nodeCode) {
        FlowParams flowParams = FlowParams.build()
                .skipType(skipType)
                .nodeCode(nodeCode)
                .message(message);

        flowParams.formData(formData);

        return ApiResult.ok(FlowEngine.taskService().skip(taskId, flowParams));
    }

    /**
     * 获取节点扩展属性
     * @return List<NodeExt>
     */
    public static ApiResult<List<NodeExt>> nodeExt() {
        try {
            // 需要业务系统实现该接口
            NodeExtService nodeExtService = FrameInvoker.getBean(NodeExtService.class);
            if (nodeExtService == null) {
                return ApiResult.ok(Collections.emptyList());
            }
            List<NodeExt> nodeExts = nodeExtService.getNodeExt();
            return ApiResult.ok(nodeExts);
        } catch (Exception e) {
            log.error("获取节点扩展属性", e);
            throw new FlowException(ExceptionUtil.handleMsg("获取节点扩展属性失败", e));
        }
    }

}
