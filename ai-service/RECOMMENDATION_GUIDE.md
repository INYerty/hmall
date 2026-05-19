# AI 服务商品推荐模块使用指南

## 功能概述

AI 服务模块现已集成商品推荐功能，用户可以通过自然语言请求来获取个性化的商品推荐。

## 快速开始

### 1. API 端点

#### 商品推荐接口
- **URL**: `GET /recommend/products`
- **Content-Type**: `text/event-stream;charset=UTF-8` (流式响应)
- **参数**:
  - `memoryId` (可选): 会话 ID，用于保持对话上下文。默认值: "default"
  - `userRequest` (必需): 用户的推荐请求

### 2. 使用示例

#### 示例 1: 推荐手机
```bash
curl "http://localhost:8080/recommend/products?memoryId=user123&userRequest=推荐一个性价比高的手机"
```

#### 示例 2: 获取热销商品
```bash
curl "http://localhost:8080/recommend/products?userRequest=有什么热销的商品吗？"
```

#### 示例 3: 推荐家电
```bash
curl "http://localhost:8080/recommend/products?memoryId=user123&userRequest=我需要购买家电，请推荐一些"
```

### 3. 响应示例

```
【HONOR Magic 6 Pro】
  品牌：HONOR
  分类：手机
  价格：¥3999.00
  销量：5623件
  评论数：12450条
  库存：120件

【iPhone 15 Pro】
  品牌：Apple
  分类：手机
  价格：¥7999.00
  销量：8932件
  评论数：15632条
  库存：85件
```

## 核心组件

### 1. RecommendationTool
位置: `com.inyert.consultant.tool.RecommendationTool`

提供两个主要工具函数:
- `recommendItems(keyword, category)`: 根据关键词和分类推荐商品
- `getHotItems()`: 获取热销商品列表

### 2. ProductRecommendationService
位置: `com.inyert.consultant.service.ProductRecommendationService`

业务层接口，处理商品推荐逻辑。

### 3. RecommendationController
位置: `com.inyert.consultant.controller.RecommendationController`

HTTP 控制层，暴露推荐 API 端点。

### 4. ItemClient
位置: `com.inyert.consultant.client.ItemClient`

OpenFeign 客户端，用于调用 item-service 获取商品详情。

## 对话上下文管理

系统支持基于 `memoryId` 的对话上下文保留:

```bash
# 第一次请求 - 询问需求
curl "http://localhost:8080/recommend/products?memoryId=user123&userRequest=我想买一部手机"

# 第二次请求 - 追问详情（AI 会记住之前的对话）
curl "http://localhost:8080/recommend/products?memoryId=user123&userRequest=预算在3000以内可以吗？"
```

## 配置说明

### application.yaml 主要配置
```yaml
spring:
  application:
    name: ai-service
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: hmall
```

### 所需环境
1. **Nacos**: 用于服务发现（item-service 必须在 Nacos 中注册）
2. **Redis**: 用于会话存储和向量数据库
3. **MySQL**: 用于本地数据存储
4. **OpenAI 兼容服务**: 当前使用阿里云通义千问

## FAQ

### Q: 如何自定义推荐策略？
A: 编辑 `RecommendationTool` 中的 `recommendItems` 方法，修改商品查询逻辑。

### Q: 推荐结果能否持久化？
A: 可以在 `RecommendationServiceImpl` 中添加日志存储逻辑。

### Q: 如何扩展推荐功能？
A: 在 `RecommendationTool` 中添加新的 `@Tool` 方法即可。

## 故障排查

### 1. 404 错误
- 检查 item-service 是否已启动
- 检查 Nacos 中是否注册了 item-service

### 2. 空推荐结果
- 检查数据库中是否有相应分类的商品
- 检查 ItemClient 的 ID 映射是否正确

### 3. AI 不调用推荐工具
- 检查系统提示词（system.txt）是否已更新
- 确保 RecommendationTool 已注入为 Bean

## 开发建议

1. **会话管理**: 使用用户 ID 作为 `memoryId`，而不是 "default"
2. **错误处理**: 推荐工具已包含基本错误处理，但建议添加更详细的异常日志
3. **性能优化**: 可以添加推荐结果缓存机制
4. **用户反馈**: 考虑添加反馈接口来改进推荐算法

