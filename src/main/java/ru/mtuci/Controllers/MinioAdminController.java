package ru.mtuci.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mtuci.Services.MinioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/storage")
@Tag(name = "Управление файловым хранилищем (MinIO)")
@RequiredArgsConstructor
public class MinioAdminController {

    private final MinioService minioService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "Загрузка файла в хранилище")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Генерируем уникальное имя
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 2. Исправленный вызов: передаем 4 параметра из MultipartFile
            minioService.uploadFile(
                    objectName,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            return ResponseEntity.ok(Map.of(
                    "objectName", objectName,
                    "message", "Файл успешно загружен"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка при обработке файла: " + e.getMessage()));
        }
    }

    @GetMapping("/url/{objectName}")
    @Operation(summary = "Получение pre-signed URL для файла")
    public ResponseEntity<?> getUrl(@PathVariable String objectName) {
        String url = minioService.getPresignedUrl(objectName);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/urls/batch")
    @Operation(summary = "Массовое получение ссылок по списку имен объектов")
    public ResponseEntity<?> getBatchUrls(@RequestBody List<String> objectNames) {
        Map<String, String> urls = new HashMap<>();
        for (String name : objectNames) {
            try {
                urls.put(name, minioService.getPresignedUrl(name));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(urls);
    }
}