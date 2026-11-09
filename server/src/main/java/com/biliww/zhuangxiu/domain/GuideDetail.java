package com.biliww.zhuangxiu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 单阶段攻略明细（P1：买什么/找谁/避坑点/预算区间）。
 * 由运营在库内维护，前端攻略页按 sort_order 展示。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "guide_details")
@EntityListeners(AuditingEntityListener.class)
public class GuideDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guide_id", nullable = false)
    private Long guideId;

    @Column(nullable = false, length = 30)
    private String section;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
