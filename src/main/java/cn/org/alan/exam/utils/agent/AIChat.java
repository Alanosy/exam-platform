package cn.org.alan.exam.utils.agent;

/**
 * 大模型调用接口
 * @author 赵浩森
 * @since 2025/4/12 21:22
 * @version 1.0
 */
public interface AIChat {

    /**
     * 向大模型输入消息，返回结果
     *
     * @param msg 输入消息
     * @return 结果
     */
    public String getChatResponse(String msg) throws Exception;
}