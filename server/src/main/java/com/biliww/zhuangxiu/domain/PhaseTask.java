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
 * 阶段子项：某阶段内用户自定义的待办/检查清单（如「盯打压/验防水」）。
 * 与 expenses 区分：子项无金额，只用于阶段内逐项勾选；完成度可反映阶段推进细节。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "phase_tasks")
@EntityListeners(AuditingEntityListener.class)
public class PhaseTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "phase_id", nullable = false)
    private Long phaseId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean done = false;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(length = 2000)
    private String note;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
