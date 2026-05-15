package com.inyert.consultant.config;


import com.inyert.consultant.repository.RedisChatMemoryStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonConfig {

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired
    private EmbeddingModel embeddingModel;


    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;
//    @Autowired
//    private OpenAiChatModel model;
//
//    @Bean
//    public ConsultantService consultantService(){
//
//        return AiServices.builder(ConsultantService.class)
//                .chatModel(model)
//                .build();
//    }


    //构建聊天记录
    @Bean
    public ChatMemory chatMemory(){
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore) //使用RedisChatMemoryStore作为聊天记录存储
                        .maxMessages(20)
                        .build();
            }
        };
    }


    //TODO: document(文档)->document(Loader,Parser)->embeddingIngestor(进入车间进行切割\向量化\最后存储给store)->store(接收的仓库)
    //构建向量数据库操作对象
//    @Bean
    public EmbeddingStore store(){
        //加载文档进内存
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/content", new ApachePdfBoxDocumentParser());
//        List<Document> documents = FileSystemDocumentLoader.loadDocuments("D:\\JavaSelfLearn\\GitCloneProjects\\consultant\\src\\main\\resources\\content");

        //设置文本分割器
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(500, 100);

        //创建向量数据库操作对象 (操作的是内存版本的向量数据库，实际项目中可以替换成RedisEmbeddingStore或者其他的向量数据库实现)
//        InMemoryEmbeddingStore store = new InMemoryEmbeddingStore();
        //构建一个embeddingStoreIngestor对象，完成文本数据切割，向量化和存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(redisEmbeddingStore)
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .build();
        ingestor.ingest(documents);
        return redisEmbeddingStore;
    }

    //构建向量数据库检索对象
    @Bean
    public ContentRetriever contentRetriever(/*EmbeddingStore store*/){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)
                .minScore(0.8)
                .maxResults(3)
                .embeddingModel(embeddingModel)
                .build();
    }



}
