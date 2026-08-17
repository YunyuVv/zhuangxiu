package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RegisterReflectionForBinding
public class PhaseUpdateRequest {

    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private String note;
}
