# HMall 电商项目 Code Wiki

## 一、项目概述

HMall 是一个基于 Spring Boot 构建的电商平台后端服务项目，支持单体应用和微服务两种运行模式。项目采用 MyBatis Plus 作为 ORM 框架，使用 JWT 进行身份认证，集成了 RabbitMQ 消息队列，并支持 Nacos 服务注册与配置管理。

## 二、项目整体架构

### 2.1 模块结构

```
hmall/
├── hm-service/          # 单体服务（核心业务）
├── hm-common/           # 公共模块（工具类、配置、异常处理）
├── hm-api/              # API模块（Feign客户端、DTO定义）
├── hm-gateway/          # 网关服务（微服务模式）
├── item-service/        # 商品服务（微服务模式）
├── cart-service/        # 购物车服务（微服务模式）
├── user-service/        # 用户服务（微服务模式）
├── trade-service/       # 交易服务（微服务模式）
├── pay-service/         # 支付服务（微服务模式）
├── ai-service/          # AI服务
└── hmall-nginx/         # Nginx前端静态资源
```

### 2.2 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 11 | 开发语言 |
| Spring Boot | 2.7.12 | 应用框架 |
| Spring Cloud | 2021.0.3 | 微服务框架 |
| Spring Cloud Alibaba | 2021.0.4.0 | 服务治理 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0.23 | 数据库 |
| RabbitMQ | - | 消息队列 |
| Redis | - | 缓存 |
| JWT | - | 身份认证 |
| Hutool | 5.8.11 | 工具类库 |
| Knife4j | 4.1.0 | API文档 |

### 2.3 数据流向

```
用户请求 → Gateway → 各微服务/单体服务
                            ↓
                        MySQL数据库
                            ↓
                        Redis缓存（可选）
                            ↓
                        RabbitMQ消息队列（异步处理）
```

## 三、核心模块详解

### 3.1 hm-common（公共模块）

#### 3.1.1 核心类

**[R.java](file:///d:/JavaSelfLearn/hmall/hm-common/src/main/java/com/hmall/common/domain/R.java)** - 统一响应封装类

```java
public class R<T> {
    private int code;      // 状态码
    private String msg;    // 提示信息
    private T data;        // 数据
    
    public static <T> R<T> ok(T data);       // 成功响应
    public static <T> R<T> error(String msg); // 错误响应
    public static <T> R<T> error(int code, String msg); // 自定义错误响应
}
```

**[PageDTO.java](file:///d:/JavaSelfLearn/hmall/hm-common/src/main/java/com/hmall/common/domain/PageDTO.java)** - 分页响应封装类

```java
public class PageDTO<T> {
    protected Long total;   // 总记录数
    protected Long pages;   // 总页数
    protected List<T> list; // 数据列表
    
    public static <T> PageDTO<T> of(Page<R> page, Class<T> clazz); // 从MyBatis Plus分页对象转换
}
```

**[PageQuery.java](file:///d:/JavaSelfLearn/hmall/hm-common/src/main/java/com/hmall/common/domain/PageQuery.java)** - 分页查询条件

```java
public class PageQuery {
    private Integer pageNo = 1;     // 页码
    private Integer pageSize = 20;  // 每页数量
    private Boolean isAsc = true;   // 是否升序
    private String sortBy;          // 排序字段
    
    public <T> Page<T> toMpPage(String defaultSortBy, boolean isAsc); // 转换为MyBatis Plus分页对象
}
```

**[UserContext.java](file:///d:/JavaSelfLearn/hmall/hm-common/src/main/java/com/hmall/common/utils/UserContext.java)** - 用户上下文（ThreadLocal）

```java
public class UserContext {
    public static void setUser(Long userId); // 设置当前登录用户
    public static Long getUser();           // 获取当前登录用户
    public static void removeUser();        // 移除当前登录用户
}
```

#### 3.1.2 异常体系

| 异常类 | 用途 |
|--------|------|
| `CommonException` | 基础异常类 |
| `BadRequestException` | 请求参数错误 |
| `BizIllegalException` | 业务逻辑异常 |
| `ForbiddenException` | 禁止访问 |
| `UnauthorizedException` | 未授权 |
| `DbException` | 数据库异常 |

### 3.2 hm-service（单体服务）

#### 3.2.1 领域模型（PO）

**[User.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/domain/po/User.java)** - 用户实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| password | String | 密码（加密存储） |
| phone | String | 手机号 |
| balance | Integer | 账户余额（分） |
| status | UserStatus | 用户状态 |
| createTime | LocalDateTime | 创建时间 |

**[Item.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/domain/po/Item.java)** - 商品实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 商品ID |
| name | String | SKU名称 |
| price | Integer | 价格（分） |
| stock | Integer | 库存数量 |
| image | String | 商品图片 |
| category | String | 类目名称 |
| brand | String | 品牌名称 |
| spec | String | 规格 |
| sold | Integer | 销量 |
| status | Integer | 商品状态（1正常/2下架/3删除） |

**[Cart.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/domain/po/Cart.java)** - 购物车实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 购物车条目ID |
| userId | Long | 用户ID |
| itemId | Long | 商品ID |
| num | Integer | 购买数量 |
| name | String | 商品标题 |
| spec | String | 商品规格 |
| price | Integer | 价格（分） |
| image | String | 商品图片 |

**[Order.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/domain/po/Order.java)** - 订单实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID（雪花算法） |
| totalFee | Integer | 总金额（分） |
| paymentType | Integer | 支付类型（1支付宝/2微信/3余额） |
| userId | Long | 用户ID |
| status | Integer | 订单状态（1未付款/2已付款未发货/3已发货/4交易成功/5交易取消/6已评价） |
| payTime | LocalDateTime | 支付时间 |
| consignTime | LocalDateTime | 发货时间 |
| endTime | LocalDateTime | 交易完成时间 |

**[PayOrder.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/domain/po/PayOrder.java)** - 支付订单实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 支付单ID |
| bizOrderNo | Long | 业务订单号 |
| payOrderNo | Long | 支付单号 |
| amount | Integer | 支付金额（分） |
| payChannelCode | String | 支付渠道编码 |
| payType | Integer | 支付类型 |
| status | Integer | 支付状态（0待提交/1待支付/2已关闭/3支付成功） |
| qrCodeUrl | String | 支付二维码链接 |

**[Address.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/domain/po/Address.java)** - 地址实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 地址ID |
| userId | Long | 用户ID |
| province | String | 省 |
| city | String | 市 |
| town | String | 县/区 |
| street | String | 详细地址 |
| mobile | String | 手机号 |
| contact | String | 联系人 |
| isDefault | Integer | 是否默认（1默认/0否） |

#### 3.2.2 枚举类型

**[UserStatus.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/enums/UserStatus.java)** - 用户状态

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | FROZEN | 禁止使用 |
| 1 | NORMAL | 已激活 |

**[PayStatus.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/enums/PayStatus.java)** - 支付状态

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | NOT_COMMIT | 未提交 |
| 1 | WAIT_BUYER_PAY | 待支付 |
| 2 | TRADE_CLOSED | 已关闭 |
| 3 | TRADE_SUCCESS | 支付成功 |

**[PayType.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/enums/PayType.java)** - 支付类型

| 值 | 名称 | 说明 |
|----|------|------|
| 1 | JSAPI | 网页支付JS |
| 2 | MINI_APP | 小程序支付 |
| 3 | APP | APP支付 |
| 4 | NATIVE | 扫码支付 |
| 5 | BALANCE | 余额支付 |

**[PayChannel.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/enums/PayChannel.java)** - 支付渠道

| 值 | 说明 |
|----|------|
| wxPay | 微信支付 |
| aliPay | 支付宝支付 |
| balance | 余额支付 |

#### 3.2.3 Service层

**[IUserService](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/service/IUserService.java)** - 用户服务接口

```java
public interface IUserService extends IService<User> {
    UserLoginVO login(LoginFormDTO loginFormDTO);  // 用户登录
    void deductMoney(String pw, Integer totalFee); // 扣减余额
}
```

**[IItemService](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/service/IItemService.java)** - 商品服务接口

```java
public interface IItemService extends IService<Item> {
    void deductStock(List<OrderDetailDTO> items);       // 批量扣减库存
    List<ItemDTO> queryItemByIds(Collection<Long> ids); // 批量查询商品
}
```

**[ICartService](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/service/ICartService.java)** - 购物车服务接口

```java
public interface ICartService extends IService<Cart> {
    void addItem2Cart(CartFormDTO cartFormDTO);         // 添加商品到购物车
    List<CartVO> queryMyCarts();                        // 查询我的购物车
    void removeByItemIds(Collection<Long> itemIds);     // 批量删除购物车商品
}
```

**[IOrderService](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/service/IOrderService.java)** - 订单服务接口

```java
public interface IOrderService extends IService<Order> {
    Long createOrder(OrderFormDTO orderFormDTO);  // 创建订单
    void markOrderPaySuccess(Long orderId);       // 标记订单已支付
}
```

**[IPayOrderService](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/service/IPayOrderService.java)** - 支付服务接口

```java
public interface IPayOrderService extends IService<PayOrder> {
    String applyPayOrder(PayApplyDTO applyDTO);              // 生成支付单
    void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO); // 余额支付
}
```

#### 3.2.4 Controller层

**[UserController](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/controller/UserController.java)** - 用户控制器

| API路径 | 方法 | 说明 |
|---------|------|------|
| `/users/login` | POST | 用户登录 |
| `/users/money/deduct` | PUT | 扣减余额 |

**[ItemController](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/controller/ItemController.java)** - 商品控制器

| API路径 | 方法 | 说明 |
|---------|------|------|
| `/items/page` | GET | 分页查询商品 |
| `/items` | GET | 批量查询商品 |
| `/items/{id}` | GET | 根据ID查询商品 |
| `/items` | POST | 新增商品 |
| `/items` | PUT | 更新商品 |
| `/items/status/{id}/{status}` | PUT | 更新商品状态 |
| `/items/{id}` | DELETE | 删除商品 |
| `/items/stock/deduct` | PUT | 批量扣减库存 |

**[CartController](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/controller/CartController.java)** - 购物车控制器

| API路径 | 方法 | 说明 |
|---------|------|------|
| `/carts` | POST | 添加商品到购物车 |
| `/carts` | PUT | 更新购物车数据 |
| `/carts/{id}` | DELETE | 删除购物车商品 |
| `/carts` | GET | 查询购物车列表 |
| `/carts` | DELETE | 批量删除购物车商品 |

**[OrderController](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/controller/OrderController.java)** - 订单控制器

| API路径 | 方法 | 说明 |
|---------|------|------|
| `/orders/{id}` | GET | 根据ID查询订单 |
| `/orders` | POST | 创建订单 |
| `/orders/{orderId}` | PUT | 标记订单已支付 |

**[PayController](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/controller/PayController.java)** - 支付控制器

| API路径 | 方法 | 说明 |
|---------|------|------|
| `/pay-orders` | POST | 生成支付单 |
| `/pay-orders/{id}` | POST | 尝试余额支付 |

#### 3.2.5 核心业务流程

**订单创建流程**（[OrderServiceImpl.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java)）：

```
1. 接收订单表单 → 2. 查询商品信息 → 3. 计算订单总价 → 4. 保存订单 → 5. 保存订单详情 → 6. 清理购物车 → 7. 扣减库存
```

**支付流程**（[PayOrderServiceImpl.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java)）：

```
1. 生成支付单（幂等性校验）→ 2. 用户提交支付 → 3. 扣减余额 → 4. 更新支付单状态 → 5. 更新订单状态
```

### 3.3 hm-api（API模块）

#### 3.3.1 Feign客户端

| 客户端类 | 服务名 | 用途 |
|----------|--------|------|
| [ItemClient](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/client/ItemClient.java) | item-service | 商品服务调用 |
| [CartClient](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/client/CartClient.java) | cart-service | 购物车服务调用 |
| [UserClient](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/client/UserClient.java) | user-service | 用户服务调用 |
| [TradeClient](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/client/TradeClient.java) | trade-service | 交易服务调用 |
| [PayClient](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/client/PayClient.java) | pay-service | 支付服务调用 |

#### 3.3.2 DTO定义

| DTO类 | 用途 |
|-------|------|
| [ItemDTO](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/dto/ItemDTO.java) | 商品数据传输对象 |
| [OrderDetailDTO](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/dto/OrderDetailDTO.java) | 订单详情数据传输对象 |
| [PayOrderDTO](file:///d:/JavaSelfLearn/hmall/hm-api/src/main/java/com/hmall/api/dto/PayOrderDTO.java) | 支付订单数据传输对象 |

### 3.4 hm-gateway（网关服务）

网关服务负责请求路由和认证鉴权，配置了 JWT 认证拦截器和 Nacos 服务发现。

**配置文件**：[application.yaml](file:///d:/JavaSelfLearn/hmall/hm-gateway/src/main/resources/application.yaml)

```yaml
# JWT配置
hm:
  jwt:
    location: classpath:hmall.jks
    alias: hmall
    password: hmall123
    tokenTTL: 30m
  auth:
    excludePaths:  # 免认证路径
      - /search/**
      - /users/login
      - /items/**
      - /hi
```

## 四、数据库设计

### 4.1 数据库表结构

```sql
-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    password VARCHAR(255),
    phone VARCHAR(20),
    balance INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 商品表
CREATE TABLE item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200),
    price INT,
    stock INT,
    image VARCHAR(500),
    category VARCHAR(50),
    brand VARCHAR(50),
    spec VARCHAR(500),
    sold INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    isAD BOOLEAN DEFAULT FALSE,
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME,
    creater BIGINT,
    updater BIGINT
);

-- 购物车表
CREATE TABLE cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    item_id BIGINT,
    num INT DEFAULT 1,
    name VARCHAR(200),
    spec VARCHAR(500),
    price INT,
    image VARCHAR(500),
    create_time DATETIME,
    update_time DATETIME
);

-- 订单表
CREATE TABLE `order` (
    id BIGINT PRIMARY KEY,
    total_fee INT,
    payment_type INT,
    user_id BIGINT,
    status INT DEFAULT 1,
    create_time DATETIME,
    pay_time DATETIME,
    consign_time DATETIME,
    end_time DATETIME,
    close_time DATETIME,
    comment_time DATETIME,
    update_time DATETIME
);

-- 订单详情表
CREATE TABLE order_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT,
    item_id BIGINT,
    name VARCHAR(200),
    spec VARCHAR(500),
    price INT,
    num INT,
    image VARCHAR(500)
);

-- 支付订单表
CREATE TABLE pay_order (
    id BIGINT PRIMARY KEY,
    biz_order_no BIGINT,
    pay_order_no BIGINT,
    biz_user_id BIGINT,
    pay_channel_code VARCHAR(50),
    amount INT,
    pay_type INT,
    status INT DEFAULT 0,
    expand_json TEXT,
    result_code VARCHAR(50),
    result_msg VARCHAR(500),
    pay_success_time DATETIME,
    pay_over_time DATETIME,
    qr_code_url VARCHAR(500),
    create_time DATETIME,
    update_time DATETIME,
    creater BIGINT,
    updater BIGINT,
    is_delete BOOLEAN DEFAULT FALSE
);

-- 地址表
CREATE TABLE address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    province VARCHAR(50),
    city VARCHAR(50),
    town VARCHAR(50),
    mobile VARCHAR(20),
    street VARCHAR(500),
    contact VARCHAR(50),
    is_default INT DEFAULT 0,
    notes VARCHAR(200)
);
```

## 五、依赖关系

### 5.1 模块依赖

```
hm-service
    ├── hm-common (工具类、异常处理)
    ├── spring-boot-starter-web (Web框架)
    ├── spring-security-crypto (密码加密)
    ├── mysql-connector-java (MySQL驱动)
    ├── mybatis-plus-boot-starter (ORM框架)
    └── spring-boot-starter-data-redis (Redis缓存)

hm-common
    ├── hutool-all (工具类库)
    ├── spring-webmvc (MVC框架)
    ├── spring-boot-starter-logging (日志)
    ├── mybatis-plus-core (MyBatis Plus核心)
    ├── hibernate-validator (参数校验)
    ├── spring-boot-autoconfigure (自动配置)
    ├── knife4j-openapi2 (API文档)
    ├── caffeine (本地缓存)
    └── spring-rabbit (消息队列)

hm-api
    ├── spring-cloud-starter-openfeign (Feign客户端)
    ├── spring-cloud-starter-loadbalancer (负载均衡)
    └── hm-common (公共模块)
```

## 六、项目运行方式

### 6.1 环境要求

- JDK 11+
- MySQL 8.0+
- Maven 3.6+
- Nacos 2.0+（微服务模式）
- RabbitMQ（可选，消息队列）
- Redis（可选，缓存）

### 6.2 单体模式运行

**步骤1：配置数据库**

修改 [application.yaml](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/resources/application.yaml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hmall?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

**步骤2：运行项目**

```bash
cd hm-service
mvn spring-boot:run
```

**步骤3：访问API文档**

打开浏览器访问：http://localhost:8080/doc.html

### 6.3 微服务模式运行

**步骤1：启动Nacos**

```bash
# 假设Nacos安装在 /nacos
cd /nacos/bin
# Linux/Mac
./startup.sh -m standalone
# Windows
startup.cmd -m standalone
```

**步骤2：配置Nacos**

确保各服务的 [bootstrap.yaml](file:///d:/JavaSelfLearn/hmall/hm-gateway/src/main/resources/bootstrap.yaml) 指向正确的Nacos地址：

```yaml
spring:
  cloud:
    nacos:
      server-addr: 192.168.88.131:8848
```

**步骤3：依次启动服务**

```bash
# 启动网关
cd hm-gateway
mvn spring-boot:run

# 启动用户服务
cd user-service
mvn spring-boot:run

# 启动商品服务
cd item-service
mvn spring-boot:run

# 启动购物车服务
cd cart-service
mvn spring-boot:run

# 启动交易服务
cd trade-service
mvn spring-boot:run

# 启动支付服务
cd pay-service
mvn spring-boot:run
```

### 6.4 打包部署

```bash
# 打包所有模块
cd hmall
mvn clean package -DskipTests

# 运行单体服务
java -jar hm-service/target/hm-service.jar

# 运行微服务（以网关为例）
java -jar hm-gateway/target/hm-gateway.jar
```

## 七、安全机制

### 7.1 JWT认证

项目使用 JWT（JSON Web Token）进行身份认证，密钥存储在 `hmall.jks` 文件中。

**认证流程**：

```
1. 用户登录 → 服务端验证用户名密码 → 生成JWT Token → 返回给客户端
2. 客户端后续请求携带Token → 网关/拦截器验证Token → 提取用户ID → 放入UserContext
3. Service层从UserContext获取当前用户
```

**JWT工具类**：[JwtTool.java](file:///d:/JavaSelfLearn/hmall/hm-service/src/main/java/com/hmall/utils/JwtTool.java)

### 7.2 密码加密

使用 Spring Security 的 BCryptPasswordEncoder 进行密码加密存储。

```java
// 加密
passwordEncoder.encode(password)

// 验证
passwordEncoder.matches(rawPassword, encodedPassword)
```

### 7.3 接口权限控制

通过 `AuthProperties` 配置免认证路径，其他接口需要携带有效Token。

```java
// 免认证路径示例
/auth/login
/items/**
/search/**
```

## 八、关键配置

### 8.1 MyBatis Plus配置

```yaml
mybatis-plus:
  configuration:
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
  global-config:
    db-config:
      update-strategy: not_null  # 更新时只更新非空字段
      id-type: auto              # 主键自增
```

### 8.2 Knife4j配置

```yaml
knife4j:
  enable: true
  openapi:
    title: 黑马商城接口文档
    version: v1.0.0
    group:
      default:
        api-rule: package
        api-rule-resources:
          - com.hmall.controller
```

## 九、扩展说明

### 9.1 AI服务模块

[ai-service](file:///d:/JavaSelfLearn/hmall/ai-service) 包含AI咨询功能，提供PDF文件处理能力，可用于智能客服、专业推荐等场景。

### 9.2 前端模块

[hmall-nginx/html](file:///d:/JavaSelfLearn/hmall/hmall-nginx/html) 包含三个前端应用：

- **hmall-portal**：用户门户（首页、登录、购物车、订单、支付）
- **hmall-admin**：后台管理（商品管理、用户管理）
- **hm-refresh-admin**：刷新版后台管理

### 9.3 Docker支持

[hm-service/Dockerfile](file:///d:/JavaSelfLearn/hmall/hm-service/Dockerfile) 提供了Docker镜像构建支持。

## 十、常见问题

### 10.1 JKS文件生成

```bash
keytool -genkeypair -alias hmall -keyalg RSA -keypass hmall123 -keystore hmall.jks -storepass hmall123
```

### 10.2 数据库连接失败

检查数据库配置是否正确，确保MySQL服务已启动，端口号正确（默认为3307，可修改为3306）。

### 10.3 微服务注册失败

确保Nacos服务已启动，检查各服务的 `bootstrap.yaml` 中Nacos地址配置是否正确。

### 10.4 Token过期

Token默认有效期为30分钟，过期后需重新登录获取新Token。