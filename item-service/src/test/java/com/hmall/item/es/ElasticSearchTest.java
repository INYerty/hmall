package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//激活本地环境配置文件
//@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticSearchTest {
//*****************************TODO:依旧是连接相关，感觉可以提取出来*********************************
    //写一个配置类 new一个RestHighLevelClient对象，注入到spring容器中，测试类直接@Autowired注入使用就好了
    /**
     * 创建连接
     */
    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        HttpHost.create("http://192.168.88.131:9200")
                )
        );
    }

    /**
     * 释放连接
     */
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

    //搜索相关api测试


    /**
     * matchAll查询
     *
     * @throws IOException
     */
    @Test
    void matchAll() throws IOException {
        //1. 创建request对象
        SearchRequest request = new SearchRequest("items");
        //2.配置request参数
        request.source()
                .query(QueryBuilders.matchAllQuery());
        //3.发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//        System.out.println("search="+search);

        //解析结果
        SearchHits searchHits = search.getHits();
        long total = searchHits.getTotalHits().value; //总记录数
        System.out.println("total=" + total);

        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
//            System.out.println(json);
            //转成itemDoc
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println(doc);
        }

    }


    /**
     * boolQuery+高亮查询
     *
     * @throws IOException
     */
    @Test
    void boolSearchAndHighlight() throws IOException {
        //1. 创建request对象
        SearchRequest request = new SearchRequest("items");
        //2.配置request参数
        request.source()
                .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("name", "手机"))
                        .filter(QueryBuilders.termQuery("brand", "华为"))
                )
                .highlighter(SearchSourceBuilder.highlight()
                        .field("brand")
                        .preTags("<em>")
                        .postTags("</em>"));
        //2.发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        SearchHits searchHits = search.getHits();
        long totalValue = searchHits.getTotalHits().value;
        search.getHits().forEach(hit -> {
            String json = hit.getSourceAsString();
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println(doc);
        });
    }

    /**
     * 多字段匹配查询
     *
     * @throws IOException
     */
    @Test
    void mutiMatchQuery() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source()
                .query(QueryBuilders.multiMatchQuery("华为", "name", "brand"));
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        long totalValue = search.getHits().getTotalHits().value;//总记录数
        System.out.println("total=" + totalValue);
        search.getHits().forEach(hit -> {
            String json = hit.getSourceAsString();
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println(doc);
        });


    }

    /**
     * termQuery是精确查询，matchQuery是模糊查询
     *
     * @throws IOException
     */
    @Test
    void termQuery() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.termQuery("brand", "华为"));
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        long totalValue = search.getHits().getTotalHits().value;
        System.out.println("total=" + totalValue);
        search.getHits().forEach(hit -> {
            String json = hit.getSourceAsString();
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println(doc);
        });
    }

    @Test
    void testRangeQuery() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(
                QueryBuilders
                        .rangeQuery("price")
                        .gte(100000)
                        .lte(200000)
        );
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        long value = search.getHits().getTotalHits().value;
        System.out.println("total=" + value);
        search.getHits().forEach(hit -> {
            String json = hit.getSourceAsString();
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println(doc);
        });
    }

    @Test
    void testRestClientForSearch() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchQuery("name", "脱脂牛奶"))
                .filter(QueryBuilders.matchQuery("brand", "德亚"))
                .filter(QueryBuilders.rangeQuery("price").lte(10000))
        );
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        long total = search.getHits().getTotalHits().value;
        System.out.println("total=" + total);
        search.getHits().forEach(hit -> {
            String json = hit.getSourceAsString();
            ItemDoc bean = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println(bean);
        });
    }

    @Test
    void testQueryPageSort() throws IOException {
        //模拟前端传递的分页参数
        int pageNo = 2,pageSize = 5;
        //1.创建request
        SearchRequest request = new SearchRequest("items");
        //2.配置request参数
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from((pageNo-1)*pageSize).size(pageSize);
        request.source().sort("sold", SortOrder.DESC);
        request.source().sort("price", SortOrder.ASC);
        //3.发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        //4.处理结果
        long total = search.getHits().getTotalHits().value;
        System.out.println("total=" + total);
        //5.解析结果
        search.getHits().forEach(hit -> {
            String json = hit.getSourceAsString();
            ItemDoc bean = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println(bean);
        });
    }

    @Test
    void boolHighlight() throws IOException {
        //1. 创建request对象
        SearchRequest request = new SearchRequest("items");
        //2.配置request参数
        request.source()
                .query(QueryBuilders.matchQuery("name","脱脂牛奶"))
                .highlighter(SearchSourceBuilder.highlight()
                        .field("name")
                        .preTags("<em>")
                        .postTags("</em>"));
        //2.发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        SearchHits searchHits = search.getHits();
        long totalValue = searchHits.getTotalHits().value;
        System.out.println("total=" + totalValue);
        search.getHits().forEach(hit -> {
            String json = hit.getSourceAsString();
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            //获取高亮结果
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            //判断有没有高亮
            if(hfs!=null && !hfs.isEmpty()){
                //根据高亮字段名获取高亮结果
                HighlightField hf = hfs.get("name");
                //如果需要高亮的字符很长，那么es底层会将该结果，切分成多个，
                // 所以变成了一个数组"name" : [],这里取得[0]是第一个高亮分组,简化了，因为字符串没有太长
                //获取高亮结果，覆盖原来结果
                String hfName = hf.getFragments()[0].toString();

                doc.setName(hfName);
            }
            System.out.println(doc);
        });
    }


    @Test
    void testAggs() throws IOException {
        SearchRequest request = new SearchRequest("items");
        //设置size为0，不返回结果 只要聚合不要文档
        request.source().size(0);
        //聚合:聚合类型，聚合名称，聚合字段
        String brandAggName = "brandAgg";
        request.source().aggregation(AggregationBuilders.terms(brandAggName).field("brand").size(10));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        System.out.println(response);
        TotalHits totalHits = response.getHits().getTotalHits();
        System.out.println("total=" + totalHits.value);
        Aggregations aggregations = response.getAggregations();
        Terms brandTerms = aggregations.get(brandAggName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //遍历桶
        buckets.forEach(bucket -> {
            System.out.println("brand: " + bucket.getKeyAsString());
            System.out.println("docCount: " + bucket.getDocCount());
        });
    }

    }
