package ru.mtuci.Configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.internal-endpoint}")
    private String internalEndpoint;

    @Value("${minio.external-endpoint}")
    private String externalEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean(name = "internalMinioClient")
    public MinioClient internalMinioClient() {
        return MinioClient.builder()
                .endpoint(internalEndpoint)
                .credentials(accessKey, secretKey)
                .region("us-east-1") // Указываем регион
                .build();
    }

    @Bean(name = "externalMinioClient")
    public MinioClient externalMinioClient() {
        return MinioClient.builder()
                .endpoint(externalEndpoint)
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .build();
    }
}