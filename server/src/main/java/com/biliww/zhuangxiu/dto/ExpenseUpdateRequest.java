package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RegisterReflectionForBinding
public class ExpenseUpdateRequest {

    private Long phaseId;
    private String name;
    private String category;
    private BigDecimal amount;
    private String payMethod;
    private LocalDate expenseDate;
    private String note;
}
