# 校园二手交易平台 — 完整实现大纲

> 对齐定稿颗粒度，覆盖后端全部文件、前端全部页面、数据库全部字段

---

## 一、项目总览

| 维度     | 内容                                               |
|--------|----------------------------------------------------|
| 项目名称   | 校园二手交易平台                                          |
| 技术栈    | Spring Boot 3.3 + Spring Security + JWT + Redis + WebSocket + Vue 3 + Element Plus |
| 架构模式   | 前后端分离（单体后端 + SPA前端）                              |
| JDK版本  | 17                                                 |
| 数据库    | MySQL 8.0（10张表）+ Redis（未读数/黑名单/缓存）                 |
| 端口     | 后端 8080 / 前端开发服务器 5173                            |
| 总文件数   | 93 个源码文件（不含 node_modules / target）                 |

---

## 二、数据库层（10张表，完全对应定稿）

### 2.1 表结构清单

| # | 表名              | 行数（定稿字段）| 关键约束 |
|---|-----------------|----------|------|
| 1 | users           | 11 字段    | username/phone/email 唯一索引 |
| 2 | categories      | 6 字段     | parent_id 自关联外键，支持两级 |
| 3 | goods           | 14 字段    | FULLTEXT(title,description)，status 枚举 |
| 4 | goods_images    | 5 字段     | ON DELETE CASCADE |
| 5 | orders          | 12 字段    | order_no 唯一，4 个时间戳字段 |
| 6 | reviews         | 7 字段     | CHECK score BETWEEN 1 AND 5 |
| 7 | chat_sessions   | 5 字段     | (buyer_id,seller_id,goods_id) 联合唯一 |
| 8 | messages        | 7 字段     | ON DELETE CASCADE |
| 9 | browse_history  | 4 字段     | 双外键 ON DELETE CASCADE |
| 10| reports         | 6 字段     | status 三态：PENDING/HANDLED/DISMISSED |

### 2.2 初始化数据
- 一级分类 6 个（数码电子/教材书籍/生活用品/运动户外/服装配饰/其他）
- 二级分类 11 个（手机/电脑/耳机音箱/相机 等）
- 管理员账号：admin / admin123456（BCrypt 强度 12）

---

## 三、后端层（Spring Boot 单体）

### 3.1 目录结构

```
src/main/java/com/campus/
├── CampusTradeApplication.java          主启动类
├── common/
│   ├── result/Result.java               统一响应体（code/message/data）
│   ├── exception/
│   │   ├── BusinessException.java       业务异常（带 HTTP code）
│   │   └── GlobalExceptionHandler.java  全局异常处理（参数校验/业务/兜底）
│   └── utils/JwtUtils.java              JWT 生成/解析/校验（Bean注入密钥）
├── config/
│   ├── SecurityConfig.java              Spring Security 无状态配置 + JwtAuthFilter
│   ├── WebSocketConfig.java             STOMP端点 + StompAuthInterceptor + ChatWsController
│   └── AppConfig.java                   Redis序列化 + CORS + 静态文件映射
├── entity/（10个）
├── repository/（10个）
├── service/impl/（8个）
├── controller/（7个）
└── dto/
    ├── req/（12个）
    └── resp/（11个）
```

### 3.2 实体层（entity/ — 10个文件）

| 文件                | 对应表              | 要点 |
|---------------------|-----------------|-----|
| User.java           | users           | BCrypt 密码存 password_hash，@CreationTimestamp/@UpdateTimestamp |
| Goods.java          | goods           | status 默认 DRAFT，BigDecimal 精度 10,2 |
| GoodsImage.java     | goods_images    | sortOrder 控制封面（0=封面） |
| Category.java       | categories      | parentId 自关联，不持 children |
| Order.java          | orders          | 4 个时间戳字段（创建/确认/完成/取消） |
| Review.java         | reviews         | reviewer_id + reviewee_id 双向外键 |
| ChatSession.java    | chat_sessions   | @UniqueConstraint 三字段联合唯一 |
| Message.java        | messages        | contentType: TEXT/IMAGE |
| BrowseHistory.java  | browse_history  | 轻量记录，去重逻辑在 Service 层 |
| Report.java         | reports         | status 三态机 |

### 3.3 Repository 层（10个接口，关键自定义查询）

| Repository              | 核心自定义方法 |
|-------------------------|-----------|
| GoodsRepository         | `search(keyword,categoryId,minPrice,maxPrice,pageable)` —— JPQL 多条件查询；`incrementViews` —— @Modifying 原子更新 |
| OrderRepository         | `existsByGoodsIdAndBuyerIdAndStatusIn` —— 防重复下单；`countFinished` / `sumFinishedAmount` —— 后台统计 |
| ReviewRepository        | `avgScoreByRevieweeId` —— 卖家评分计算；`existsByOrderIdAndReviewerId` —— 防重复评价 |
| MessageRepository       | `countUnread` / `markAllRead` —— @Modifying 批量标记已读 |
| BrowseHistoryRepository | `deleteByUserIdAndGoodsId` —— 去重前先删旧记录 |
| ReportRepository        | `existsByReporterIdAndGoodsId` —— 防重复举报 |

### 3.4 Service 层（8个服务类）

#### AuthService — 认证服务
| 方法 | 逻辑要点 |
|-----|--------|
| register | 用户名/手机/邮箱唯一性校验 → BCrypt 加密密码 → 保存 |
| login | 支持用户名或手机号 → matchesPassword → 生成 AccessToken + RefreshToken → RefreshToken 存 Redis（7天） |
| refresh | 验证 RefreshToken → 比对 Redis → 滚动刷新（旧 Token 作废，生成新对） |
| logout | AccessToken 写入 Redis 黑名单（剩余有效期 TTL） → 删除 RefreshToken |
| changePassword | 校验旧密码 → BCrypt 新密码 → 强制登出（清 Redis） |

#### GoodsService — 商品服务
| 方法 | 逻辑要点 |
|-----|--------|
| publish | 校验分类存在 → 保存 Goods（DRAFT/ON_SALE） → 批量保存 GoodsImage |
| update | 校验归属 → 更新字段 → 删除旧图片 → 重新保存新图片 |
| putOnSale / takeDown | 校验归属 → 状态变更（DRAFT↔ON_SALE） |
| delete | 校验归属且非进行中订单 → 级联删除图片（CascadeType） |
| getDetail | 查商品 → 异步增加浏览数 → 写浏览历史（@Async） → 组装卖家信息/分类名/图片列表 |
| search | 多条件 JPQL → 支持 latest/price_asc/price_desc/views 排序 |
| browseHistory | 查 browse_history → join goods 获取详情 |
| adminSearch | 不过滤 status，全量查询供管理员使用 |
| adminTakeDown | 强制将 status 设为 DRAFT |

#### OrderService — 订单服务
| 方法 | 逻辑要点 |
|-----|--------|
| create | 校验商品在售 → 校验非自购 → 生成唯一 orderNo（时间戳+随机4位） → 商品状态改 RESERVED |
| confirm | 校验卖家归属 → PENDING→CONFIRMED → 记录 paidAt |
| finish | 校验买家归属 → CONFIRMED→FINISHED → 记录 finishedAt → 商品状态改 SOLD |
| cancel | 校验归属（买家或PENDING状态） → 状态改 CANCELED → 商品恢复 ON_SALE → 记录 cancelAt |
| submitReview | 校验 FINISHED 状态 → 校验参与方 → 防重复评价 → 互评（reviewee = 对方） |

#### ChatService — 聊天服务
| 方法 | 逻辑要点 |
|-----|--------|
| getOrCreate | 幂等创建：先查 (buyer+seller+goods) 唯一索引，存在直接返回 |
| saveMessage | 校验会话归属 → 持久化 → Redis incr 接收方未读数（TTL 30天） |
| history | 分页查询 → markAllRead → Redis 清零未读 |
| unreadSummary | 遍历所有会话 → 从 Redis 取各会话未读数 |
| getReceiverId | session.buyerId == senderId ? sellerId : buyerId |

#### ReportService — 举报服务
| 方法 | 逻辑要点 |
|-----|--------|
| submit | 校验商品存在 → 防重复举报（同用户同商品唯一） → 保存 |
| adminList | status 过滤分页，并 join 用户名/商品标题 |
| handle | PENDING→HANDLED（顺带强制下架）或 DISMISSED |

#### 其他服务
- **CategoryService**：`tree()` 先查顶级分类再查子分类，组装两级树结构
- **AdminService**：`stat()` 聚合用户数/商品数/订单数/成交额/待处理举报数
- **FileService**：验证 MIME 类型（JPEG/PNG/WEBP）→ 按日期分目录存储 → 返回可访问 URL

### 3.5 Controller 层（7个，共38个接口）

| Controller          | 前缀             | 接口数 | 认证要求 |
|--------------------|----------------|------|------|
| AuthController      | /api/auth       | 7    | 部分公开 |
| GoodsController     | /api/goods      | 10   | 部分公开 |
| OrderController     | /api/orders     | 8    | 全需认证 |
| ChatController      | /api/chat       | 4    | 全需认证 |
| CategoryController  | /api/categories | 1    | 公开   |
| FileController      | /api/files      | 1    | 需认证  |
| AdminController     | /api/admin      | 9    | 仅ADMIN |

### 3.6 安全架构

```
HTTP Request
    │
    ▼
JwtAuthFilter（OncePerRequestFilter）
    ├── 提取 Bearer Token
    ├── validateToken（签名+过期）
    ├── 检查 Redis 黑名单
    ├── 解析 userId + role
    └── 写入 SecurityContext（UsernamePasswordAuthenticationToken）
    │
    ▼
SecurityFilterChain
    ├── 白名单路径 → permitAll
    ├── /api/admin/** → hasRole("ADMIN")
    └── 其余 → authenticated
    │
    ▼
Controller → Service → Repository
```

### 3.7 WebSocket 架构

```
客户端建立 SockJS 连接 ws://host:8080/ws
    │
    ▼
StompAuthInterceptor（CONNECT 帧）
    └── 解析 Authorization Header → 设置 Principal（userId）
    │
    ▼
订阅：
    /topic/chat/{sessionId}       ← 会话广播（双方均订阅）
    /user/{userId}/queue/notify   ← 个人未读角标推送
    │
客户端发送：STOMP SEND /app/chat.send
    │
    ▼
ChatWsController.handleMessage()
    ├── 1. 解析 senderId（from Principal）
    ├── 2. chatService.saveMessage() → 持久化 + Redis 未读+1
    ├── 3. messagingTemplate → /topic/chat/{sessionId} 广播
    └── 4. messagingTemplate → /user/{receiverId}/queue/notify 推送角标
```

---

## 四、前端层（Vue 3 SPA）

### 4.1 目录结构

```
frontend/src/
├── main.js                    注册 Vue/Pinia/Router/ElementPlus
├── App.vue                    全局导航栏（搜索/用户下拉/消息角标）
├── router/index.js            路由表 + 导航守卫（requiresAuth/requiresAdmin）
├── stores/
│   ├── user.js                登录态/Token/用户信息（Pinia）
│   └── chat.js                全局未读数（Pinia）
├── utils/
│   ├── request.js             Axios实例 + Token注入 + 401自动刷新
│   └── websocket.js           STOMP客户端封装（connect/subscribe/send）
└── views/
    ├── AuthView.vue
    ├── HomeView.vue
    ├── GoodsDetailView.vue
    ├── PublishView.vue
    ├── MyGoodsView.vue
    ├── OrderView.vue
    ├── ChatView.vue
    ├── ProfileView.vue
    ├── HistoryView.vue
    └── admin/（5个）
```

### 4.2 页面详细说明

#### AuthView.vue — 登录/注册
| 区域 | 实现 |
|-----|-----|
| Tab 切换 | el-tabs 切换登录/注册，模式切换时重置表单 |
| 登录表单 | 用户名+密码，支持回车提交；支持用户名或手机号 |
| 注册表单 | 用户名+密码+确认密码+手机号（选填）；全字段校验 |
| 校验规则 | 用户名 3~20 位字母数字下划线；密码 6 位+；密码确认对比；手机号正则 |
| 登录成功 | 存 Token → 获取用户信息 → 跳转到 redirect 或首页 |

#### HomeView.vue — 首页
| 区域 | 实现 |
|-----|-----|
| 分类导航 | 横向滚动标签；点击过滤，再次点击取消 |
| 筛选工具栏 | 关键词输入 + 价格区间（最低价/最高价）+ 排序选择（最新/价格升降/浏览量） |
| 商品网格 | 响应式 grid（`auto-fill, minmax(200px,1fr)`）；卡片含封面图/标题/价格/状态标签/卖家信息 |
| 图片处理 | `coverUrl`（第一张图）展示，默认图兜底 |
| 分页 | el-pagination；监听 route.query.keyword 实现从导航栏搜索联动 |

#### GoodsDetailView.vue — 商品详情
| 区域 | 实现 |
|-----|-----|
| 图片轮播 | el-carousel，disabled autoplay，outside indicator |
| 商品信息 | 标题/价格（含划线原价）/状态标签/描述/位置/浏览数 |
| 卖家区 | 头像+昵称+平均评分（el-rate disabled） |
| 操作按钮（非卖家）| 立即购买（ON_SALE 才可用）/ 联系卖家 / 举报 |
| 操作按钮（卖家）| 编辑 / 上架或下架 / 删除（二次确认） |
| 下单弹窗 | 确认商品信息 + 填写备注 → POST /api/orders |
| 举报弹窗 | 填写原因（限200字）→ POST /api/goods/report |

#### PublishView.vue — 发布/编辑商品
| 区域 | 实现 |
|-----|-----|
| 图片上传 | el-upload 调 /api/files/image；最多 9 张；预览+删除；第一张自动设封面 |
| 表单字段 | 标题（必填）/ 分类（el-cascader两级）/ 售价 / 原价（选填）/ 地点（选填）/ 描述（必填） |
| 编辑模式 | 路由带 /:id 时从接口加载已有数据回填 |
| 提交 | 保存草稿（DRAFT）或立即发布（ON_SALE）两个按钮 |

#### MyGoodsView.vue — 我的商品
| 区域 | 实现 |
|-----|-----|
| Tab 筛选 | 全部 / 在售 / 草稿 / 已售 |
| 卡片操作 | 编辑（跳转）/ 上架 / 下架 / 删除（二次确认） |
| 分页 | 12条/页 |

#### OrderView.vue — 订单管理
| 区域 | 实现 |
|-----|-----|
| Tab 切换 | 我的购买 / 我的出售 |
| 订单卡片 | 订单号/状态标签/商品缩略图/商品标题/价格/对方名称/创建时间 |
| 操作按钮 | 卖家：确认订单（PENDING）；买家：确认收货（CONFIRMED）/ 取消订单（PENDING）/ 去评价（FINISHED且未评） |
| 联系对方 | 创建/获取会话 → 跳转聊天页 |
| 评价弹窗 | el-rate 打分（1-5）+ 文字评价（限500字） |

#### ChatView.vue — 即时聊天
| 区域 | 实现 |
|-----|-----|
| 左侧会话列表 | 头像/名称/最后消息/未读角标；点击切换会话 |
| 右侧消息区 | 自己消息靠右（绿色气泡）/ 对方靠左；支持 TEXT 和 IMAGE 类型 |
| 历史消息 | 分页加载，"加载更多"按钮；首次加载后滚动到底部 |
| 发送消息 | 输入框回车发送 + 发送按钮；图片通过 el-upload 上传后发送 |
| 实时推送 | STOMP 订阅 `/topic/chat/{sessionId}`；接收方订阅 `/user/queue/notify` 更新角标 |
| WebSocket 生命周期 | onMounted 连接，选中会话时订阅，离开时取消订阅，onUnmounted 不断连 |

#### ProfileView.vue — 个人资料
| 区域 | 实现 |
|-----|-----|
| 头像上传 | el-upload 点击头像触发，上传后更新预览 |
| 基本信息 | 用户名（只读）/ 昵称 / 手机号 / 邮箱 |
| 修改密码 | 旧密码 + 新密码 + 确认密码；前端一致性校验 |
| 我的评价 | 列出所有被评价记录，含评分和文字评价 |

#### HistoryView.vue — 浏览历史
- 商品网格（同首页样式），分页展示
- 直接调 /api/goods/history 接口

#### Admin — 管理后台（5个页面）

| 页面               | 功能 |
|------------------|-----|
| DashBoard.vue    | 6 个统计卡片：注册用户/商品总数/订单总数/成交金额/已完成订单/待处理举报 |
| UserMgmt.vue     | 分页表格；禁用/启用按钮；el-prompt 重置密码 |
| GoodsMgmt.vue    | 分页表格含封面缩略图；强制下架（二次确认） |
| ReportMgmt.vue   | Radio 过滤状态；处理（联动下架商品）/ 驳回 |
| CategoryMgmt.vue | 树形表格展示两级分类；新增（弹窗）/ 删除（有子分类时阻止） |

### 4.3 状态管理（Pinia）

#### user store
| 状态/方法 | 说明 |
|--------|-----|
| token / refreshToken | localStorage 持久化 |
| userInfo | 含 id/username/role/nickname/avatarUrl 等 |
| isLogin / isAdmin | computed |
| initFromStorage() | 页面刷新时从 localStorage 恢复 |
| setTokens() / setUserInfo() | 登录后调用 |
| fetchMe() | 从 /api/auth/me 刷新用户信息 |
| logout() | 清空内存 + localStorage |

#### chat store
| 状态/方法 | 说明 |
|--------|-----|
| unreadMap | `{ sessionId: count }` |
| totalUnread | computed，所有会话未读数之和（用于导航栏角标） |
| fetchUnread() | 从 /api/chat/unread 拉取 |
| setUnread() / clearUnread() | WebSocket 推送时调用 |

### 4.4 工具层

#### request.js（Axios 封装）
1. 请求拦截：自动注入 `Authorization: Bearer {token}`
2. 响应拦截：业务 code ≠ 200 时 ElMessage.error
3. **401 自动刷新**：isRefreshing 标志防并发；pendingQueue 队列重放失败请求
4. 刷新失败：清空存储 → 跳转 /auth

#### websocket.js（STOMP 封装）
1. `connect(token, onConnect)`：创建 Client，connectHeaders 含 JWT
2. 支持断线重连（reconnectDelay: 5000ms）
3. subscribers Map：CONNECT 后自动恢复订阅
4. `subscribe(dest, cb)`：支持连接前调用（入队），连接后立即生效
5. `sendMessage(payload)`：返回 boolean 表示是否发送成功

---

## 五、完整文件清单（93个文件）

### 后端（54个 .java 文件）

```
├── CampusTradeApplication.java
├── common/
│   ├── result/Result.java
│   ├── exception/BusinessException.java
│   ├── exception/GlobalExceptionHandler.java
│   └── utils/JwtUtils.java
├── config/
│   ├── SecurityConfig.java（含 JwtAuthFilter 内部类）
│   ├── WebSocketConfig.java（含 StompAuthInterceptor + ChatWsController）
│   └── AppConfig.java（含 RedisConfig + WebMvcConfig）
├── entity/（10个）
│   User / Goods / GoodsImage / Category / Order /
│   Review / ChatSession / Message / BrowseHistory / Report
├── repository/（10个）
│   UserRepository / GoodsRepository / GoodsImageRepository /
│   CategoryRepository / OrderRepository / ReviewRepository /
│   ChatSessionRepository / MessageRepository /
│   BrowseHistoryRepository / ReportRepository
├── service/impl/（8个）
│   AuthService / GoodsService / OrderService / ChatService /
│   ReportService / CategoryService / AdminService / FileService
├── controller/（7个）
│   AuthController / GoodsController / OrderController /
│   ChatController / CategoryController / FileController / AdminController
└── dto/
    ├── req/（12个）
    │   RegisterReq / LoginReq / RefreshTokenReq / UpdateUserReq /
    │   ChangePasswordReq / GoodsReq / GoodsSearchReq / OrderReq /
    │   ReviewReq / ChatSessionReq / SendMsgPayload / ReportReq
    └── resp/（11个）
        UserResp / TokenResp / GoodsResp / OrderResp / ReviewResp /
        ChatSessionResp / MessageResp / CategoryResp /
        AdminStatResp / ReportResp / PageResp
```

### 前端（25个文件）

```
├── main.js
├── App.vue
├── router/index.js
├── stores/user.js
├── stores/chat.js
├── utils/request.js
├── utils/websocket.js
└── views/（18个 .vue）
    ├── AuthView.vue
    ├── HomeView.vue
    ├── GoodsDetailView.vue
    ├── PublishView.vue
    ├── MyGoodsView.vue
    ├── OrderView.vue
    ├── ChatView.vue
    ├── ProfileView.vue
    ├── HistoryView.vue
    └── admin/
        ├── AdminLayout.vue
        ├── DashBoard.vue
        ├── UserMgmt.vue
        ├── GoodsMgmt.vue
        ├── ReportMgmt.vue
        └── CategoryMgmt.vue
```

### 配置/部署（4个文件）

```
├── pom.xml
├── application.yml
├── deploy/schema.sql
└── README.md
```

---

## 六、接口与页面对照表

| 前端页面               | 调用的后端接口 |
|--------------------|-----------|
| AuthView 登录        | POST /api/auth/login |
| AuthView 注册        | POST /api/auth/register |
| HomeView            | GET /api/categories，GET /api/goods |
| GoodsDetailView     | GET /api/goods/{id}，GET /api/orders/reviews/{uid} |
| GoodsDetailView 下单 | POST /api/orders |
| GoodsDetailView 聊天 | POST /api/chat/sessions |
| GoodsDetailView 举报 | POST /api/goods/report |
| PublishView         | GET /api/categories，POST/PUT /api/goods，POST /api/files/image |
| MyGoodsView         | GET /api/goods/my，PUT /api/goods/{id}/on-sale 等 |
| OrderView           | GET /api/orders/purchases|sales，PUT /api/orders/{id}/confirm|finish|cancel，POST /api/orders/review |
| ChatView            | GET /api/chat/sessions，GET /api/chat/sessions/{id}/messages，WS |
| ProfileView         | GET/PUT /api/auth/me，PUT /api/auth/password，GET /api/orders/reviews/{uid} |
| HistoryView         | GET /api/goods/history |
| Admin/DashBoard     | GET /api/admin/stat |
| Admin/UserMgmt      | GET /api/admin/users，PUT /api/admin/users/{id}/status|reset-password |
| Admin/GoodsMgmt     | GET /api/admin/goods，PUT /api/admin/goods/{id}/take-down |
| Admin/ReportMgmt    | GET /api/admin/reports，PUT /api/admin/reports/{id}/handle |
| Admin/CategoryMgmt  | GET /api/categories，POST /api/admin/categories，DELETE /api/admin/categories/{id} |

---

## 七、安全机制总结

| 机制 | 实现方式 |
|-----|--------|
| 密码存储 | BCrypt 强度 12，`password_hash` 字段 |
| 身份认证 | JWT AccessToken（30分钟）+ RefreshToken（7天，Redis 存储） |
| 登出安全 | AccessToken 写入 Redis 黑名单，提前失效 |
| 接口鉴权 | Spring Security filterChain；`/api/admin/**` 仅 ADMIN |
| WebSocket 认证 | STOMP CONNECT 帧解析 JWT，Principal 绑定 userId |
| 参数校验 | Bean Validation（@Valid）+ GlobalExceptionHandler 统一返回 400 |
| 前端 Token 刷新 | Axios 401 拦截 → 自动调 /api/auth/refresh → 重放请求 |
| CORS | WebMvcConfigurer.addCorsMappings（开发阶段 allowAll，生产改具体域名） |

---

## 八、启动步骤（5分钟快速运行）

```bash
# 1. 导入数据库
mysql -u root -p < deploy/schema.sql

# 2. 修改 application.yml（数据库密码、Redis密码、JWT密钥）

# 3. 启动后端
mvn spring-boot:run
# → http://localhost:8080

# 4. 启动前端（新终端）
cd frontend && npm install && npm run dev
# → http://localhost:5173

# 5. 访问
# 普通用户：注册/登录后即可使用全部功能
# 管理后台：用 admin/admin123456 登录后访问 /admin
```
