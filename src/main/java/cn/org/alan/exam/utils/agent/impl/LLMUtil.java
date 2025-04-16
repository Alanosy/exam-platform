package cn.org.alan.exam.utils.agent.impl;

import cn.org.alan.exam.utils.agent.AIChat;
import cn.org.alan.exam.utils.agent.Assistant;
import cn.org.alan.exam.utils.agent.Constants;
//import dev.langchain4j.data.document.Document;
//import dev.langchain4j.data.document.DocumentSplitter;
//import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
//import dev.langchain4j.data.document.splitter.DocumentSplitters;
//import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
//import dev.langchain4j.rag.content.retriever.ContentRetriever;
//import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
//import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
//import io.milvus.client.MilvusServiceClient;
//import io.milvus.common.clientenum.ConsistencyLevelEnum;
//import io.milvus.param.ConnectParam;
//import io.milvus.param.IndexType;
//import io.milvus.param.MetricType;
import org.jcp.xml.dsig.internal.dom.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 大语言模型实现AIChat
 * 目前只可以直接与大模型进行对话，
 * 目前java版本是8不能使用embedding模型和向量数据库，
 * 如果确实使用这个功能，需要升级java17
 *
 * @author 赵浩森
 * @since 2025/4/14 15:55
 * @version 1.0
 */
@Service
@ConditionalOnProperty(name = "online-exam.chat-platform.type", havingValue = "llm")
public class LLMUtil implements AIChat {

    @Value("${llm.api-key}")
    private String llmApiKey;

    @Value("${llm.base-url}")
    private String llmBaseUrl;

    @Value("${llm.model}")
    private String llmModelName;

    @Value("${embedding.api-key}")
    private String embeddingApiKey;

    @Value("${embedding.base-url}")
    private String embeddingBaseUrl;

    @Value("${embedding.model}")
    private String embeddingModelName;

    @Value("${milvus.host}")
    private String milvusHost;

    @Value("${milvus.port}")
    private Integer milvusPort;

    @Value("${milvus.username}")
    private String milvusUsername;

    @Value("${milvus.password}")
    private String milvusPassword;

    @Override
    // 获取聊天响应的方法
    public String getChatResponse(String msg) {
        // 创建一个助手（Assistant）
        Assistant assistant = createAssistant();

        // 创建系统消息，通常用于定义助手的行为或角色
        ChatMessage systemMessage = new SystemMessage(Constants.systemMessage);

        // 创建用户消息，表示用户输入的内容
        ChatMessage userMessage = new UserMessage(msg);

        // 构建提示词
        String input = systemMessage.text() + "\n" + userMessage.text();
        // 调用助手的 `answer` 方法生成回复，并返回结果
        return assistant.answer(input);
    }

    // 创建助手（Assistant）的方法
    private Assistant createAssistant() {

        // 初始化 OpenAI 的聊天语言模型（LLM），并配置相关参数
        OpenAiChatModel llm = OpenAiChatModel.builder().apiKey(llmApiKey) // 设置 OpenAI API 密钥
                .modelName(llmModelName) // 设置使用的模型名称
                .baseUrl(llmBaseUrl) // 设置 OpenAI API 的基础 URL
                .temperature(Constants.temperature) // 设置生成文本的随机性
                .maxTokens(Constants.maxToken) // 设置生成文本的最大 token 数
                .build();

        // 初始化嵌入模型（确保维度与模型匹配）
        /* EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingBaseUrl)
                .apiKey(embeddingApiKey)
                .modelName(embeddingModelName) // 明确指定模型名称
                .build();

        // 配置 Milvus 客户端
        MilvusServiceClient customMilvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(milvusHost)
                        .withPort(milvusPort)
                        .build()
        );
        // 构建 MilvusEmbeddingStore
        MilvusEmbeddingStore embeddingStore = MilvusEmbeddingStore.builder()
                .milvusClient(customMilvusClient)
                .username(milvusUsername)
                .password(milvusPassword)
                .collectionName("example_collection")
                .dimension(1536) // 使用实际维度
                .indexType(IndexType.HNSW) // 更高效的索引类型
                .metricType(MetricType.IP) // 内积（IP）
                .consistencyLevel(ConsistencyLevelEnum.EVENTUALLY)
                .autoFlushOnInsert(true)
                .idFieldName("id")
                .textFieldName("text")
                .metadataFieldName("metadata")
                .vectorFieldName("vector")
                .build();

        // 初始化内容检索器（Content Retriever），用于根据用户的查询检索相关的文本片段
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder().embeddingStore(embeddingStore) // 使用嵌入存储
                .embeddingModel(embeddingModel) // 使用嵌入模型计算相似度
                .maxResults(Constants.maxResults) // 设置检索的最大结果数量
                .minScore(Constants.minScore) // 设置检索的最低相似度分数
                .build();

        // 初始化聊天记忆（Chat Memory），用于保存最近的对话历史
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(Constants.withMaxMessages);*/

        // 使用 AiServices 构建助手实例，并设置相关组件
        return AiServices.builder(Assistant.class).chatLanguageModel(llm) // 设置聊天语言模型
//                .contentRetriever(contentRetriever) // 设置内容检索器
//                .chatMemory(chatMemory) // 设置聊天记忆
                .build();
    }

    // 将相对路径转换为绝对路径
    public Path toPath(String relativePath) {
        try {
            // 使用类加载器获取资源路径，并将其转换为 URI 形式的 Path
            URL fileUrl = Utils.class.getClassLoader().getResource(relativePath);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            // 如果发生 URI 格式异常，则抛出运行时异常
            throw new RuntimeException(e);
        }
    }

    // 先运行这个代码构建向量数据库
    /*public void creatKnowledgeBase() {
        // 将文档路径转换为 Path 对象
        Path directoryPath = toPath(Constants.knowledgeBasePath);

        // 递归加载指定路径下的所有文档，并使用 Apache Tika 解析器解析内容
        List<Document> documents = FileSystemDocumentLoader.loadDocumentsRecursively(directoryPath);

        // 使用递归分割器将文档分割成较小的段落
        DocumentSplitter splitter = DocumentSplitters.recursive(Constants.maxSegmentSizeInChars, // 每个片段的最大字符数
                Constants.maxOverlapSizeChars // 相邻片段之间的最大重叠字符数
        );

        // 存储分割后的文本片段
        List<TextSegment> segments = new ArrayList<>();
        documents.forEach(document -> segments.addAll(splitter.split(document)));


        // 初始化嵌入模型（确保维度与模型匹配）
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingBaseUrl)
                .apiKey(embeddingApiKey)
                .modelName(embeddingModelName) // 明确指定模型名称
                .build();

        // 生成嵌入向量
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // 配置 Milvus 客户端
        MilvusServiceClient customMilvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(milvusHost)
                        .withPort(milvusPort)
                        .build()
        );

        try {
            // 构建 MilvusEmbeddingStore
            MilvusEmbeddingStore store = MilvusEmbeddingStore.builder()
                    .milvusClient(customMilvusClient)
                    .username(milvusUsername)
                    .password(milvusPassword)
                    .collectionName("example_collection")
                    .dimension(1536) // 使用实际维度
                    .indexType(IndexType.HNSW) // 更高效的索引类型
                    .metricType(MetricType.IP) // 内积（IP）
                    .consistencyLevel(ConsistencyLevelEnum.EVENTUALLY)
                    .autoFlushOnInsert(true)
                    .idFieldName("id")
                    .textFieldName("text")
                    .metadataFieldName("metadata")
                    .vectorFieldName("vector")
                    .build();

            // 插入数据并处理异常
            store.addAll(embeddings, segments);
        } catch (Exception e) {
            System.err.println("操作失败: " + e.getMessage());
        } finally {
            customMilvusClient.close(); // 释放资源
        }
    }*/
}