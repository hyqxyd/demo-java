package cn.fighter3.controller;

import cn.fighter3.result.Result;
import cn.fighter3.service.RagService;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger logger = LoggerFactory.getLogger(RagController.class);

    @Autowired
    private RagService ragService;
    
    /**
     * 上传文档到知识库
     */
    @PostMapping("/upload")
    public Result uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("接收到文件上传请求: {}", file.getOriginalFilename());
            ragService.uploadDocumentToKnowledgeBase(file);
            
            Map<String, String> data = new HashMap<>();
            data.put("message", "文档已成功上传到知识库");
            data.put("filename", file.getOriginalFilename());
            
            return Result.success(data);
        } catch (IOException | TikaException e) {
            logger.error("文档处理失败", e);
            return Result.error(500, "文档处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 基于知识库回答问题
     */
    @PostMapping("/query")
    public Result queryKnowledgeBase(
            @RequestParam("question") String question,
            @RequestParam("userId") int userId,
            @RequestParam("sessionId") String sessionId) {
        try {
            logger.info("接收到知识库查询请求，用户ID: {}, 会话ID: {}", userId, sessionId);
            String answer = ragService.answerQuestion(question, userId, sessionId);
            
            Map<String, String> data = new HashMap<>();
            data.put("answer", answer);
            
            return Result.success(data);
        } catch (IOException e) {
            logger.error("查询处理失败", e);
            return Result.error(500, "查询处理失败: " + e.getMessage());
        }
    }
}