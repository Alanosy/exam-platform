package org.dromara.common.excel.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.SneakyThrows;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dromara.common.core.utils.reflect.ReflectUtils;
import org.dromara.common.excel.annotation.CellMerge;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 单元格合并处理器
 *
 * @author Lion Li
 */
public class CellMergeHandler {

    private final boolean hasTitle;
    private int rowIndex;

    private CellMergeHandler(final boolean hasTitle) {
        this.hasTitle = hasTitle;
        // 行合并开始下标
        this.rowIndex = hasTitle ? 1 : 0;
    }

    @SneakyThrows
    public List<CellRangeAddress> handle(List<?> list) {
        List<CellRangeAddress> cellList = new ArrayList<>();
        if (CollUtil.isEmpty(list)) {
            return cellList;
        }
        Class<?> clazz = list.get(0).getClass();
        boolean annotationPresent = clazz.isAnnotationPresent(ExcelIgnoreUnannotated.class);
        Field[] fields = ReflectUtils.getFields(clazz, field -> {
            if ("serialVersionUID".equals(field.getName())) {
                return false;
            }
            if (field.isAnnotationPresent(ExcelIgnore.class)) {
                return false;
            }
            return !annotationPresent || field.isAnnotationPresent(ExcelProperty.class);
        });

        // 有注解的字段
        List<Field> mergeFields = new ArrayList<>();
        List<Integer> mergeFieldsIndex = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.isAnnotationPresent(CellMerge.class)) {
                CellMerge cm = field.getAnnotation(CellMerge.class);
                mergeFields.add(field);
                mergeFieldsIndex.add(cm.index() == -1 ? i : cm.index());
                if (hasTitle) {
                    ExcelProperty property = field.getAnnotation(ExcelProperty.class);
                    rowIndex = Math.max(rowIndex, property.value().length);
                }
            }
        }

        Map<Field, RepeatCell> map = new HashMap<>();
        // 生成两两合并单元格
        for (int i = 0; i < list.size(); i++) {
            Object rowObj = list.get(i);
            for (int j = 0; j < mergeFields.size(); j++) {
                Field field = mergeFields.get(j);
                Object val = ReflectUtils.invokeGetter(rowObj, field.getName());

                int colNum = mergeFieldsIndex.get(j);
                if (!map.containsKey(field)) {
                    map.put(field, new RepeatCell(val, i));
                } else {
                    RepeatCell repeatCell = map.get(field);
                    Object cellValue = repeatCell.value();
                    if (cellValue == null || "".equals(cellValue)) {
                        // 空值跳过不合并
                        continue;
                    }

                    if (!cellValue.equals(val)) {
                        if ((i - repeatCell.current() > 1)) {
                            cellList.add(new CellRangeAddress(repeatCell.current() + rowIndex, i + rowIndex - 1, colNum, colNum));
                        }
                        map.put(field, new RepeatCell(val, i));
                    } else if (i == list.size() - 1) {
                        if (!isMerge(list, i, field)) {
                            // 如果最后一行不能合并，检查之前的数据是否需要合并
                            if (i - repeatCell.current() > 1) {
                                cellList.add(new CellRangeAddress(repeatCell.current() + rowIndex, i + rowIndex - 1, colNum, colNum));
                            }
                        } else if (i > repeatCell.current()) {
                            // 如果最后一行可以合并，则直接合并到最后
                            cellList.add(new CellRangeAddress(repeatCell.current() + rowIndex, i + rowIndex, colNum, colNum));
                        }
                    } else if (!isMerge(list, i, field)) {
                        if ((i - repeatCell.current() > 1)) {
                            cellList.add(new CellRangeAddress(repeatCell.current() + rowIndex, i + rowIndex - 1, colNum, colNum));
                        }
                        map.put(field, new RepeatCell(val, i));
                    }
                }
            }
        }
        return cellList;
    }

    private boolean isMerge(List<?> list, int i, Field field) {
        boolean isMerge = true;
        CellMerge cm = field.getAnnotation(CellMerge.class);
        final String[] mergeBy = cm.mergeBy();
        if (StrUtil.isAllNotBlank(mergeBy)) {
            //比对当前list(i)和list(i - 1)的各个属性值一一比对 如果全为真 则为真
            for (String fieldName : mergeBy) {
                final Object valCurrent = ReflectUtil.getFieldValue(list.get(i), fieldName);
                final Object valPre = ReflectUtil.getFieldValue(list.get(i - 1), fieldName);
                if (!Objects.equals(valPre, valCurrent)) {
                    //依赖字段如有任一不等值,则标记为不可合并
                    isMerge = false;
                }
            }
        }
        return isMerge;
    }

    record RepeatCell(Object value, int current) {}

    /**
     * 创建一个单元格合并处理器实例
     *
     * @param hasTitle 是否合并标题
     * @return 单元格合并处理器
     */
    public static CellMergeHandler of(final boolean hasTitle) {
        return new CellMergeHandler(hasTitle);
    }

    /**
     * 创建一个单元格合并处理器实例（默认不合并标题）
     *
     * @return 单元格合并处理器
     */
    public static CellMergeHandler of() {
        return new CellMergeHandler(false);
    }

}
