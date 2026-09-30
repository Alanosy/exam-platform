package cn.org.alan.exam.model.form.question;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.utils.excel.ExcelImport;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/8 10:21
 */
@Data
public class QuestionExcelFrom {
    @ExcelImport(value = "试题类型",required = true)
    private Integer quType;
    @ExcelImport(value = "题干",required = true,unique = true)
    private String content;
    @ExcelImport(value = "解析")
    private String analysis;
    
    // 添加题干图片字段
    @ExcelImport(value = "题干图片")
    private String image;
    
    @ExcelImport(value = "选项一内容")
    private String option1;
    @ExcelImport(value = "选项一是否正确")
    private Integer righted1;
    // 添加选项一图片字段
    @ExcelImport(value = "选项一图片")
    private String image1;
    
    @ExcelImport(value = "选项二内容")
    private String option2;
    @ExcelImport(value = "选项二是否正确")
    private Integer righted2;
    // 添加选项二图片字段
    @ExcelImport(value = "选项二图片")
    private String image2;
    
    @ExcelImport(value = "选项三内容")
    private String option3;
    @ExcelImport(value = "选项三是否正确")
    private Integer righted3;
    // 添加选项三图片字段
    @ExcelImport(value = "选项三图片")
    private String image3;
    
    @ExcelImport(value = "选项四内容")
    private String option4;
    @ExcelImport(value = "选项四是否正确")
    private Integer righted4;
    // 添加选项四图片字段
    @ExcelImport(value = "选项四图片")
    private String image4;
    
    @ExcelImport(value = "选项五内容")
    private String option5;
    @ExcelImport(value = "选项五是否正确")
    private Integer righted5;
    // 添加选项五图片字段
    @ExcelImport(value = "选项五图片")
    private String image5;
    
    @ExcelImport(value = "选项六内容")
    private String option6;
    @ExcelImport(value = "选项六是否正确")
    private Integer righted6;
    // 添加选项六图片字段
    @ExcelImport(value = "选项六图片")
    private String image6;


    /**
     * 类型转换
     * @param questionExcelFroms 表格获取类型转换成from类型
     * @return 转换后的结果
     */
    public static List<QuestionFrom> converterQuestionFrom(List<QuestionExcelFrom> questionExcelFroms){
        List<QuestionFrom> list = new ArrayList<>(300);
        for (QuestionExcelFrom questionExcelFrom : questionExcelFroms) {
            QuestionFrom questionFrom = new QuestionFrom();
            questionFrom.setContent(questionExcelFrom.getContent());
            questionFrom.setQuType(questionExcelFrom.getQuType());
            questionFrom.setAnalysis(questionExcelFrom.getAnalysis());
            // 设置题干图片
            questionFrom.setImage(questionExcelFrom.getImage());
            
            List<Option> options = new ArrayList<>();
            
            // 验证选项内容和是否正确的一致性
            if (questionExcelFrom.getQuType() != 4) { // 非简答题需要验证选项
                // 检查选项一
                if (questionExcelFrom.getOption1() != null && !questionExcelFrom.getOption1().isEmpty() 
                    && questionExcelFrom.getRighted1() == null) {
                    String errorMsg = String.format("导入错误 - 题干为「%s」的试题：选项一内容存在但未设置是否正确，请检查Excel文件中对应行的「选项一是否正确」列", 
                            questionExcelFrom.getContent());
                    throw new ServiceRuntimeException(errorMsg);
                }
                // 检查选项二
                if (questionExcelFrom.getOption2() != null && !questionExcelFrom.getOption2().isEmpty() 
                    && questionExcelFrom.getRighted2() == null) {
                    String errorMsg = String.format("导入错误 - 题干为「%s」的试题：选项二内容存在但未设置是否正确，请检查Excel文件中对应行的「选项二是否正确」列", 
                            questionExcelFrom.getContent());
                    throw new ServiceRuntimeException(errorMsg);
                }
                // 检查选项三
                if (questionExcelFrom.getOption3() != null && !questionExcelFrom.getOption3().isEmpty() 
                    && questionExcelFrom.getRighted3() == null) {
                    String errorMsg = String.format("导入错误 - 题干为「%s」的试题：选项三内容存在但未设置是否正确，请检查Excel文件中对应行的「选项三是否正确」列", 
                            questionExcelFrom.getContent());
                    throw new ServiceRuntimeException(errorMsg);
                }
                // 检查选项四
                if (questionExcelFrom.getOption4() != null && !questionExcelFrom.getOption4().isEmpty() 
                    && questionExcelFrom.getRighted4() == null) {
                    String errorMsg = String.format("导入错误 - 题干为「%s」的试题：选项四内容存在但未设置是否正确，请检查Excel文件中对应行的「选项四是否正确」列", 
                            questionExcelFrom.getContent());
                    throw new ServiceRuntimeException(errorMsg);
                }
                // 检查选项五
                if (questionExcelFrom.getOption5() != null && !questionExcelFrom.getOption5().isEmpty() 
                    && questionExcelFrom.getRighted5() == null) {
                    String errorMsg = String.format("导入错误 - 题干为「%s」的试题：选项五内容存在但未设置是否正确，请检查Excel文件中对应行的「选项五是否正确」列", 
                            questionExcelFrom.getContent());
                    throw new ServiceRuntimeException(errorMsg);
                }
                // 检查选项六
                if (questionExcelFrom.getOption6() != null && !questionExcelFrom.getOption6().isEmpty() 
                    && questionExcelFrom.getRighted6() == null) {
                    String errorMsg = String.format("导入错误 - 题干为「%s」的试题：选项六内容存在但未设置是否正确，请检查Excel文件中对应行的「选项六是否正确」列", 
                            questionExcelFrom.getContent());
                    throw new ServiceRuntimeException(errorMsg);
                }
            }
            
            if (questionExcelFrom.getOption1() != null && !questionExcelFrom.getOption1().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption1());
                option.setIsRight(questionExcelFrom.getRighted1());
                // 设置选项一图片
                option.setImage(questionExcelFrom.getImage1());
                options.add(option);
            }
            if (questionExcelFrom.getOption2() != null && !questionExcelFrom.getOption2().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption2());
                option.setIsRight(questionExcelFrom.getRighted2());
                // 设置选项二图片
                option.setImage(questionExcelFrom.getImage2());
                options.add(option);
            }
            if (questionExcelFrom.getOption3() != null && !questionExcelFrom.getOption3().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption3());
                option.setIsRight(questionExcelFrom.getRighted3());
                // 设置选项三图片
                option.setImage(questionExcelFrom.getImage3());
                options.add(option);
            }
            if (questionExcelFrom.getOption4() != null && !questionExcelFrom.getOption4().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption4());
                option.setIsRight(questionExcelFrom.getRighted4());
                // 设置选项四图片
                option.setImage(questionExcelFrom.getImage4());
                options.add(option);
            }
            if (questionExcelFrom.getOption5() != null && !questionExcelFrom.getOption5().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption5());
                option.setIsRight(questionExcelFrom.getRighted5());
                // 设置选项五图片
                option.setImage(questionExcelFrom.getImage5());
                options.add(option);
            }
            if (questionExcelFrom.getOption6() != null && !questionExcelFrom.getOption6().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption6());
                option.setIsRight(questionExcelFrom.getRighted6());
                // 设置选项六图片
                option.setImage(questionExcelFrom.getImage6());
                options.add(option);
            }

            questionFrom.setOptions(options);
            list.add(questionFrom);
        }
        return list;
    }
}
