package com.heslin.postopia.jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Data
@Table(
    name = "messages",
    indexes = {
        @Index(
        name = "idx_message_user_read_time",
        columnList = "username, is_read, created_at DESC"
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
    private String content;
    @JoinColumn(name = "username", foreignKey = @ForeignKey(name = "fk_message_user", foreignKeyDefinition = "FOREIGN KEY (username) REFERENCES users(username)"))
    private String username;
    private boolean isRead;
    Instant createdAt;

    public Message(String username, String content) {
        this.content = content;
        this.username = username;
        this.isRead = false;
        this.createdAt = Instant.now();
    }
}
