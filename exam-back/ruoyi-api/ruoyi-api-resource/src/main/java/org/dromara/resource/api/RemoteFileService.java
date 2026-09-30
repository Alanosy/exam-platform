package org.dromara.resource.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.resource.api.domain.RemoteFile;

import java.util.List;

/**
 * 文件服务
 *
 * @author Lion Li
 */
public interface RemoteFileService {

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    RemoteFile upload(String name, String originalFilename, String contentType, byte[] file) throws ServiceException;

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    String selectUrlByIds(String ossIds);

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    List<RemoteFile> selectByIds(String ossIds);

    /**
     * 按文件访问地址物理删除对象存储文件
     *
     * <p>只有业务侧确认该文件已无人引用（如试题已删除、富文本里的图片已被移除）时才调用，
     * 删除时会同时清理桶里的对象和 sys_oss 表记录。
     *
     * @param urls 文件访问地址集合
     * @return 实际删除的文件数量
     */
    Integer deleteByUrls(List<String> urls);
}
