package com.campus.repository;

import com.campus.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 逻辑：如果我是发送方，且我没删，就能看；如果我是接收方，且我没删，也能看。
// 【修改点】：去掉硬编码的 ORDER BY，通过外层的 Pageable 动态控制升降序
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId " +
            "AND ((m.senderId = :userId AND m.deletedBySender = false) " +
            "OR (m.senderId <> :userId AND m.deletedByReceiver = false))")
    Page<Message> findVisibleHistory(@Param("chatId") Long chatId, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatId=:chatId AND m.senderId<>:userId AND m.isRead=false")
    long countUnread(@Param("chatId") Long chatId, @Param("userId") Long userId);

    // 适应 Boolean 类型，把 1 改成了 true，0 改成了 false
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead=true WHERE m.chatId=:chatId AND m.senderId<>:userId AND m.isRead=false")
    int markAllRead(@Param("chatId") Long chatId, @Param("userId") Long userId);

    // 【新增核心代码】：利用 Spring Data JPA 方法名推导，作为 Redis 未读数过期的兜底查询
    long countByChatIdAndSenderIdNotAndIsReadFalse(Long chatId, Long userId);
}