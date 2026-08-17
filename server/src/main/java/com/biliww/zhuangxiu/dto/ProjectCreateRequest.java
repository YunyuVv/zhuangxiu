package com.biliww.zhuangxiu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RegisterReflectionForBinding
public class ProjectCreateRequest {

    @NotBlank
    @jakarta.validation.constraints.Size(max = 100)
    private String name;

    @jakarta.validation.constraints.Size(max = 255)
    private String address;

    private BigDecimal area;

    @NotNull
    private BigDecimal totalBudget;

    private LocalDate startDate;

    private LocalDate expectedEndDate;
}
