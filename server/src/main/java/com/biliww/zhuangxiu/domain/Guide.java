package com.biliww.zhuangxiu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 装修指南（阶段全景，公开内容，不关联 user_id）。
 * 以 stage_key 对应「阶段时间线」的 8 标准阶段。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "guides")
@EntityListeners(AuditingEntityListener.class)
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_key", nullable = false, unique = true, length = 30)
    private String stageKey;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(length = 50)
    private String duration;

    @Column(name = "cost_ratio", precision = 5, scale = 4, nullable = false)
    private BigDecimal costRatio = BigDecimal.ZERO;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
