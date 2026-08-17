package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RegisterReflectionForBinding
public class PhaseVO {

    private Long id;
    private String name;
    private int sortOrder;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private BigDecimal budget;
    private String note;
}
