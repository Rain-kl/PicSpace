package io.ryan.picspace.manager;

import io.ryan.picspace.common.utils.MinioOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component // 或 @Component，默认就是单例
@Slf4j
public class FileManager {
    @Autowired
    MinioOssUtil minioOssUtil;

    public String upload(MultipartFile file, String fileName) {
        try {
            return minioOssUtil.upload(file.getInputStream().readAllBytes(), fileName);
        } catch (Exception e) {
            log.error("File upload error", e);
            return null;
        }

    }

}