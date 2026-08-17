package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@RegisterReflectionForBinding
public class ProjectVO {

    private Long id;
    private String name;
    private String address;
    private BigDecimal area;
    private BigDecimal totalBudget;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String status;
    private LocalDateTime createdAt;
}
