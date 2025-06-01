package com.heslin.postopia.message.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Data
@Table(
    name = "messages",
    indexes = {
        @Index(
        name = "idx_message_user_read_time",
        columnList = "user_id, is_read, created_at DESC"
        )
    }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_sequence")
    @SequenceGenerator(
    name = "message_sequence",  // 生成器名称，需与 generator 参数一致
    sequenceName = "message_sequence",  // 数据库中的实际序列名
    allocationSize = 1000  // 每次分配的序列值数量（需与数据库序列的 INCREMENT 一致）
    )
    private Long id;
    @Column(length = 1200)
    private String content;
    private Long userId;
    @Column(name = "is_read")
    private boolean read;
    @CreatedDate
    Instant createdAt;
}
