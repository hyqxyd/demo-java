package cn.fighter3.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentProcessingService {

    private final Tika tika = new Tika();
    private final String uploadDir = "src/main/resources/upload/knowledge";
    
    @Autowired
    private cn.fighter3.config.RagConfig ragConfig;

    /**
     * 保存上传的文件并提取文本内容
     */
    public String processDocument(MultipartFile file) throws IOException, TikaException {
        // 确保目录存在
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = dirPath.resolve(uniqueFilename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        // 提取文本
        String content = tika.parseToString(filePath.toFile());

        return content;
    }

    /**
     * 将文本分割成适合嵌入的块
     */
    public List<String> splitIntoChunks(String text) {
        int maxChunkSize = ragConfig.getChunkSize();
        List<String> chunks = new ArrayList<>();
        
        // 如果文本长度小于最大块大小，直接返回
        if (text.length() <= maxChunkSize) {
            chunks.add(text);
            return chunks;
        }

        // 按段落分割
        String[] paragraphs = text.split("\n\n");
        
        StringBuilder currentChunk = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            // 如果段落本身超过最大块大小，需要进一步分割
            if (paragraph.length() > maxChunkSize) {
                // 如果当前块不为空，先添加到结果中
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }
                
                // 分割长段落
                int start = 0;
                while (start < paragraph.length()) {
                    int end = Math.min(start + maxChunkSize, paragraph.length());
                    // 尝试在句子边界分割
                    if (end < paragraph.length()) {
                        int sentenceEnd = paragraph.lastIndexOf(".", end);
                        if (sentenceEnd > start && sentenceEnd > end - 100) { // 确保不会切得太短
                            end = sentenceEnd + 1; // 包含句号
                        }
                    }
                    chunks.add(paragraph.substring(start, end));
                    start = end;
                }
            } else {
                // 检查添加当前段落是否会超出最大块大小
                if (currentChunk.length() + paragraph.length() + 2 > maxChunkSize) { // +2 for "\n\n"
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder(paragraph);
                } else {
                    // 添加段落分隔符，除非是第一个段落
                    if (currentChunk.length() > 0) {
                        currentChunk.append("\n\n");
                    }
                    currentChunk.append(paragraph);
                }
            }
        }
        
        // 添加最后一个块（如果有）
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }
}