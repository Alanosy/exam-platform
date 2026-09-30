package org.dromara.exam.question.translation;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.core.TranslationInterface;
import org.dromara.exam.question.domain.QuestionBank;
import org.dromara.exam.question.mapper.QuestionBankMapper;
import org.springframework.stereotype.Component;

/**
 * 题库id转题库名称翻译实现
 *
 * @author LionLi
 */
@Slf4j
@Component
@RequiredArgsConstructor
@TranslationType(type = ExamTransConstant.BANK_ID_TO_NAME)
public class BankNameTranslationImpl implements TranslationInterface<String> {

    private final QuestionBankMapper questionBankMapper;

    @Override
    public String translation(Object key, String other) {
        Long bankId = Convert.toLong(key);
        if (ObjectUtil.isNull(bankId)) {
            return null;
        }
        QuestionBank bank = questionBankMapper.selectById(bankId);
        // 题库不存在（如已被删除）时返回 null，由前端兜底展示原始 id
        return ObjectUtil.isNull(bank) ? null : bank.getBankName();
    }
}
