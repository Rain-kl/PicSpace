package io.ryan.picspace.common.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Data
@AllArgsConstructor
@Slf4j
public class MinioOssUtil {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {
        // 创建MinioClient实例
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType("application/octet-stream")
                            .build()
            );
        } catch (MinioException e) {
            log.error("MinIO异常: {}", e.getMessage());
            System.out.println("MinIO异常: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        } catch (IOException e) {
            log.error("IO异常: {}", e.getMessage());
            System.out.println("IO异常: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("安全异常: {}", e.getMessage());
            System.out.println("安全异常: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }

        // 文件访问路径规则 http://endpoint/bucketName/objectName
        StringBuilder stringBuilder = new StringBuilder();

        // 处理endpoint，如果没有http://或https://前缀，默认添加http://
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            stringBuilder.append("http://");
        }

        stringBuilder
                .append(endpoint.replaceFirst("^https?://", ""))
                .append("/")
                .append(bucketName)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}