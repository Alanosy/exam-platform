package org.dromara.common.mybatis.handler;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.handlers.PostInitTableInfoHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.session.Configuration;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.reflect.ReflectUtils;

/**
 * 修改表信息初始化方式
 * 目前用于全局修改是否使用逻辑删除
 *
 * @author Lion Li
 */
public class PlusPostInitTableInfoHandler implements PostInitTableInfoHandler {

    @Override
    public void postTableInfo(TableInfo tableInfo, Configuration configuration) {
        String flag = SpringUtils.getProperty("mybatis-plus.enableLogicDelete", "true");
        ReflectUtils.setFieldValue(tableInfo, "withLogicDelete", Convert.toBool(flag));
    }

}
