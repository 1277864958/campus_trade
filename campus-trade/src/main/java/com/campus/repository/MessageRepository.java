package com.campus.repository;
import com.campus.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatIdOrderByCreatedAtAsc(Long chatId, Pageable pageable);
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatId=:chatId AND m.senderId<>:userId AND m.isRead=0")
    long countUnread(@Param("chatId") Long chatId, @Param("userId") Long userId);
    @Modifying @Transactional
    @Query("UPDATE Message m SET m.isRead=1 WHERE m.chatId=:chatId AND m.senderId<>:userId AND m.isRead=0")
    int markAllRead(@Param("chatId") Long chatId, @Param("userId") Long userId);
}
