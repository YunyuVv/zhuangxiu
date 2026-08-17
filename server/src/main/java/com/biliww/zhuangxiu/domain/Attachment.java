package com.biliww.zhuangxiu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 附件（图片）：imgbb URL；通过 ref_type + ref_id 多态关联项目/阶段/花销。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /** PROJECT / PHASE / EXPENSE */
    @Column(name = "ref_type", nullable = false, length = 20)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(nullable = false)
    private String url;

    @Column(name = "thumb_url")
    private String thumbUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
