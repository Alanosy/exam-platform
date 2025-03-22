package cn.org.alan.exam.model.form.question;

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
    @ExcelImport(value = "选项一内容")
    private String option1;
    @ExcelImport(value = "选项一是否正确")
    private Integer righted1;
    @ExcelImport(value = "选项二内容")
    private String option2;
    @ExcelImport(value = "选项二是否正确")
    private Integer righted2;
    @ExcelImport(value = "选项三内容")
    private String option3;
    @ExcelImport(value = "选项三是否正确")
    private Integer righted3;
    @ExcelImport(value = "选项四内容")
    private String option4;
    @ExcelImport(value = "选项四是否正确")
    private Integer righted4;
    @ExcelImport(value = "选项五内容")
    private String option5;
    @ExcelImport(value = "选项五是否正确")
    private Integer righted5;
    @ExcelImport(value = "选项六内容")
    private String option6;
    @ExcelImport(value = "选项六是否正确")
    private Integer righted6;


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
            List<Option> options = new ArrayList<>();
            if (questionExcelFrom.getOption1() != null && !questionExcelFrom.getOption1().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption1());
                option.setIsRight(questionExcelFrom.getRighted1());
                options.add(option);
            }
            if (questionExcelFrom.getOption2() != null && !questionExcelFrom.getOption2().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption2());
                option.setIsRight(questionExcelFrom.getRighted2());
                options.add(option);
            }
            if (questionExcelFrom.getOption3() != null && !questionExcelFrom.getOption3().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption3());
                option.setIsRight(questionExcelFrom.getRighted3());
                options.add(option);
            }
            if (questionExcelFrom.getOption4() != null && !questionExcelFrom.getOption4().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption4());
                option.setIsRight(questionExcelFrom.getRighted4());
                options.add(option);
            }
            if (questionExcelFrom.getOption5() != null && !questionExcelFrom.getOption5().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption5());
                option.setIsRight(questionExcelFrom.getRighted5());
                options.add(option);
            }
            if (questionExcelFrom.getOption6() != null && !questionExcelFrom.getOption6().isEmpty()) {
                Option option = new Option();
                option.setContent(questionExcelFrom.getOption6());
                option.setIsRight(questionExcelFrom.getRighted6());
                options.add(option);
            }

            questionFrom.setOptions(options);
            list.add(questionFrom);
        }
        return list;
    }

}
