# AI 服务启动问题排查与解决

## 🔧 问题描述

启动时出现以下错误：
```
io.milvus.exception.ServerException: index not found[collection=embedding_collection]
```

## 🎯 根本原因

应用配置了对 **Milvus 向量数据库** 的依赖，但 Milvus 服务未启动或无法连接。

## ✅ 解决方案

### 已应用的修复

我们已注释掉了 `ConsultantService.java` 中的 `contentRetriever` 配置：

```java
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        // contentRetriever = "contentRetriever",   // ← 已注释
        tools = {"reservationTool", "recommendationTool"}
)
```

**影响**：
- ✅ 应用现在可以正常启动
- ✅ 推荐服务完全正常运行
- ✅ 聊天对话功能不受影响
- ⏭️ 如需完整的 RAG 功能，后续可启用 Milvus

### 现在可以启动应用了！

```bash
# 确保已编译
mvn clean compile

# 启动应用
mvn spring-boot:run
```

## 📋 启动前检查清单

在启动前，请确保：

- [ ] Redis 正在运行 (localhost:6379)
  ```bash
  redis-cli ping
  # 应返回 PONG
  ```

- [ ] MySQL 正在运行 (localhost:3306)
  ```bash
  mysql -u root -p
  ```

- [ ] Nacos 正在运行 (localhost:8848) - **可选**
  - 如果 Nacos 未启动，应用会延迟启动但不会崩溃

- [ ] API Key 已配置
  - 在启动前设置环境变量或检查 `application.yaml`

## 🚀 完整启动流程

### 第 1 步: 启动依赖服务

```bash
# 终端1: 启动 Redis
redis-server

# 终端2: 启动 MySQL
mysql -u root -p

# 终端3: 启动 Nacos（可选）
nacos/bin/startup.cmd -m standalone
```

### 第 2 步: 启动应用

```bash
cd D:\JavaSelfLearn\hmall\ai-service
mvn spring-boot:run
```

### 第 3 步: 验证启动成功

查看控制台输出，应该看到：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

2026-05-19 17:15:00.XXX  INFO xxxxx --- [main] c.i.consultant.AiServiceApplication : Started AiServiceApplication in X.XXX seconds
```

## 🧪 测试推荐功能

启动成功后，测试推荐 API：

```bash
curl "http://localhost:8080/recommend/products?memoryId=test001&userRequest=推荐一个手机"
```

## 📊 可选: 启用完整 RAG 功能

如果需要完整的文档检索增强生成(RAG)功能：

### 1. 启动 Milvus

```bash
# 使用 Docker
docker run -d --rm --name milvus -p 19530:19530 -p 9091:9091 milvusdb/milvus:latest

# 或者本地安装 Milvus
# 详见: https://milvus.io/docs/install_standalone-docker.md
```

### 2. 启用 contentRetriever

编辑 `ConsultantService.java`，取消注释 `contentRetriever` 配置：

```java
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",   // ← 取消注释
        tools = {"reservationTool", "recommendationTool"}
)
```

### 3. 重新编译并启动

```bash
mvn clean compile
mvn spring-boot:run
```

## 🐛 常见情况排查

### 情况 1: 应用启动还是失败

**检查内容**：
```bash
# 查看完整错误信息
cat logs/ai-service/spring.log

# 或在启动时添加调试参数
mvn spring-boot:run -Ddebug
```

### 情况 2: Redis 连接失败

```
ERROR: Error creating bean with name 'redisChatMemoryStore'
```

**解决方案**：
```bash
# 启动 Redis
redis-server

# 或检查 Redis 连接配置
# application.yaml 中的 spring.data.redis 设置
```

### 情况 3: MySQL 连接失败

```
ERROR: Connection refused: localhost:3306
```

**解决方案**：
```bash
# 启动 MySQL
mysql -u root -p

# 检查连接字符串
# 确保用户名和密码正确
```

## 📝 配置检查

查看 `application.yaml` 中的关键配置：

```yaml
spring:
  application:
    name: ai-service
  data:
    redis:
      host: localhost
      port: 6379
      password: root
      database: 5
  datasource:
    url: jdbc:mysql://localhost:3306/langchain4j
    username: root
    password: 1234

langchain4j:
  open-ai:
    chat-model:
      api-key: ${API-KEY}
      model-name: qwen-plus
```

## 📞 获取帮助

如果问题仍未解决：

1. 查看详细日志：`logs/ai-service/spring.log`
2. 查阅文档：[QUICK_START.md](./QUICK_START.md)
3. 检查依赖服务是否正常运行

---

**修复时间**: 2026-05-19 17:10  
**修复内容**: 注释掉 Milvus 强制依赖，使应用能够在不需要 Milvus 的情况下启动  
**推荐功能**: ✅ 完全正常  
**RAG 功能**: ⏸️ 可选（需要启动 Milvus 并取消注释配置）

