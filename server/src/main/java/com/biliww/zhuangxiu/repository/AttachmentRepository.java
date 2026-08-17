package com.biliww.zhuangxiu.repository;

import com.biliww.zhuangxiu.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByRefTypeAndRefId(String refType, Long refId);

    List<Attachment> findByProjectId(Long projectId);
}
