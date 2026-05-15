package ru.mtuci.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.Entities.MalwareSignature;
import ru.mtuci.Entities.SignatureStatus;
import ru.mtuci.Models.BatchSignatureRequest;
import ru.mtuci.Repositories.MalwareSignatureRepository;
import ru.mtuci.Services.BinarySignatureExportService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/binary/signatures")
@RequiredArgsConstructor
@Tag(name = "Бинарный API сигнатур", description = "Методы для высокопроизводительной выгрузки антивирусных баз в бинарном формате")
public class BinarySignatureController {

    private final MalwareSignatureRepository repository;
    private final BinarySignatureExportService exportService;

    @Operation(
            summary = "Полная выгрузка базы",
            description = "Возвращает все актуальные сигнатуры (status=ACTUAL) в формате multipart/mixed. Пакет содержит manifest.bin и data.bin."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная генерация бинарного пакета", content = @Content(mediaType = "multipart/mixed")),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping(value = "/full", produces = "multipart/mixed")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> getFull() throws Exception {
        List<MalwareSignature> signs = repository.findAllByStatus(SignatureStatus.ACTUAL);
        return buildMultipartResponse(signs, 1, -1);
    }

    @Operation(
            summary = "Инкрементальное обновление",
            description = "Возвращает сигнатуры, измененные после указанного времени (updatedAt > since). Включает записи ACTUAL и DELETED."
    )
    @GetMapping(value = "/increment", produces = "multipart/mixed")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> getIncrement(
            @Parameter(description = "Временная метка начала изменений (ISO 8601)", required = true)
            @RequestParam Instant since) throws Exception {
        List<MalwareSignature> signs = repository.findByUpdatedAtAfter(since);
        return buildMultipartResponse(signs, 2, since.toEpochMilli());
    }

    @Operation(
            summary = "Выгрузка по списку ID",
            description = "Принимает список UUID и формирует бинарный пакет только для найденных записей."
    )
    @PostMapping(value = "/by-ids", produces = "multipart/mixed")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> getByIds(
            @RequestBody BatchSignatureRequest request) throws Exception {
        List<MalwareSignature> signs = repository.findAllById(request.getIds());
        return buildMultipartResponse(signs, 3, -1);
    }

    private ResponseEntity<MultiValueMap<String, HttpEntity<?>>> buildMultipartResponse(List<MalwareSignature> signs, int type, long since) throws Exception {
        byte[] data = exportService.generateDataBin(signs);
        byte[] manifest = exportService.generateManifestBin(signs, data, type, since);

        MultiValueMap<String, HttpEntity<?>> body = new LinkedMultiValueMap<>();
        body.add("manifest", createPart(manifest, "manifest.bin"));
        body.add("data", createPart(data, "data.bin"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("multipart/mixed; boundary=gc0p4Jq0M2Yt08j34c0p"))
                .body(body);
    }

    private HttpEntity<byte[]> createPart(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return new HttpEntity<>(data, headers);
    }
}