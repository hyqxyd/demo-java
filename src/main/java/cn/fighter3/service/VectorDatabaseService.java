package cn.fighter3.service;

import cn.fighter3.config.RagConfig;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper; // 添加这个导入
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// 删除这行导入
// import io.milvus.v2.service.collection.request.HasCollectionReq;
import java.util.ArrayList;
import java.util.List;

@Service
public class VectorDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(VectorDatabaseService.class);
    private final MilvusServiceClient milvusClient;
    private final String COLLECTION_NAME = "knowledge_base";
    private final int VECTOR_DIM; // 向量维度从配置文件读取

    @Autowired
    public VectorDatabaseService(RagConfig ragConfig) {
        this.VECTOR_DIM = ragConfig.getVectorDimension();
        // 连接到Milvus服务器，添加连接超时配置
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(ragConfig.getMilvusHost())
                .withPort(ragConfig.getMilvusPort())
                .withConnectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .withKeepAliveTime(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS)
                .withKeepAliveTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        
        try {
            this.milvusClient = new MilvusServiceClient(connectParam);
            logger.info("Milvus客户端连接成功: {}:{}", ragConfig.getMilvusHost(), ragConfig.getMilvusPort());
        } catch (Exception e) {
            logger.error("Milvus客户端连接失败", e);
            throw new RuntimeException("无法连接到Milvus服务器", e);
        }
        
        // 延迟初始化集合，增加重试机制
        initializeCollectionWithRetry();
        logger.info("向量数据库服务初始化完成");
    }

    /**
     * 带重试机制的集合初始化
     */
    private void initializeCollectionWithRetry() {
        int maxRetries = 3;
        int retryDelay = 2000; // 2秒
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                // 等待Milvus服务完全启动
                if (i > 0) {
                    Thread.sleep(retryDelay);
                    logger.info("重试连接Milvus，第{}次尝试", i + 1);
                }
                
                // 确保集合存在
                ensureCollectionExists();
                return; // 成功则退出
                
            } catch (Exception e) {
                logger.warn("第{}次初始化向量数据库失败: {}", i + 1, e.getMessage());
                if (i == maxRetries - 1) {
                    logger.error("向量数据库初始化失败，已达到最大重试次数", e);
                    throw new RuntimeException("无法初始化向量数据库: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 确保知识库集合存在
     */
    private void ensureCollectionExists() {
        try {
            logger.info("检查Milvus集合是否存在: {}", COLLECTION_NAME);
            
            // 检查集合是否存在
            R<Boolean> hasCollectionResp = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .build());
            
            // 检查响应状态
            if (hasCollectionResp.getStatus() != R.Status.Success.getCode()) {
                logger.error("检查集合存在失败，状态码: {}, 错误信息: {}", 
                    hasCollectionResp.getStatus(), hasCollectionResp.getMessage());
                throw new RuntimeException("检查集合存在失败: " + hasCollectionResp.getMessage());
            }
            
            // 使用getData()获取Boolean值，然后再应用!运算符
            if (hasCollectionResp.getData() != Boolean.TRUE) {
                logger.info("集合不存在，开始创建: {}", COLLECTION_NAME);
                createCollection();
                logger.info("成功创建知识库集合: {}", COLLECTION_NAME);
            } else {
                logger.info("知识库集合已存在: {}", COLLECTION_NAME);
            }
        } catch (Exception e) {
            logger.error("检查集合存在时出错，集合名: {}", COLLECTION_NAME, e);
            throw new RuntimeException("无法检查向量集合: " + e.getMessage(), e);
        }
    }

    /**
     * 创建知识库集合
     */
    private void createCollection() {
        try {
            // 定义字段
            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true)
                    .build();

            FieldType contentField = FieldType.newBuilder()
                    .withName("content")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build();

            FieldType filenameField = FieldType.newBuilder()
                    .withName("filename")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(256)
                    .build();

            FieldType embeddingField = FieldType.newBuilder()
                    .withName("embedding")
                    .withDataType(DataType.FloatVector)
                    .withDimension(VECTOR_DIM)
                    .build();

            // 创建一个 List 来存储所有字段
            List<FieldType> fieldTypes = new ArrayList<>();
            fieldTypes.add(idField);
            fieldTypes.add(contentField);
            fieldTypes.add(filenameField);
            fieldTypes.add(embeddingField);
            
            // 创建集合
            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withFieldTypes(fieldTypes)
                    .build();

            milvusClient.createCollection(createCollectionParam);

            // 创建索引
            CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withFieldName("embedding")
                    .withIndexType(IndexType.HNSW)
                    .withMetricType(MetricType.IP)  // 将 COSINE 改为 IP
                    .withExtraParam("{\"M\":16,\"efConstruction\":200}")
                    .build();

            milvusClient.createIndex(createIndexParam);
            logger.info("为集合 {} 创建索引成功", COLLECTION_NAME);
        } catch (Exception e) {
            logger.error("创建集合时出错", e);
            throw new RuntimeException("无法创建向量集合: " + e.getMessage(), e);
        }
    }

    /**
     * 插入文档块到知识库
     */
    public void insertDocumentChunk(String content, String filename, float[] embedding) {
        try {
            // 将 float[] 转换为 List<Float>
            List<Float> embeddingList = new ArrayList<>();
            for (float value : embedding) {
                embeddingList.add(value);
            }
            
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("content", List.of(content)));
            fields.add(new InsertParam.Field("filename", List.of(filename)));
            fields.add(new InsertParam.Field("embedding", List.of(embeddingList)));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withFields(fields)
                    .build();

            milvusClient.insert(insertParam);
            logger.info("成功插入文档块，文件名: {}", filename);
        } catch (Exception e) {
            logger.error("插入文档块时出错", e);
            throw new RuntimeException("无法插入文档块: " + e.getMessage(), e);
        }
    }

    /**
     * 根据查询向量搜索相关文档
     */
    public List<SearchResult> searchSimilarDocuments(float[] queryEmbedding, int topK) {
        try {
            // 加载集合
            milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .build());
    
            // 将 float[] 转换为 List<Float>
            List<Float> queryEmbeddingList = new ArrayList<>();
            for (float value : queryEmbedding) {
                queryEmbeddingList.add(value);
            }
    
            // 搜索相似文档
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withMetricType(MetricType.IP)
                    .withOutFields(List.of("content", "filename"))  // 使用List.of创建List<String>
                    .withTopK(topK)
                    .withVectors(List.of(queryEmbeddingList))
                    .withVectorFieldName("embedding")
                    .build();
    
            R<SearchResults> response = milvusClient.search(searchParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                logger.error("搜索失败: {}", response.getMessage());
                throw new RuntimeException("搜索失败: " + response.getMessage());
            }
            
            // 使用SearchResultsWrapper包装搜索结果 - 修正构造函数参数
            SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
            
            // 解析搜索结果
            List<SearchResult> results = new ArrayList<>();
            // 获取查询结果数量 - 使用查询向量的数量
            int numQueries = 1; // 因为我们只传入了一个查询向量
            
            for (int i = 0; i < numQueries; i++) {
                List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(i);
                for (int j = 0; j < scores.size(); j++) {
                    SearchResultsWrapper.IDScore score = scores.get(j);
                    // 获取字段值 - 修正方法签名，参数顺序为(fieldName, index)
                    String content = wrapper.getFieldData("content", j).toString();
                    String filename = wrapper.getFieldData("filename", j).toString();
                    float scoreValue = score.getScore();
                    
                    results.add(new SearchResult(content, filename, scoreValue));
                }
            }
            
            logger.info("搜索完成，找到 {} 个相关文档", results.size());
            return results;
        } catch (Exception e) {
            logger.error("搜索相似文档时出错", e);
            throw new RuntimeException("无法搜索相似文档: " + e.getMessage(), e);
        }
    }
    
    // 搜索结果类
    public static class SearchResult {
        private final String content;
        private final String filename;
        private final float score;
        
        public SearchResult(String content, String filename, float score) {
            this.content = content;
            this.filename = filename;
            this.score = score;
        }
        
        public String getContent() {
            return content;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public float getScore() {
            return score;
        }
    }
}