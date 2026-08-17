package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding
public class UploadKeyResponse {

    private String apiKey;
    private String uploadUrl;
    private long expiresInSeconds;
}
