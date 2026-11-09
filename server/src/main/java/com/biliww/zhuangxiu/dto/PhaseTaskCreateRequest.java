package com.biliww.zhuangxiu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding
public class PhaseTaskCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 2000)
    private String note;
}
