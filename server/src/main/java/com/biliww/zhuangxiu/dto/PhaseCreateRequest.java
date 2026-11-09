package com.biliww.zhuangxiu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding
public class PhaseCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String name;
}
