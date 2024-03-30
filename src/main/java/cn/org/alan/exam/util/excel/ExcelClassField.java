package cn.org.alan.exam.util.excel;


import java.util.LinkedHashMap;

/**
 * @author sunnyzyq
 * @date 2021/12/17
 */
public class ExcelClassField {

    /** 字段名称 */
    private String fieldName;

    /** 表头名称 */
    private String name;

    /** 映射关系 */
    private LinkedHashMap<String, String> kvMap;

    /** 示例值 */
    private Object example;

    /** 排序 */
    private int sort;

    /** 是否为注解字段：0-否，1-是 */
    private int hasAnnotation;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedHashMap<String, String> getKvMap() {
        return kvMap;
    }

    public void setKvMap(LinkedHashMap<String, String> kvMap) {
        this.kvMap = kvMap;
    }

    public Object getExample() {
        return example;
    }

    public void setExample(Object example) {
        this.example = example;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getHasAnnotation() {
        return hasAnnotation;
    }

    public void setHasAnnotation(int hasAnnotation) {
        this.hasAnnotation = hasAnnotation;
    }

}
