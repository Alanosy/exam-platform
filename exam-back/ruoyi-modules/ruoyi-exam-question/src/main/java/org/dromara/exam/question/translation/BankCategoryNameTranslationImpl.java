package org.dromara.exam.question.translation;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.core.TranslationInterface;
import org.dromara.exam.question.domain.QuestionBankCategory;
import org.dromara.exam.question.mapper.QuestionBankCategoryMapper;
import org.springframework.stereotype.Component;

/**
 * 题库分类id转分类名称翻译实现
 *
 * @author LionLi
 */
@Slf4j
@Component
@RequiredArgsConstructor
@TranslationType(type = ExamTransConstant.BANK_CATEGORY_ID_TO_NAME)
public class BankCategoryNameTranslationImpl implements TranslationInterface<String> {

    private final QuestionBankCategoryMapper questionBankCategoryMapper;

    @Override
    public String translation(Object key, String other) {
        Long categoryId = Convert.toLong(key);
        if (ObjectUtil.isNull(categoryId)) {
            return null;
        }
        QuestionBankCategory category = questionBankCategoryMapper.selectById(categoryId);
        // 分类不存在（如已被删除）时返回 null，由前端兜底展示原始 id
        return ObjectUtil.isNull(category) ? null : category.getCategoryName();
    }
}
