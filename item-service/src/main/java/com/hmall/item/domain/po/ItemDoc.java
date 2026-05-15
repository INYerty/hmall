package com.hmall.item.domain.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Elasticsearch 商品文档对象
 * 对应 ES 索引中的一条商品数据
 */
@Data
public class ItemDoc {

    /**
     * 商品id
     * ES中使用keyword类型，不分词，用于精确匹配
     */
    @ApiModelProperty("商品id")
    private String id;

    /**
     * 商品名称
     * text类型，会使用 ik_smart 分词器
     * 用于全文检索
     */
    @ApiModelProperty("商品名称")
    private String name;

    /**
     * 商品价格（单位：分）
     */
    @ApiModelProperty("价格（分）")
    private Integer price;

    /**
     * 商品图片地址
     * keyword类型，不建立索引，仅用于展示
     */
    @ApiModelProperty("商品图片")
    private String image;

    /**
     * 商品分类名称
     * keyword类型，用于过滤或聚合
     */
    @ApiModelProperty("类目名称")
    private String category;

    /**
     * 商品品牌
     * keyword类型，用于过滤或聚合
     */
    @ApiModelProperty("品牌名称")
    private String brand;

    /**
     * 商品销量
     */
    @ApiModelProperty("销量")
    private Integer sold;

    /**
     * 商品评论数量
     * 不建立索引，仅用于展示
     */
    @ApiModelProperty("评论数")
    private Integer commentCount;

    /**
     * 是否为广告商品
     * true：广告
     * false：普通商品
     */
    @ApiModelProperty("是否是推广广告，true/false")
    private Boolean isAD;

    /**
     * 商品更新时间
     * 用于排序或过滤
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}