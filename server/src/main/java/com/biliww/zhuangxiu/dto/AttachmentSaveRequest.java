package com.biliww.zhuangxiu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding
public class AttachmentSaveRequest {

    @NotNull
    private Long projectId;

    @NotBlank
    @jakarta.validation.constraints.Size(max = 20)
    private String refType;

    private Long refId;

    @NotBlank
    private String url;

    private String thumbUrl;
}
