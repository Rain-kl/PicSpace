package io.ryan.picspace.manager;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
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

    public String upload(byte[] bytes, String fileName) {
        try {
            return minioOssUtil.upload(bytes, fileName);
        } catch (Exception e) {
            log.error("File upload error", e);
            return null;
        }
    }

    public String uploadByUrl(String fileUrl, String fileName) {
        byte[] bytes = HttpUtil.downloadBytes(fileUrl);
        return minioOssUtil.upload(bytes, fileName);
    }

    public HttpResponse doHeadRequest(String fileUrl) {
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
            return response;
        }
    }

}