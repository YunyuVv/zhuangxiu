package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 项目更新：字段均可选，仅更新非空项。
 */
@Data
@RegisterReflectionForBinding
public class ProjectUpdateRequest {

    private String name;
    private String address;
    private BigDecimal area;
    private BigDecimal totalBudget;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String status;
}
