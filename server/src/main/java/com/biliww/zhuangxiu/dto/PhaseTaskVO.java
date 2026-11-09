package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.time.LocalDateTime;

@Data
@RegisterReflectionForBinding
public class PhaseTaskVO {

    private Long id;
    private Long phaseId;
    private String name;
    private boolean done;
    private int sortOrder;
    private String note;
    private LocalDateTime createdAt;
}
