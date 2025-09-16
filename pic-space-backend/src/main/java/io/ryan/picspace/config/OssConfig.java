package io.ryan.picspace.config;

import io.ryan.picspace.config.properties.MinioOssProperties;
import io.ryan.picspace.common.utils.MinioOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfig {

    @Bean
    @ConditionalOnMissingBean
    public MinioOssUtil minioOssUtil(MinioOssProperties properties) {
        return new MinioOssUtil(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName()
        );
    }
}
