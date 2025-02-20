package org.dromara.common.dubbo.threadpool;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.threadpool.ThreadPool;
import org.apache.dubbo.common.threadpool.support.cached.CachedThreadPool;
import org.apache.dubbo.common.threadpool.support.loom.VirtualThreadPool;
import org.dromara.common.core.utils.SpringUtils;

import java.util.concurrent.Executor;

/**
 * 自定义dubbo线程池配置兼容jdk17与jdk21
 */
public class VirtualOrCachedThreadPool implements ThreadPool {
    @Override
    public Executor getExecutor(URL url) {
        if (SpringUtils.isVirtual()) {
            // 如果开启了虚拟线程 使用虚拟线程池
            return new VirtualThreadPool().getExecutor(url);
        }
        return new CachedThreadPool().getExecutor(url);
    }
}
