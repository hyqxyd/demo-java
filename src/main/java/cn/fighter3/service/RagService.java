package cn.fighter3.service;

import cn.fighter3.config.RagConfig;
import cn.fighter3.service.VectorDatabaseService.SearchResult;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    @Autowired
    private DocumentProcessingService documentProcessingService;
    
    @Autowired
    private DashScopeEmbeddingService embeddingService;
    
    @Autowired
    private VectorDatabaseService vectorDatabaseService;
    
    @Autowired
    private WenxinYiyanService wenxinYiyanService; // 百度文心API服务
    
    @Autowired
    private RagConfig ragConfig;
    
    /**
     * 上传文档到知识库
     */
    public void uploadDocumentToKnowledgeBase(MultipartFile file) throws IOException, TikaException {
        logger.info("开始处理文档: {}", file.getOriginalFilename());
        
        // 处理文档，提取文本
        String content = documentProcessingService.processDocument(file);
        String filename = file.getOriginalFilename();
        
        logger.info("文档处理完成，开始分割文本");
        
        // 分割文本为块
        List<String> chunks = documentProcessingService.splitIntoChunks(content);
        
        logger.info("文本分割完成，共 {} 个块，开始生成嵌入向量", chunks.size());
        
        // 为每个块生成嵌入并存储
        int count = 0;
        for (String chunk : chunks) {
            float[] embedding = embeddingService.getEmbedding(chunk);
            vectorDatabaseService.insertDocumentChunk(chunk, filename, embedding);
            count++;
            if (count % 10 == 0) {
                logger.info("已处理 {} 个文本块", count);
            }
        }
        
        logger.info("文档 {} 处理完成，共添加 {} 个文本块到知识库", filename, chunks.size());
    }
    
    /**
     * 基于知识库回答问题
     */
    public String answerQuestion(String question, int userId, String sessionId) throws IOException {
        logger.info("收到问题: {}", question);
        
        // 为问题生成嵌入
        float[] questionEmbedding = embeddingService.getEmbedding(question);
        
        // 检索相关文档
        List<SearchResult> relevantDocuments = vectorDatabaseService.searchSimilarDocuments(
                questionEmbedding, ragConfig.getTopK());
        
        logger.info("找到 {} 个相关文档片段", relevantDocuments.size());
        
        // 构建上下文
        String context = relevantDocuments.stream()
                .map(result -> "[" + result.getFilename() + "] " + result.getContent())
                .collect(Collectors.joining("\n\n"));
        
        // 构建增强提示
        String enhancedPrompt = "基于以下信息回答问题:\n\n" + context + "\n\n问题: " + question + "\n\n请根据提供的信息给出准确、全面的回答。如果提供的信息不足以回答问题，请明确指出。";
        
        logger.info("构建增强提示完成，调用大模型生成回答");
        
        // 调用大模型生成回答
        String answer = callLargeLanguageModel(enhancedPrompt, userId, sessionId);
        
        logger.info("大模型回答生成完成");
        
        return answer;
    }
    
    /**
     * 调用大语言模型
     */
    private String callLargeLanguageModel(String prompt, int userId, String sessionId) throws IOException {
        // 使用百度文心API服务
        try {
            return wenxinYiyanService.getAnswer(prompt, userId, sessionId);
        } catch (Exception e) {
            logger.error("调用大模型API失败", e);
            return "调用大模型服务时发生错误: " + e.getMessage();
        }
    }
}