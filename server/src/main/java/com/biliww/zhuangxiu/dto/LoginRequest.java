package com.biliww.zhuangxiu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
