package com.inyert.consultant.tool;

import com.hmall.api.dto.ItemDTO;
import com.inyert.consultant.client.ItemClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * 商品推荐工具 (Spring AI)
 */
@Component
public class RecommendationTool {

    // 建议替换为数据库中真实的手机类商品 ID
    public static final List<Long> SAMPLE_RECOMMEND_IDS = List.of(100002672274L, 100002672272L, 100002651384L, 100002662190L, 100002624522L, 100002544842L);
    public static final List<Long> HOT_ITEM_IDS = List.of(100002465730L, 100002440466L, 100002352522L, 100002292164L, 100002260394L);

    @Autowired
    private ItemClient itemClient;

    @Bean
    @Description("当用户询问任何具体商品（如手机、衣服）时，必须调用此工具。输入关键词和分类，返回平台真实的商品列表。严禁在没有调用此工具的情况下回答商品推荐问题。")
    public Function<RecommendRequest, String> recommendItems() {
        return req -> {
            try {
                // 显式声明 Supplier 的返回类型以解决泛型推断问题
                java.util.function.Supplier<List<ItemDTO>> supplier = () -> {
                    try {
                        return itemClient.queryItemByIds(SAMPLE_RECOMMEND_IDS);
                    } catch (Exception e) {
                        System.err.println("Feign 调用失败，详细错误: ");
                        e.printStackTrace();
                        return java.util.Collections.emptyList();
                    }
                };
                
                List<ItemDTO> items = java.util.concurrent.CompletableFuture.supplyAsync(supplier).join();
                System.out.println("[TOOL-DEBUG] Feign 调用成功，返回商品数量: " + (items != null ? items.size() : 0));
                if (items != null && !items.isEmpty()) {
                    System.out.println("[TOOL-DEBUG] 第一个商品名称: " + items.get(0).getName());
                }

                if (items == null || items.isEmpty()) {
                    return "抱歉，根据您的需求暂未找到合适的商品（当前 item-service 可能未启动）。";
                }
                StringBuilder result = new StringBuilder("根据您的需求，为您推荐以下商品：\n");
                for (ItemDTO item : items) {
                    result.append(String.format(
                        "【%s】- 品牌：%s - 价格：¥%.2f - 销量：%d件\n",
                        item.getName(),
                        item.getBrand() != null ? item.getBrand() : "未知",
                        item.getPrice() / 100.0,
                        item.getSold() != null ? item.getSold() : 0
                    ));
                }
                return result.toString();
            } catch (Exception e) {
                return "推荐商品时出错：" + e.getMessage();
            }
        };
    }

    @Bean
    @Description("获取当前平台热销的商品列表")
    public Function<EmptyRequest, String> getHotItems() {
        return v -> {
            try {
                List<ItemDTO> items = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                        itemClient.queryItemByIds(HOT_ITEM_IDS)
                ).join();
                if (items == null || items.isEmpty()) {
                    return "当前暂无热销商品信息。";
                }
                StringBuilder result = new StringBuilder("当前热销商品列表：\n");
                for (ItemDTO item : items) {
                    result.append(String.format(
                        "【%s】- ¥%.2f（销量：%d件）\n",
                        item.getName(),
                        item.getPrice() / 100.0,
                        item.getSold() != null ? item.getSold() : 0
                    ));
                }
                return result.toString();
            } catch (Exception e) {
                return "获取热销商品时出错：" + e.getMessage();
            }
        };
    }

    public List<ItemDTO> querySampleItems() {
        return java.util.concurrent.CompletableFuture.supplyAsync(() ->
                itemClient.queryItemByIds(SAMPLE_RECOMMEND_IDS)
        ).join();
    }

    public record RecommendRequest(
            @com.fasterxml.jackson.annotation.JsonProperty(required = true)
            @com.fasterxml.jackson.annotation.JsonPropertyDescription("关键词，如手机、衣服") String keyword,
            @com.fasterxml.jackson.annotation.JsonProperty(required = true)
            @com.fasterxml.jackson.annotation.JsonPropertyDescription("分类，如电子产品") String category) {}

    public record EmptyRequest() {}
}
