-- ============================================================
-- 校园二手交易平台 — 数据库建表脚本（定稿版，共10张表）
-- MySQL 8.0 | utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_trade
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_trade;

-- ============================================================
-- 1. 用户表 users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键，用户唯一标识，自增',
    username      VARCHAR(50) NOT NULL                COMMENT '用户名，唯一索引',
    password_hash VARCHAR(128)NOT NULL                COMMENT '密码加密哈希值（BCrypt）',
    nickname      VARCHAR(50)                         COMMENT '用户昵称',
    avatar_url    VARCHAR(300)                        COMMENT '用户头像资源路径',
    phone         VARCHAR(20)                         COMMENT '绑定手机号',
    email         VARCHAR(100)                        COMMENT '绑定电子邮箱',
    role          VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色（USER / ADMIN）',
    status        TINYINT(1)  NOT NULL DEFAULT 1      COMMENT '账户状态（1正常 / 0禁用）',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP             COMMENT '账户创建时间',
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP          COMMENT '账户最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone    (phone),
    UNIQUE KEY uk_email    (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 分类表 categories（支持两级自关联）
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键，分类唯一标识，自增',
    name       VARCHAR(50) NOT NULL                COMMENT '分类显示名称',
    parent_id  BIGINT                              COMMENT '父分类ID（NULL表示顶级分类，自关联）',
    icon       VARCHAR(100)                        COMMENT '分类对应的图标资源标识或路径',
    sort       INT(11)     NOT NULL DEFAULT 0      COMMENT '界面显示排序权重，默认0',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分类创建时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================================
-- 3. 商品表 goods
-- ============================================================
CREATE TABLE IF NOT EXISTS goods (
    id             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键，商品唯一标识，自增',
    seller_id      BIGINT          NOT NULL               COMMENT '卖家ID（外键→users）',
    title          VARCHAR(100)    NOT NULL               COMMENT '商品标题',
    description    TEXT                                   COMMENT '商品详细描述',
    price          DECIMAL(10,2)   NOT NULL               COMMENT '挂牌售价（最大99999999.99）',
    original_price DECIMAL(10,2)                          COMMENT '购入原价',
    category_id    BIGINT          NOT NULL               COMMENT '分类ID（外键→categories）',
    status         VARCHAR(20)     NOT NULL DEFAULT 'DRAFT'
                                                          COMMENT '状态（DRAFT/ON_SALE/RESERVED/SOLD）',
    views          INT(11)         NOT NULL DEFAULT 0     COMMENT '累计浏览次数，默认0',
    likes          INT(11)         NOT NULL DEFAULT 0     COMMENT '累计点赞总数，默认0',
    location       VARCHAR(100)                           COMMENT '校内交接地点',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP          COMMENT '发布时间',
    updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP        COMMENT '信息更新时间',
    PRIMARY KEY (id),
    KEY idx_seller_id   (seller_id),
    KEY idx_category_id (category_id),
    KEY idx_status      (status),
    KEY idx_created_at  (created_at),
    FULLTEXT KEY ft_title_desc (title, description),
    CONSTRAINT fk_goods_seller   FOREIGN KEY (seller_id)   REFERENCES users(id),
    CONSTRAINT fk_goods_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================================
-- 4. 商品图片表 goods_images
-- ============================================================
CREATE TABLE IF NOT EXISTS goods_images (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，图片唯一标识，自增',
    goods_id   BIGINT       NOT NULL               COMMENT '关联商品ID（外键→goods）',
    image_url  VARCHAR(300) NOT NULL               COMMENT '图片在服务器或云端的完整URL',
    sort_order TINYINT(3)   NOT NULL DEFAULT 0     COMMENT '展示排序，0为默认封面',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '图片上传时间',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id),
    CONSTRAINT fk_img_goods FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- ============================================================
-- 5. 订单表 orders
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键，订单唯一标识，自增',
    order_no    VARCHAR(32)   NOT NULL               COMMENT '业务唯一订单号（时间戳+随机串）',
    buyer_id    BIGINT        NOT NULL               COMMENT '买家ID（外键→users）',
    seller_id   BIGINT        NOT NULL               COMMENT '卖家ID（外键→users）',
    goods_id    BIGINT        NOT NULL               COMMENT '商品ID（外键→goods）',
    price       DECIMAL(10,2) NOT NULL               COMMENT '实际成交价格',
    status      VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                                                     COMMENT '订单状态（PENDING/CONFIRMED/FINISHED/CANCELED）',
    remark      VARCHAR(500)                         COMMENT '买家交易备注',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    paid_at     DATETIME                             COMMENT '卖家确认交易时间',
    finished_at DATETIME                             COMMENT '交易最终完成时间',
    cancel_at   DATETIME                             COMMENT '订单取消时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_buyer_id  (buyer_id),
    KEY idx_seller_id (seller_id),
    KEY idx_goods_id  (goods_id),
    KEY idx_status    (status),
    CONSTRAINT fk_ord_buyer  FOREIGN KEY (buyer_id)  REFERENCES users(id),
    CONSTRAINT fk_ord_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_ord_goods  FOREIGN KEY (goods_id)  REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 6. 评价表 reviews
-- ============================================================
CREATE TABLE IF NOT EXISTS reviews (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键，评价唯一标识，自增',
    order_id    BIGINT      NOT NULL               COMMENT '关联订单ID（外键→orders）',
    reviewer_id BIGINT      NOT NULL               COMMENT '发起评价的用户ID（外键→users）',
    reviewee_id BIGINT      NOT NULL               COMMENT '被评价的用户ID（外键→users）',
    score       TINYINT(1)  NOT NULL               COMMENT '评分分值（1～5星）',
    content     VARCHAR(500)                       COMMENT '文字评价反馈内容',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价提交时间',
    PRIMARY KEY (id),
    KEY idx_order_id    (order_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_reviewee_id (reviewee_id),
    CONSTRAINT fk_rev_order    FOREIGN KEY (order_id)    REFERENCES orders(id),
    CONSTRAINT fk_rev_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
    CONSTRAINT fk_rev_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(id),
    CONSTRAINT chk_score CHECK (score BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================================
-- 7. 聊天会话表 chat_sessions
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_sessions (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键，会话唯一标识，自增',
    buyer_id   BIGINT   NOT NULL               COMMENT '买家用户ID（外键→users）',
    seller_id  BIGINT   NOT NULL               COMMENT '卖家用户ID（外键→users）',
    goods_id   BIGINT   NOT NULL               COMMENT '咨询商品ID（外键→goods）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话发起时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session (buyer_id, seller_id, goods_id),
    KEY idx_cs_buyer  (buyer_id),
    KEY idx_cs_seller (seller_id),
    CONSTRAINT fk_cs_buyer  FOREIGN KEY (buyer_id)  REFERENCES users(id),
    CONSTRAINT fk_cs_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_cs_goods  FOREIGN KEY (goods_id)  REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ============================================================
-- 8. 消息表 messages
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id           BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键，消息唯一标识，自增',
    chat_id      BIGINT     NOT NULL               COMMENT '所属会话ID（外键→chat_sessions）',
    sender_id    BIGINT     NOT NULL               COMMENT '发送者ID（外键→users）',
    content      TEXT       NOT NULL               COMMENT '消息文本内容或图片URL地址',
    content_type VARCHAR(10)NOT NULL DEFAULT 'TEXT' COMMENT '消息类型（TEXT / IMAGE）',
    is_read      TINYINT(1) NOT NULL DEFAULT 0     COMMENT '阅读状态（0未读 / 1已读）',
    created_at   DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
    PRIMARY KEY (id),
    KEY idx_msg_chat_id   (chat_id),
    KEY idx_msg_sender_id (sender_id),
    KEY idx_msg_created   (created_at),
    CONSTRAINT fk_msg_chat   FOREIGN KEY (chat_id)   REFERENCES chat_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================================
-- 9. 浏览记录表 browse_history
-- ============================================================
CREATE TABLE IF NOT EXISTS browse_history (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    user_id    BIGINT   NOT NULL               COMMENT '浏览用户ID（外键→users）',
    goods_id   BIGINT   NOT NULL               COMMENT '被浏览商品ID（外键→goods）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (id),
    KEY idx_bh_user_id  (user_id),
    KEY idx_bh_goods_id (goods_id),
    KEY idx_bh_created  (created_at),
    CONSTRAINT fk_bh_user  FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bh_goods FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览记录表';

-- ============================================================
-- 10. 举报表 reports
-- ============================================================
CREATE TABLE IF NOT EXISTS reports (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    reporter_id BIGINT       NOT NULL               COMMENT '举报人ID（外键→users）',
    goods_id    BIGINT       NOT NULL               COMMENT '被举报商品ID（外键→goods）',
    reason      VARCHAR(200) NOT NULL               COMMENT '举报原因',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态（PENDING/HANDLED/DISMISSED）',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    PRIMARY KEY (id),
    KEY idx_rp_reporter (reporter_id),
    KEY idx_rp_goods    (goods_id),
    KEY idx_rp_status   (status),
    CONSTRAINT fk_rp_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_rp_goods    FOREIGN KEY (goods_id)    REFERENCES goods(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ============================================================
-- 初始化：商品分类
-- ============================================================
INSERT INTO categories (name, parent_id, icon, sort) VALUES
('数码电子', NULL, 'icon-digital', 1),
('教材书籍', NULL, 'icon-book',    2),
('生活用品', NULL, 'icon-home',    3),
('运动户外', NULL, 'icon-sport',   4),
('服装配饰', NULL, 'icon-clothes', 5),
('其他',     NULL, 'icon-other',   6);

INSERT INTO categories (name, parent_id, icon, sort) VALUES
('手机',     1, 'icon-phone',    1),
('电脑',     1, 'icon-laptop',   2),
('耳机音箱', 1, 'icon-earphone', 3),
('相机',     1, 'icon-camera',   4),
('教材',     2, 'icon-textbook', 1),
('考研资料', 2, 'icon-exam',     2),
('课外读物', 2, 'icon-reading',  3),
('寝室用品', 3, 'icon-dorm',     1),
('厨房用品', 3, 'icon-kitchen',  2),
('球类运动', 4, 'icon-ball',     1),
('健身器材', 4, 'icon-fitness',  2);

-- ============================================================
-- 初始化：管理员账号
-- 账号: admin  密码: admin123456
-- BCrypt(12轮) 哈希值，实际部署请重新生成
-- ============================================================
INSERT INTO users (username, password_hash, nickname, role, status) VALUES
('admin',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCVHGkGjxJ8NMGXzUKzuBuq',
 '系统管理员', 'ADMIN', 1);

-- ============================================================
-- 初始化：示例用户（密码均为 admin123456）
-- ============================================================
INSERT INTO users (username, password_hash, nickname, avatar_url, phone, email, role, status) VALUES
('zhangsan', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCVHGkGjxJ8NMGXzUKzuBuq', '张三', NULL, '13800000001', 'zhangsan@campus.edu', 'USER', 1),
('lisi',     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCVHGkGjxJ8NMGXzUKzuBuq', '李四', NULL, '13800000002', 'lisi@campus.edu',     'USER', 1),
('wangwu',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCVHGkGjxJ8NMGXzUKzuBuq', '王五', NULL, '13800000003', 'wangwu@campus.edu',   'USER', 1),
('zhaoliu',  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCVHGkGjxJ8NMGXzUKzuBuq', '赵六', NULL, '13800000004', 'zhaoliu@campus.edu',  'USER', 1),
('xiaoming', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCVHGkGjxJ8NMGXzUKzuBuq', '小明', NULL, '13800000005', 'xiaoming@campus.edu', 'USER', 1),
('xiaohong', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCVHGkGjxJ8NMGXzUKzuBuq', '小红', NULL, '13800000006', 'xiaohong@campus.edu', 'USER', 1);

-- ============================================================
-- 初始化：示例商品（seller_id 对应上面的用户）
-- ============================================================
INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, views, likes, location) VALUES
(2, 'iPhone 14 Pro 256G 暗紫色', '去年9月购入，电池健康92%，无磕碰，全原装配件齐全，贴膜戴壳使用', 4999.00, 8999.00, 7, 'ON_SALE', 128, 23, '西区快递站'),
(2, '2024款 MacBook Air M3 15寸', '今年3月购入，仅办公使用，循环次数不到50，送原装充电器和保护壳', 8500.00, 12999.00, 8, 'ON_SALE', 256, 45, '图书馆一楼大厅'),
(3, '高等数学同济第七版上下册', '考完试了用不到了，书本干净无笔记，附赠配套习题册', 25.00, 89.00, 11, 'ON_SALE', 89, 12, '教学楼A栋门口'),
(3, '考研英语真题黄皮书2024', '做了一半，答案册全新未拆，适合25考研同学', 35.00, 68.00, 12, 'ON_SALE', 67, 8, '图书馆二楼'),
(4, 'AirPods Pro 2 USB-C版', '使用半年，降噪效果完美，耳塞全新替换过，带原装盒', 899.00, 1899.00, 9, 'ON_SALE', 198, 34, '南门星巴克'),
(4, '索尼WH-1000XM5 头戴式耳机', '银色款，佩戴舒适降噪顶级，配件齐全带收纳包', 1299.00, 2999.00, 9, 'ON_SALE', 145, 28, '东区食堂门口'),
(5, '宿舍小冰箱 45L', '大二买的，制冷正常无异味，毕业带不走低价出', 150.00, 599.00, 14, 'ON_SALE', 76, 15, '北区宿舍楼下'),
(5, '小米台灯Pro', '护眼台灯，亮度可调，考研期间陪伴神器，九成新', 89.00, 249.00, 14, 'ON_SALE', 54, 9, '北区宿舍楼下'),
(6, '威尔胜NBA官方比赛用球', '室内球，手感极佳，买来打了不到10次，几乎全新', 120.00, 299.00, 16, 'ON_SALE', 43, 7, '体育馆门口'),
(6, '迪卡侬瑜伽垫 加厚款', '6mm加厚防滑，用过几次，清洗干净了', 39.00, 99.00, 17, 'ON_SALE', 32, 5, '体育馆门口'),
(7, 'iPad Air 5 64G WiFi版', '星光色，配了类纸膜和妙控键盘，上课记笔记神器', 2800.00, 4799.00, 7, 'ON_SALE', 312, 56, '西区快递站'),
(7, '线性代数+概率论教材套装', '同济版线代+浙大版概率论，有少量铅笔笔记可擦除', 20.00, 76.00, 11, 'ON_SALE', 45, 6, '教学楼B栋'),
(2, '佳能EOS R50 微单相机', '今年初购入，快门次数不到2000，送64G存储卡和相机包', 4200.00, 5999.00, 10, 'ON_SALE', 167, 31, '图书馆一楼大厅'),
(3, 'Switch OLED 白色 + 3游戏', '附塞尔达王国之泪、马里奥奥德赛、健身环，全套出', 1800.00, 2998.00, 7, 'ON_SALE', 223, 42, '南门星巴克'),
(4, '优衣库联名T恤 5件套装', 'M码，KAWS/村上隆联名款，每件只穿过1-2次，打包出', 99.00, 395.00, 5, 'ON_SALE', 88, 14, '东区食堂门口'),
(5, '小米空气净化器4 Pro', '滤芯刚换新，适合宿舍或出租屋使用，静音效果好', 399.00, 999.00, 14, 'ON_SALE', 61, 11, '北区宿舍楼下'),
(6, '考研政治肖秀荣全套2025', '肖四肖八+精讲精练+1000题，全新未拆封', 55.00, 158.00, 12, 'ON_SALE', 134, 22, '图书馆二楼'),
(7, '罗技MX Master 3S 鼠标', '深灰色，办公鼠标天花板，手感极佳，送鼠标垫', 399.00, 749.00, 8, 'ON_SALE', 95, 18, '西区快递站'),
(2, '大学物理实验报告册（已填写参考）', '上学期的实验报告，数据完整可参考，帮你省时间', 10.00, 15.00, 13, 'ON_SALE', 201, 35, '教学楼A栋门口'),
(3, '哑铃套装 20kg 可调节', '包胶哑铃不伤地板，宿舍健身必备，买来练了两个月', 80.00, 199.00, 17, 'ON_SALE', 38, 6, '体育馆门口');

-- ============================================================
-- 初始化：商品图片（使用 picsum 占位图）
-- ============================================================
INSERT INTO goods_images (goods_id, image_url, sort_order) VALUES
(1, 'https://picsum.photos/seed/phone1/600/450', 0),
(1, 'https://picsum.photos/seed/phone1b/600/450', 1),
(2, 'https://picsum.photos/seed/laptop1/600/450', 0),
(2, 'https://picsum.photos/seed/laptop1b/600/450', 1),
(3, 'https://picsum.photos/seed/book1/600/450', 0),
(4, 'https://picsum.photos/seed/book2/600/450', 0),
(5, 'https://picsum.photos/seed/airpods1/600/450', 0),
(5, 'https://picsum.photos/seed/airpods1b/600/450', 1),
(6, 'https://picsum.photos/seed/headphone1/600/450', 0),
(7, 'https://picsum.photos/seed/fridge1/600/450', 0),
(8, 'https://picsum.photos/seed/lamp1/600/450', 0),
(9, 'https://picsum.photos/seed/basketball1/600/450', 0),
(10, 'https://picsum.photos/seed/yoga1/600/450', 0),
(11, 'https://picsum.photos/seed/ipad1/600/450', 0),
(11, 'https://picsum.photos/seed/ipad1b/600/450', 1),
(12, 'https://picsum.photos/seed/mathbook1/600/450', 0),
(13, 'https://picsum.photos/seed/camera1/600/450', 0),
(13, 'https://picsum.photos/seed/camera1b/600/450', 1),
(14, 'https://picsum.photos/seed/switch1/600/450', 0),
(14, 'https://picsum.photos/seed/switch1b/600/450', 1),
(15, 'https://picsum.photos/seed/tshirt1/600/450', 0),
(16, 'https://picsum.photos/seed/purifier1/600/450', 0),
(17, 'https://picsum.photos/seed/politics1/600/450', 0),
(18, 'https://picsum.photos/seed/mouse1/600/450', 0),
(19, 'https://picsum.photos/seed/physics1/600/450', 0),
(20, 'https://picsum.photos/seed/dumbbell1/600/450', 0);
