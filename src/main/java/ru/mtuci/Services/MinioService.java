package ru.mtuci.Services;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {

    private final MinioClient internalClient;
    private final MinioClient externalClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioService(
            @Qualifier("internalMinioClient") MinioClient internalClient,
            @Qualifier("externalMinioClient") MinioClient externalClient) {
        this.internalClient = internalClient;
        this.externalClient = externalClient;

        log.warn("=== [DEBUG] ИНИЦИАЛИЗАЦИЯ MINIO SERVICE ===");
        log.warn("internalClient и externalClient успешно внедрены");
    }

    public void uploadFile(String objectName, InputStream inputStream, long size, String contentType) {
        log.warn("=== [DEBUG] НАЧАЛО ЗАГРУЗКИ ФАЙЛА В MINIO ===");
        log.warn("-> Объект: {}", objectName);
        log.warn("-> Размер: {} байт", size);
        log.warn("-> Тип контента: {}", contentType);
        log.warn("-> Целевой бакет: {}", bucketName);

        try {
            log.warn("-> Шаг 1: Проверка существования бакета...");
            boolean found = internalClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                log.warn("-> Шаг 1.1: Бакет '{}' не найден. Создаю новый...", bucketName);
                internalClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.warn("-> Бакет успешно создан.");
            } else {
                log.warn("-> Шаг 1.1: Бакет '{}' существует. Продолжаем...", bucketName);
            }

            log.warn("-> Шаг 2: Выполняю загрузку (putObject) через internalClient...");
            internalClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.warn("=== [DEBUG] ФАЙЛ УСПЕШНО ЗАГРУЖЕН В MINIO ===");

        } catch (Exception e) {
            log.error("=== [DEBUG] ОШИБКА ПРИ ЗАГРУЗКЕ В MINIO ===");
            log.error("-> Класс ошибки: {}", e.getClass().getName());
            log.error("-> Сообщение: {}", e.getMessage());
            log.error("-> Стек вызовов:", e);
            throw new RuntimeException("Failed to store file in MinIO: " + e.getMessage(), e);
        }
    }

    public String getPresignedUrl(String objectName) {
        log.warn("=== [DEBUG] НАЧАЛО ГЕНЕРАЦИИ URL ДЛЯ MINIO ===");
        log.warn("-> Запрашиваемый объект: {}", objectName);

        try {
            log.warn("-> Шаг 1: Вызываю getPresignedObjectUrl у externalClient...");
            String url = externalClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );

            log.warn("-> Сгенерированный URL: {}", url);
            log.warn("=== [DEBUG] УСПЕШНАЯ ГЕНЕРАЦИЯ URL ===");
            return url;

        } catch (Exception e) {
            log.error("=== [DEBUG] ОШИБКА ГЕНЕРАЦИИ URL В MINIO ===");
            log.error("-> Сообщение: {}", e.getMessage());
            log.error("-> Стек вызовов:", e);
            throw new RuntimeException("Could not generate download link: " + e.getMessage(), e);
        }
    }
}