# AI 服务商品推荐功能 - 项目文件结构

## 📂 完整的项目文件树

```
hmall/
├── pom.xml (已更新 - 添加 ai-service 模块)
│
├── ai-service/
│   ├── pom.xml (已更新)
│   │   └── 新增依赖:
│   │       - spring-cloud-starter-openfeign
│   │       - spring-cloud-starter-alibaba-nacos-discovery
│   │       - hm-api
│   │
│   ├── src/main/java/com/inyert/consultant/
│   │   ├── ConsultantApplication.java (已更新)
│   │   │   └── 新增: @EnableFeignClients
│   │   │
│   │   ├── client/ (新建)
│   │   │   └── ItemClient.java (新建)
│   │   │       └── OpenFeign 客户端，调用 item-service
│   │   │
│   │   ├── controller/
│   │   │   ├── ChatController.java (原有)
│   │   │   └── RecommendationController.java (新建)
│   │   │       └── 商品推荐 HTTP 端点
│   │   │
│   │   ├── controller/aiservice/
│   │   │   └── ConsultantService.java (已更新)
│   │   │       └── 配置 RecommendationTool
│   │   │
│   │   ├── service/
│   │   │   ├── ReservationService.java (原有)
│   │   │   └── ProductRecommendationService.java (新建)
│   │   │       └── 推荐业务服务接口
│   │   │
│   │   ├── service/impl/
│   │   │   ├── ReservationImpl.java (原有)
│   │   │   └── ProductRecommendationServiceImpl.java (新建)
│   │   │       └── 推荐业务实现
│   │   │
│   │   ├── tool/
│   │   │   └── RecommendationTool.java (新建)
│   │   │       ├── @Tool: recommendItems(keyword, category)
│   │   │       └── @Tool: getHotItems()
│   │   │
│   │   ├── config/ (原有)
│   │   ├── mapper/ (原有)
│   │   ├── po/ (原有)
│   │   └── repository/ (原有)
│   │
│   ├── src/main/resources/
│   │   ├── application.yaml (已更新)
│   │   │   └── 新增 Nacos 配置
│   │   └── system.txt (已更新)
│   │       └── 更新 AI 系统提示词
│   │
│   ├── QUICK_START.md (新建)
│   │   └── 快速启动指南
│   ├── RECOMMENDATION_GUIDE.md (新建)
│   │   └── 推荐功能使用指南
│   └── IMPLEMENTATION_SUMMARY.md (新建)
│       └── 实现完成总结
│
├── item-service/ (依赖的服务)
│   └── 提供商品数据
│
└── hm-api/
    ├── ItemDTO.java (被 ai-service 引用)
    └── ItemClient.java (被 ai-service 引用)
```

## 📋 新增/更新文件清单

### 新建文件（5个）

| 文件 | 路径 | 描述 |
|------|------|------|
| ItemClient.java | `client/` | OpenFeign 客户端 |
| RecommendationTool.java | `tool/` | AI 工具函数 |
| ProductRecommendationService.java | `service/` | 推荐业务接口 |
| ProductRecommendationServiceImpl.java | `service/impl/` | 推荐业务实现 |
| RecommendationController.java | `controller/` | HTTP 推荐端点 |

### 更新文件（5个）

| 文件 | 修改内容 | 描述 |
|------|---------|------|
| ../pom.xml | `<module>ai-service</module>` | 添加模块 |
| pom.xml | 添加 3 个依赖 | OpenFeign、Nacos、hm-api |
| ConsultantApplication.java | 添加 @EnableFeignClients | 启用 OpenFeign |
| ConsultantService.java | 配置 RecommendationTool | 添加工具到 AiService |
| application.yaml | 添加 Nacos 配置 | 服务发现配置 |
| system.txt | 更新系统提示词 | 商品推荐指导 |

### 新建文档（3个）

| 文件 | 描述 |
|------|------|
| QUICK_START.md | 5分钟快速启动指南 |
| RECOMMENDATION_GUIDE.md | 详细功能使用指南 |
| IMPLEMENTATION_SUMMARY.md | 实现完成总结 |

## 🔄 文件依赖关系

```
RecommendationController.java
    ↓ 依赖
ProductRecommendationService.java
    ↓ 依赖
ProductRecommendationServiceImpl.java
    ↓ 依赖
ConsultantService.java
    ↓ 依赖
RecommendationTool.java
    ↓ 依赖
ItemClient.java (OpenFeign)
    ↓ 依赖
hm-api ItemDTO
    ↓ 依赖
item-service (REST API)
```

## 🔌 外部集成

### 直接调用
- **ConsultantApplication** → 启动时自动发现并加载 OpenFeign 客户端
- **RecommendationController** → Spring MVC 自动扫描并注册

### 通过 OpenFeign 调用
- **ItemClient** → item-service (REST API)
  - 地址通过 Nacos 服务发现动态获取
  - 服务名: `item-service`

### 通过 AI 框架调用
- **RecommendationTool** → LangChain4j Tool 框架
  - AI 自动识别并调用 Tool 方法
  - 方法由 @Tool 注解标记

## 📊 核心类关系图

```
┌─────────────────────────────────┐
│   RecommendationController      │
│   (REST 层)                      │
└──────────────┬──────────────────┘
               │ 注入
               ↓
┌─────────────────────────────────┐
│  ProductRecommendationService   │
│  (接口 - 业务层)                 │
└──────────────┬──────────────────┘
               │ 实现
               ↓
┌─────────────────────────────────┐
│  ProductRecommendationServiceImpl│
│  (实现 - 业务层)                 │
└──────────────┬──────────────────┘
               │ 注入
               ↓
┌─────────────────────────────────┐
│     ConsultantService           │
│     (AI 服务层 - 接口)           │
│     配置:                        │
│     - tools = RecommendationTool│
│     - chatModel                  │
│     - streamingChatModel         │
└──────────────┬──────────────────┘
               │ 使用
               ↓
┌─────────────────────────────────┐
│      RecommendationTool         │
│      (@Tool 方法集合)            │
│      - recommendItems()          │
│      - getHotItems()             │
└──────────────┬──────────────────┘
               │ 调用
               ↓
┌─────────────────────────────────┐
│       ItemClient                │
│       (OpenFeign 客户端)         │
│       queryItemByIds()           │
└──────────────┬──────────────────┘
               │ Nacos 发现
               ↓
        item-service
```

## 📐 API 端点映射

### 推荐接口

```
HTTP 端点
  GET /recommend/products
  
Query 参数
  ├── memoryId: String (可选)
  │   └── 用途: 保持对话上下文
  │   └── 默认值: "default"
  │
  └── userRequest: String (必需)
      └── 用途: 用户的推荐请求
      └── 示例: "推荐一个手机"

Response
  ├── Content-Type: text/event-stream;charset=UTF-8
  ├── 格式: Server-Sent Events (流式)
  └── 数据: 推荐商品详情 (实时流)
```

## 🔐 权限和安全配置

### 当前配置（开发环境）
- ✅ 无认证要求
- ✅ CORS 默认允许
- ✅ 日志输出到文件

### 生产环境建议
- [ ] 添加 JWT 认证
- [ ] 配置 CORS 白名单
- [ ] 添加请求频率限制
- [ ] 启用 HTTPS

## 🧬 数据流向

```
用户输入
  └─> HTTP GET /recommend/products
        └─> RecommendationController
              └─> ProductRecommendationService.recommendProducts()
                    └─> ConsultantService.chat()
                          ↓
                    AI 分析请求
                          ↓
                    调用 RecommendationTool
                          ├─> recommendItems(keyword, category)
                          │     └─> ItemClient.queryItemByIds()
                          │           └─> Nacos: item-service
                          │                 └─> 数据库查询
                          │                       └─> 返回商品列表
                          │
                          └─> getHotItems()
                                └─> ItemClient.queryItemByIds()
                                      └─> item-service
                                            └─> 返回热销商品
                    ↓
              格式化推荐结果
                    ↓
              流式返回给用户
```

## 💾 编译生成物

编译后生成的 JAR 包：

```
ai-service/target/
├── ai-service-0.0.1-SNAPSHOT.jar (可执行 JAR)
├── ai-service-0.0.1-SNAPSHOT.jar.original (未扩展的 JAR)
├── classes/ (编译后的 class 文件)
│   └── com/inyert/consultant/
│       ├── ConsultantApplication.class
│       ├── client/ItemClient.class
│       ├── controller/RecommendationController.class
│       ├── service/ProductRecommendationService.class
│       ├── service/impl/ProductRecommendationServiceImpl.class
│       └── tool/RecommendationTool.class
└── maven-status/ (Maven 构建状态)
```

## ✨ 关键特性点

| 特性 | 实现位置 | 状态 |
|------|---------|------|
| 商品推荐 | RecommendationTool | ✅ |
| 热销查询 | RecommendationTool | ✅ |
| 对话上下文 | ConsultantService + Redis | ✅ |
| 服务发现 | ItemClient + Nacos | ✅ |
| 流式响应 | RecommendationController | ✅ |
| 错误处理 | RecommendationTool | ✅ |
| 日志记录 | Spring Boot 默认 | ✅ |

---

**最后更新**: 2026-05-19

