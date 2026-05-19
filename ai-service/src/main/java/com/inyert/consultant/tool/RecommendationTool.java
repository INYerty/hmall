package com.inyert.consultant.tool;

import com.hmall.api.dto.ItemDTO;
import com.inyert.consultant.client.ItemClient;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品推荐工具
 * 提供AI能调用的商品查询和推荐功能
 */
@Component
public class RecommendationTool {

    @Autowired
    private ItemClient itemClient;

    /**
     * 根据关键词和分类推荐商品
     * @param keyword 搜索关键词（如：手机、家电等）
     * @param category 商品分类
     * @return 推荐的商品列表
     */
    @Tool("根据用户的需求推荐符合条件的商品。输入搜索关键词和分类，返回推荐的商品列表。推荐商品时要考虑用户的实际需求，选择性价比高、评价好的商品")
    public String recommendItems(String keyword, String category) {
        try {
            // 这里可以根据实际需求调用商品查询接口
            // 目前演示用随机ID查询
            List<Long> sampleIds;

            // 根据不同的分类返回不同范围的商品ID
            if (category != null && category.contains("手机")) {
                sampleIds = List.of(1L, 2L, 3L, 4L, 5L);
            } else if (category != null && category.contains("家电")) {
                sampleIds = List.of(10L, 11L, 12L, 13L, 14L);
            } else if (category != null && category.contains("服装")) {
                sampleIds = List.of(20L, 21L, 22L, 23L, 24L);
            } else {
                // 默认推荐一些热门商品
                sampleIds = List.of(1L, 2L, 10L, 11L, 20L, 21L);
            }

            // 查询商品详情
            List<ItemDTO> items = itemClient.queryItemByIds(sampleIds);

            if (items == null || items.isEmpty()) {
                return "抱歉，根据您的需求暂未找到合适的商品。";
            }

            // 格式化推荐结果
            StringBuilder result = new StringBuilder("根据您的需求，为您推荐以下商品：\n");

            for (ItemDTO item : items) {
                result.append(String.format(
                    "【%s】\n" +
                    "  品牌：%s\n" +
                    "  分类：%s\n" +
                    "  价格：¥%.2f\n" +
                    "  销量：%d件\n" +
                    "  评论数：%d条\n" +
                    "  库存：%d件\n\n",
                    item.getName(),
                    item.getBrand() != null ? item.getBrand() : "未知",
                    item.getCategory() != null ? item.getCategory() : "未知",
                    item.getPrice() / 100.0,
                    item.getSold() != null ? item.getSold() : 0,
                    item.getCommentCount() != null ? item.getCommentCount() : 0,
                    item.getStock() != null ? item.getStock() : 0
                ));
            }

            return result.toString();
        } catch (Exception e) {
            return "推荐商品时出错：" + e.getMessage();
        }
    }

    /**
     * 获取热销商品
     * @return 热销商品列表
     */
    @Tool("获取当前平台热销的商品列表")
    public String getHotItems() {
        try {
            List<Long> hotIds = List.of(1L, 2L, 3L, 4L, 5L);
            List<ItemDTO> items = itemClient.queryItemByIds(hotIds);

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
    }
}

