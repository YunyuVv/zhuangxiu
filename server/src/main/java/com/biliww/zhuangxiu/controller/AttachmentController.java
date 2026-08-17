package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.dto.AttachmentSaveRequest;
import com.biliww.zhuangxiu.dto.UploadKeyResponse;
import com.biliww.zhuangxiu.security.SecurityUtil;
import com.biliww.zhuangxiu.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/upload-key")
    public ApiResult<UploadKeyResponse> uploadKey() {
        return ApiResult.success(attachmentService.getUploadKey(SecurityUtil.getCurrentUserId()));
    }

    @PostMapping
    public ApiResult<Void> save(@Valid @RequestBody AttachmentSaveRequest req) {
        attachmentService.save(SecurityUtil.getCurrentUserId(), req);
        return ApiResult.success();
    }
}
