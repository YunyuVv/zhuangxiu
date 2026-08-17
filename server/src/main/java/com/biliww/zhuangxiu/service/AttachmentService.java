package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.Attachment;
import com.biliww.zhuangxiu.domain.Project;
import com.biliww.zhuangxiu.dto.AttachmentSaveRequest;
import com.biliww.zhuangxiu.dto.UploadKeyResponse;
import com.biliww.zhuangxiu.repository.AttachmentRepository;
import com.biliww.zhuangxiu.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.imgbb.api-key}")
    private String apiKey;

    @Value("${app.imgbb.upload-url}")
    private String uploadUrl;

    /** 登录后返回 imgbb 上传 Key（前端缓存 24h，按研发评审结论）。 */
    public UploadKeyResponse getUploadKey(Long userId) {
        UploadKeyResponse r = new UploadKeyResponse();
        r.setApiKey(apiKey);
        r.setUploadUrl(uploadUrl);
        r.setExpiresInSeconds(86400L);
        return r;
    }

    /** 前端 imgbb 直传后，把图片 URL 存回后端。 */
    public void save(Long userId, AttachmentSaveRequest req) {
        Project p = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "项目不存在"));
        if (!p.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限");
        }
        Attachment a = new Attachment();
        a.setProjectId(req.getProjectId());
        a.setRefType(req.getRefType());
        a.setRefId(req.getRefId());
        a.setUrl(req.getUrl());
        a.setThumbUrl(req.getThumbUrl());
        attachmentRepository.save(a);
    }
}
