# AI 服务商品推荐 - 快速启动指南

## ⚠️ 已知问题修复说明

**如果启动时出现 Milvus 连接错误**：这已经被修复了！

我们已注释掉了 Milvus 的强制依赖，你现在可以直接启动应用。
详见：[STARTUP_TROUBLESHOOTING.md](./STARTUP_TROUBLESHOOTING.md)

---

## 🚀 快速开始（5分钟）

### 第1步: 启动必要的基础设施

#### 1.1 启动 Redis（必需）
```bash
# Windows 下
redis-server.exe

# Linux/Mac
redis-server
```

#### 1.2 启动 MySQL（必需）
```bash
# 创建数据库（如未创建）
mysql -u root -p

> CREATE DATABASE langchain4j CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> CREATE DATABASE hmall CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 1.3 启动 Nacos（可选，但推荐）
```bash
# Windows
cd nacos/bin
startup.cmd -m standalone

# Linux/Mac
cd nacos/bin
./startup.sh -m standalone
```

访问: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos

### 第2步: 启动核心服务

按以下顺序启动服务（在不同的终端窗口）：

```bash
# 1. 启动 item-service（必须先启动，因为 ai-service 需要调用它）
cd item-service
mvn spring-boot:run

# 2. 启动 ai-service
cd ai-service
mvn spring-boot:run
```

**检查启动成功的标志**:
- ✅ 控制台出现 "Started ConsultantApplication in X seconds"
- ✅ Nacos 中能看到 ai-service 和 item-service 已注册

### 第3步: 测试推荐功能

#### 使用 curl 测试
```bash
# 测试 1: 推荐手机
curl "http://localhost:8080/recommend/products?memoryId=test001&userRequest=推荐一个性价比高的手机"

# 测试 2: 热销商品
curl "http://localhost:8080/recommend/products?userRequest=有什么热销的商品"

# 测试 3: 推荐家电
curl "http://localhost:8080/recommend/products?memoryId=test002&userRequest=我要买家电，有什么推荐吗"
```

#### 使用 Postman 测试
1. 新建 GET 请求
2. URL: `http://localhost:8080/recommend/products`
3. Query Params:
   - `memoryId`: user123
   - `userRequest`: 推荐一个手机
4. 点击 Send，观察流式响应

## 📊 系统架构

```
┌──────────────────────────────────────────┐
│        HTTP 客户端 (curl/Postman)         │
│              :8080                        │
└────────────────────┬─────────────────────┘
                     │ /recommend/products
                     ↓
         ┌───────────────────────┐
         │   ai-service:8080     │ ← Nacos 注册
         │  (商品推荐服务)        │
         └──────────┬────────────┘
                    │ 
         ┌──────────┴─────────────────┐
         │ OpenFeign + Nacos 发现      │
         ↓                            ↓
    ┌─────────────┐          ┌──────────────┐
    │ item-service│          │    Redis     │
    │  :8081      │          │ localhost:63 │
    │  (商品数据)  │          │     79       │
    └─────────────┘          └──────────────┘
```

## 🔧 常见问题排查

### 问题 1: ai-service 启动失败，提示找不到 item-service

**原因**: item-service 未启动或未在 Nacos 中注册

**解决方案**:
```bash
# 1. 检查 item-service 是否启动
curl http://localhost:8081/health

# 2. 检查 Nacos 中是否注册
访问 http://localhost:8848/nacos
在 "服务管理" → "服务列表" 中查找 item-service

# 3. 检查 application.yaml 中的服务名是否正确
# item-service 中应该有:
spring:
  application:
    name: item-service
```

### 问题 2: 推荐请求返回空结果

**可能的原因与解决方案**:

```bash
# 1. 检查数据库中是否有商品数据
mysql> USE hmall;
mysql> SELECT COUNT(*) FROM item;

# 2. 检查 Redis 连接
redis-cli ping
# 应返回 PONG

# 3. 检查 API Key 是否配置
# 在 application.yaml 中检查 API-KEY 环境变量
```

### 问题 3: OpenFeign 调用超时

**解决方案**:
```yaml
# 在 application.yaml 中添加超时配置
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
```

### 问题 4: 找不到 ItemDTO 类

**解决方案**:
```bash
# 1. 确保 hm-api 模块已编译
cd hm-api
mvn clean install

# 2. 刷新 Maven 缓存
cd ai-service
mvn clean install
```

## 📝 配置文件检查清单

### application.yaml 必要配置

```yaml
spring:
  application:
    name: ai-service                    # ✓ 必需
  cloud:
    nacos:
      server-addr: localhost:8848       # ✓ Nacos 地址
      discovery:
        namespace: hmall                # ✓ 命名空间
  data:
    redis:
      host: localhost                   # ✓ Redis 主机
      port: 6379
      password: root

langchain4j:
  open-ai:
    chat-model:
      api-key: ${API-KEY}              # ✓ 需要设置环境变量
      model-name: qwen-plus
```

### 环境变量配置

```bash
# 设置 API Key（根据你的 AI 服务供应商）
# Windows
set API-KEY=sk-xxxx...

# Linux/Mac
export API-KEY=sk-xxxx...
```

## 🧪 完整测试流程

### 测试 1: 实时推荐（命令行）
```bash
curl -v "http://localhost:8080/recommend/products?memoryId=user001&userRequest=推荐一个5G手机"
```

**预期输出**:
```
【HONOR Magic 6 Pro】
  品牌：HONOR
  分类：手机
  价格：¥3999.00
  销量：5623件
  评论数：12450条
  库存：120件
```

### 测试 2: 多轮对话保持上下文
```bash
# 第一轮对话
curl "http://localhost:8080/recommend/products?memoryId=user002&userRequest=我想买一个手机"

# 第二轮对话（使用相同的 memoryId）
curl "http://localhost:8080/recommend/products?memoryId=user002&userRequest=有更便宜的吗"
```

### 测试 3: 集成测试（Postman 集合）

```json
{
  "info": {
    "name": "AI 推荐服务测试",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "推荐手机",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/recommend/products?memoryId=test&userRequest=推荐一个手机"
      }
    },
    {
      "name": "热销商品",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/recommend/products?userRequest=有什么热销的"
      }
    }
  ]
}
```

## 💡 性能优化建议

### 1. 启用异步处理
```java
@Async
public Flux<String> recommendProducts(String memoryId, String userRequest) {
    return productRecommendationService.recommendProducts(memoryId, userRequest);
}
```

### 2. 添加缓存
```java
@Cacheable("product-recommendations")
public String getHotItems() {
    return recommendationTool.getHotItems();
}
```

### 3. 连接池配置
```yaml
feign:
  client:
    config:
      default:
        poolName: "ai-service-pool"
```

## 📚 相关文档

- [RECOMMENDATION_GUIDE.md](./RECOMMENDATION_GUIDE.md) - 详细功能指南
- [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md) - 实现细节总结

## ✅ 检查清单

在转移到生产环境前，请确保：

- [ ] 所有依赖服务已启动（Nacos、Redis、MySQL）
- [ ] item-service 已在 Nacos 中注册
- [ ] API Key 环境变量已设置
- [ ] 数据库中有示例数据
- [ ] 推荐接口能够正常调用
- [ ] 流式响应格式正确
- [ ] 多轮对话能保持上下文

## 🚨 故障恢复

### 服务意外退出

```bash
# 查看日志
cat logs/ai-service/spring.log | tail -100

# 重启服务
cd ai-service
mvn clean spring-boot:run
```

### 清理缓存重新开始

```bash
# 清理 Redis 缓存
redis-cli
> FLUSHDB

# 清理 Maven 缓存
mvn clean install -DskipTests

# 重启所有服务
```

---

**文档更新时间**: 2026-05-19  
**版本**: 1.0.0
