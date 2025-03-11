package org.dromara.workflow.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.api.RemoteDictService;
import org.dromara.system.api.domain.vo.RemoteDictTypeVo;
import org.dromara.warm.flow.ui.service.NodeExtService;
import org.dromara.warm.flow.ui.vo.NodeExt;
import org.dromara.workflow.common.ConditionalOnEnable;
import org.dromara.workflow.common.enums.ButtonPermissionEnum;
import org.dromara.workflow.common.enums.NodeExtEnum;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 流程设计器-节点扩展属性
 *
 * @author AprilWind
 */
@ConditionalOnEnable
@Slf4j
@RequiredArgsConstructor
@Service
public class FlwNodeExtServiceImpl implements NodeExtService {

    /**
     * 权限页code
     */
    private static final String PERMISSION_TAB = "wf_button_tab";

    /**
     * 权限页名称
     */
    private static final String PERMISSION_TAB_NAME = "权限";

    /**
     * 枚举类型标识
     */
    private static final String ENUM_TYPE_PREFIX = "enum:";

    /**
     * 基础设置
     */
    private static final int TYPE_BASE_SETTING = 1;

    /**
     * 新页签
     */
    private static final int TYPE_NEW_TAB = 2;

    /**
     * 存储不同 dictType 对应的配置信息
     */
    private static final Map<String, Map<String, Object>> CHILD_NODE_MAP = new HashMap<>();

    static {
        CHILD_NODE_MAP.put(ButtonPermissionEnum.class.getName(),
            Map.of("label", "权限按钮", "type", 4, "must", false, "multiple", true));
    }

    @DubboReference
    private RemoteDictService remoteDictService;

    /**
     * 获取节点扩展属性
     *
     * @return 结果
     */
    @Override
    public List<NodeExt> getNodeExt() {
        List<NodeExt> nodeExtList = new ArrayList<>();
        // 构建按钮权限页面
        nodeExtList.add(buildNodeExt(PERMISSION_TAB, PERMISSION_TAB_NAME, TYPE_NEW_TAB,
            ENUM_TYPE_PREFIX + ButtonPermissionEnum.class.getName()));
        return nodeExtList;
    }

    /**
     * 构建一个 NodeExt 对象
     *
     * @param code        编码，此json中唯一
     * @param name        名称，如果type为新页签时，作为页签名称
     * @param type        节点类型，1：基础设置，2：新页签
     * @param sourceTypes 字典/枚举类型来源（逗号分隔）
     * @return 返回构建好的 NodeExt 对象
     */
    private NodeExt buildNodeExt(String code, String name, int type, String sourceTypes) {
        NodeExt nodeExt = new NodeExt();
        nodeExt.setCode(code);
        nodeExt.setType(type);
        nodeExt.setName(name);
        nodeExt.setChilds(StringUtils.splitList(sourceTypes)
            .stream().map(this::buildChildNode)
            .filter(ObjectUtil::isNotNull)
            .toList()
        );
        return nodeExt;
    }

    /**
     * 构建一个 ChildNode 对象
     *
     * @param sourceType 字典类型
     * @return 返回构建好的 ChildNode 对象
     */
    private NodeExt.ChildNode buildChildNode(String sourceType) {
        return sourceType.startsWith(ENUM_TYPE_PREFIX) ?
            buildChildNodeFromEnum(sourceType.substring(ENUM_TYPE_PREFIX.length())) : buildChildNodeFromDict(sourceType);
    }

    /**
     * 根据枚举构建一个 ChildNode 对象
     *
     * @param enumClassName 枚举名称
     * @return 返回构建好的 ChildNode 对象
     */
    private NodeExt.ChildNode buildChildNodeFromEnum(String enumClassName) {
        try {
            Class<?> enumClass = Class.forName(enumClassName);
            if (!enumClass.isEnum()) {
                return null;
            }
            NodeExt.ChildNode childNode = buildChildNodeMap(enumClassName);
            // 编码，此json中唯
            childNode.setCode(ENUM_TYPE_PREFIX + enumClassName);
            // 字典，下拉框和复选框时用到
            childNode.setDict(Arrays.stream(enumClass.getEnumConstants())
                .filter(NodeExtEnum.class::isInstance)
                .map(NodeExtEnum.class::cast)
                .map(x ->
                    new NodeExt.DictItem(x.getLabel(), x.getValue(), x.isSelected())
                ).toList());
            return childNode;
        } catch (ClassNotFoundException e) {
            log.error("Enum class not found: {}", enumClassName, e);
        }
        return null;
    }

    /**
     * 根据字典构建一个 ChildNode 对象
     *
     * @param dictType 字典类型
     * @return 返回构建好的 ChildNode 对象
     */
    private NodeExt.ChildNode buildChildNodeFromDict(String dictType) {
        RemoteDictTypeVo dictTypeDTO = remoteDictService.selectDictTypeByType(dictType);
        if (ObjectUtil.isNull(dictTypeDTO)) {
            return null;
        }
        NodeExt.ChildNode childNode = buildChildNodeMap(dictType);
        // 编码，此json中唯一
        childNode.setCode(dictType);
        // label名称
        childNode.setLabel(dictTypeDTO.getDictName());
        // 描述
        childNode.setDesc(dictTypeDTO.getRemark());
        // 字典，下拉框和复选框时用到
        childNode.setDict(remoteDictService.selectDictDataByType(dictType)
            .stream().map(x ->
                new NodeExt.DictItem(x.getDictLabel(), x.getDictValue(), Convert.toBool(x.getIsDefault(), false))
            ).toList());
        return childNode;
    }

    /**
     * 根据 CHILD_NODE_MAP 中的配置信息，构建一个基本的 ChildNode 对象
     * 该方法用于设置 ChildNode 的常规属性，例如 label、type、是否必填、是否多选等
     *
     * @param key CHILD_NODE_MAP 的 key
     * @return 返回构建好的 ChildNode 对象
     */
    private NodeExt.ChildNode buildChildNodeMap(String key) {
        NodeExt.ChildNode childNode = new NodeExt.ChildNode();
        Map<String, Object> map = CHILD_NODE_MAP.get(key);
        // label名称
        childNode.setLabel((String) map.get("label"));
        // 1：输入框 2：输入框 3：下拉框 4：选择框
        childNode.setType(Convert.toInt(map.get("type"), 1));
        // 是否必填
        childNode.setMust(Convert.toBool(map.get("must"), false));
        // 是否多选
        childNode.setMultiple(Convert.toBool(map.get("multiple"), true));
        return childNode;
    }

}
