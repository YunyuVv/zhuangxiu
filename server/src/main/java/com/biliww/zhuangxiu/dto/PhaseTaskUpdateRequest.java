package com.biliww.zhuangxiu.dto;

import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding
public class PhaseTaskUpdateRequest {

    private String name;
    private Boolean done;
    private Integer sortOrder;
    private String note;
}
