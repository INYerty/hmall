package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

//激活本地环境配置文件
@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocumentTest {
    @Autowired
    private IItemService itemService;

    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.88.131", 9200, "http")
                )
        );
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void testConnection() {
        System.out.println("client=" + client);
    }

    /*
     *********************************文档操作******************************************
     */


    /**
     * 添加文档
     */
    @Test
    void testAddDocsToIndex() throws IOException {
        // 2.参数准备
        //从数据库查询一条数据转成json字符串
        Item item = itemService.getById(100000011127L);
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        itemDoc.setPrice(29900);
        // 1.准备request对象
        IndexRequest request = new IndexRequest("items").id(item.getId().toString());
        String parse = JSONUtil.toJsonStr(itemDoc);
        request.source(parse, XContentType.JSON);
        // 3.发送请求
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }


    /**
     * 查询文档 也是 修改文档(是全量修改):执行完新增，那么再去做一个新增操作就相当于是修改文档
     */
    @Test
    void testGetDocsFromIndex() throws IOException {
        GetRequest request = new GetRequest("items", "100000011127");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        //解析结果
        String json = response.getSourceAsString();
        ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println(doc);
    }

    /**
     * 删除文档
     */
    @Test
    void testDeleteDocsFromIndex() throws IOException {
        DeleteRequest request = new DeleteRequest("items", "100000011127");
        client.delete(request, RequestOptions.DEFAULT);
    }


    /**
     * 局部修改文档
     */
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("items", "100000011127");
        request.doc(
                "price", 25600
        );
        client.update(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量添加文档
     */
    @Test
    void testBulkDoc() throws IOException {
        int pageNo = 1, pageSize = 500;
        while (true) {
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));

            List<Item> records = page.getRecords();
            if (records == null || records.isEmpty()) {
                return;
            }
            BulkRequest request = new BulkRequest();
            for (Item item : records) {
                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                String json = JSONUtil.toJsonStr(itemDoc);
                request.add(
                        new IndexRequest("items")
                                .id(itemDoc.getId())
                                .source(json, XContentType.JSON)
                );
            }
            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
            System.out.println("已添加第" + pageNo + "页数据");
        }
    }
}