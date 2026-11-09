package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@RegisterReflectionForBinding
public class ExpenseVO {

    private Long id;
    private Long phaseId;
    private String name;
    private String category;
    private BigDecimal amount;
    private String payMethod;
    private LocalDate expenseDate;
    private String note;
    private LocalDateTime createdAt;
}
