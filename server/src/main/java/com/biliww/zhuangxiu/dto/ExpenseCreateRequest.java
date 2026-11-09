package com.biliww.zhuangxiu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RegisterReflectionForBinding
public class ExpenseCreateRequest {

    private Long phaseId;

    /** 物品/器材名称（采购清单必填）。 */
    @NotBlank
    @jakarta.validation.constraints.Size(max = 100)
    private String name;

    @NotBlank
    @jakarta.validation.constraints.Size(max = 30)
    private String category;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @jakarta.validation.constraints.Size(max = 30)
    private String payMethod;

    @NotNull
    private LocalDate expenseDate;

    private String note;
}
