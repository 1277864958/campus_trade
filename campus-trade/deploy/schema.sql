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
