package cn.fighter3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    @Value("${knowledge.base.chunk-size:1000}")
    private int chunkSize;

    @Value("${knowledge.base.top-k:5}")
    private int topK;

    @Value("${milvus.vector.dimension:1536}")
    private int vectorDimension;

    // Getters
    public String getMilvusHost() {
        return milvusHost;
    }

    public int getMilvusPort() {
        return milvusPort;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getTopK() {
        return topK;
    }

    public int getVectorDimension() {
        return vectorDimension;
    }
}