package com.biliww.zhuangxiu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 阶段：标准 8 工序（拆改→水电→泥瓦→木工→油漆→安装→软装→验收）。
 * budget 支持"阶段预算 vs 实际"对比。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "phases")
@EntityListeners(AuditingEntityListener.class)
public class Phase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** 未开始 / 进行中 / 已完成 */
    @Column(nullable = false, length = 20)
    private String status = "未开始";

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(length = 2000)
    private String note;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
