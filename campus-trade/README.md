# 校园二手交易平台

基于 **Spring Boot 3.3 + Vue 3 + MySQL 8.0** 的前后端分离校园二手物品交易系统。

---

## 技术栈

| 层       | 技术                                          |
|---------|---------------------------------------------|
| 后端框架    | Spring Boot 3.3 / Spring Security / Spring Data JPA |
| 认证      | JWT（无状态）+ Redis 黑名单                         |
| 实时通信    | Spring WebSocket + STOMP                    |
| 数据库     | MySQL 8.0（主存储）+ Redis（缓存/会话/未读数）            |
| 前端框架    | Vue 3 + Vite + Element Plus + Pinia          |
| 文件存储    | 本地磁盘（可替换为阿里云 OSS）                           |
| JDK     | 17                                           |

---

## 快速启动

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- Redis 6+

### 2. 数据库初始化

```bash
mysql -u root -p < deploy/schema.sql
```

初始管理员账号：`admin` / `admin123456`

### 3. 修改后端配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
  data:
    redis:
      password: 你的Redis密码（无密码留空）

jwt:
  secret: "替换为32位以上随机字符串"

file:
  upload-dir: ./uploads   # 图片存储路径
```

### 4. 启动后端

```bash
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

---

## 项目结构

```
campus-trade/
├── pom.xml
├── deploy/
│   └── schema.sql                    ← 数据库建表脚本（10张表）
├── src/main/
│   ├── resources/
│   │   └── application.yml
│   └── java/com/campus/
│       ├── CampusTradeApplication.java
│       ├── common/
│       │   ├── result/Result.java            ← 统一响应体
│       │   ├── exception/                    ← 业务异常 + 全局处理器
│       │   └── utils/JwtUtils.java           ← JWT 工具
│       ├── config/
│       │   ├── SecurityConfig.java           ← Spring Security + JWT过滤器
│       │   ├── WebSocketConfig.java          ← STOMP + 认证拦截器 + 消息处理
│       │   └── AppConfig.java                ← Redis配置 + CORS + 静态资源
│       ├── entity/                           ← 10个JPA实体
│       ├── repository/                       ← 10个Spring Data接口
│       ├── service/impl/                     ← 8个服务类
│       │   ├── AuthService.java              ← 注册/登录/刷新/登出/改密
│       │   ├── GoodsService.java             ← 商品CRUD/搜索/浏览历史
│       │   ├── OrderService.java             ← 订单流转/评价
│       │   ├── ChatService.java              ← 会话/消息/未读数
│       │   ├── ReportService.java            ← 举报提交/管理员处理
│       │   ├── CategoryService.java          ← 分类树/增删
│       │   ├── AdminService.java             ← 统计/用户管理
│       │   └── FileService.java              ← 图片上传
│       ├── controller/                       ← 7个Controller
│       │   ├── AuthController.java
│       │   ├── GoodsController.java
│       │   ├── OrderController.java
│       │   ├── ChatController.java
│       │   ├── CategoryController.java
│       │   ├── FileController.java
│       │   └── AdminController.java
│       └── dto/
│           ├── req/                          ← 12个请求DTO
│           └── resp/                         ← 11个响应DTO
└── frontend/
    ├── package.json
    ├── vite.config.js                        ← 含API代理配置
    └── src/
        ├── main.js
        ├── App.vue                           ← 全局导航栏
        ├── router/index.js                   ← 路由 + 权限守卫
        ├── stores/
        │   ├── user.js                       ← 用户状态(Pinia)
        │   └── chat.js                       ← 未读数状态(Pinia)
        ├── utils/
        │   ├── request.js                    ← Axios + Token刷新
        │   └── websocket.js                  ← STOMP客户端封装
        └── views/
            ├── AuthView.vue                  ← 登录/注册
            ├── HomeView.vue                  ← 首页商品列表+搜索
            ├── GoodsDetailView.vue           ← 商品详情+下单+举报
            ├── PublishView.vue               ← 发布/编辑商品
            ├── MyGoodsView.vue               ← 我的商品管理
            ├── OrderView.vue                 ← 订单管理+评价
            ├── ChatView.vue                  ← 实时聊天
            ├── ProfileView.vue               ← 个人资料+修改密码
            ├── HistoryView.vue               ← 浏览历史
            └── admin/
                ├── AdminLayout.vue           ← 管理后台布局
                ├── DashBoard.vue             ← 数据统计
                ├── UserMgmt.vue              ← 用户管理（禁用/重置密码）
                ├── GoodsMgmt.vue             ← 商品管理（强制下架）
                ├── ReportMgmt.vue            ← 举报管理（处理/驳回）
                └── CategoryMgmt.vue          ← 分类管理（增删）
```

---

## API 接口总览

### 认证接口 `/api/auth`
| 方法   | 路径              | 说明           | 认证 |
|------|-----------------|--------------|-----|
| POST | /register       | 注册           | ×   |
| POST | /login          | 登录           | ×   |
| POST | /refresh        | 刷新Token      | ×   |
| POST | /logout         | 登出           | ✓   |
| GET  | /me             | 获取当前用户信息     | ✓   |
| PUT  | /me             | 更新个人资料       | ✓   |
| PUT  | /password       | 修改密码         | ✓   |

### 商品接口 `/api/goods`
| 方法     | 路径                  | 说明        | 认证 |
|--------|-----------------------|-----------|-----|
| GET    | /                     | 搜索/列表     | ×   |
| GET    | /{id}                 | 商品详情      | ×   |
| POST   | /                     | 发布商品      | ✓   |
| PUT    | /{id}                 | 编辑商品      | ✓   |
| DELETE | /{id}                 | 删除商品      | ✓   |
| PUT    | /{id}/on-sale         | 上架        | ✓   |
| PUT    | /{id}/take-down       | 下架        | ✓   |
| GET    | /my                   | 我发布的商品    | ✓   |
| GET    | /history              | 浏览历史      | ✓   |
| POST   | /report               | 举报商品      | ✓   |

### 订单接口 `/api/orders`
| 方法   | 路径                   | 说明       | 认证 |
|------|----------------------|----------|-----|
| POST | /                    | 创建订单     | ✓   |
| GET  | /purchases           | 我的购买     | ✓   |
| GET  | /sales               | 我的出售     | ✓   |
| PUT  | /{id}/confirm        | 卖家确认     | ✓   |
| PUT  | /{id}/finish         | 买家确认收货   | ✓   |
| PUT  | /{id}/cancel         | 取消订单     | ✓   |
| POST | /review              | 提交评价     | ✓   |
| GET  | /reviews/{userId}    | 查看用户评价   | ×   |

### 聊天接口 `/api/chat`
| 方法   | 路径                          | 说明      | 认证 |
|------|------------------------------|---------|-----|
| POST | /sessions                    | 获取或创建会话 | ✓   |
| GET  | /sessions                    | 会话列表    | ✓   |
| GET  | /sessions/{id}/messages      | 历史消息    | ✓   |
| GET  | /unread                      | 未读数汇总   | ✓   |

**WebSocket：** `ws://host:8080/ws`
- 发消息：`/app/chat.send`
- 订阅会话：`/topic/chat/{sessionId}`
- 订阅未读：`/user/{userId}/queue/notify`

### 其他接口
| 方法   | 路径                         | 说明      | 认证        |
|------|------------------------------|---------|-----------|
| GET  | /api/categories              | 分类树     | ×         |
| POST | /api/files/image             | 上传图片    | ✓         |
| GET  | /api/admin/stat              | 统计数据    | ✓ ADMIN   |
| GET  | /api/admin/users             | 用户列表    | ✓ ADMIN   |
| PUT  | /api/admin/users/{id}/status | 禁用/启用   | ✓ ADMIN   |
| GET  | /api/admin/goods             | 商品列表    | ✓ ADMIN   |
| PUT  | /api/admin/goods/{id}/take-down | 强制下架 | ✓ ADMIN   |
| GET  | /api/admin/reports           | 举报列表    | ✓ ADMIN   |
| PUT  | /api/admin/reports/{id}/handle  | 处理举报 | ✓ ADMIN   |

---

## 数据库设计（10张表）

| 表名              | 说明     | 核心字段                                    |
|-----------------|--------|-----------------------------------------|
| users           | 用户表    | username, password_hash, role, status   |
| categories      | 分类表    | name, parent_id（两级自关联）                  |
| goods           | 商品表    | seller_id, status(DRAFT/ON_SALE/RESERVED/SOLD) |
| goods_images    | 商品图片表  | goods_id, image_url, sort_order         |
| orders          | 订单表    | buyer_id, seller_id, status(PENDING/CONFIRMED/FINISHED/CANCELED) |
| reviews         | 评价表    | reviewer_id, reviewee_id, score(1-5)    |
| chat_sessions   | 聊天会话表  | buyer_id+seller_id+goods_id（联合唯一）       |
| messages        | 消息表    | chat_id, sender_id, content_type(TEXT/IMAGE) |
| browse_history  | 浏览记录表  | user_id, goods_id（用于浏览历史）               |
| reports         | 举报表    | reporter_id, goods_id, reason, status(PENDING/HANDLED/DISMISSED) |
