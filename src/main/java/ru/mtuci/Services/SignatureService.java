package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mtuci.Models.Ticket;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignatureService {

    // Внедряем провайдер ключей
    private final SignatureKeyStoreService keyStoreService;

    // Внедряем ваш канонизатор (т.к. они в одном пакете ru.mtuci.Services, импорт не нужен)
    private final JsonCanonicalizer jsonCanonicalizer;

    public String signPayload(Object payload) {
        try {
            PrivateKey privateKey = keyStoreService.getPrivateKey();
            // Канонизатор уже принимает Object
            String canonicalJson = jsonCanonicalizer.canonizeJson(payload);
            byte[] canonicalBytes = canonicalJson.getBytes(StandardCharsets.UTF_8);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(canonicalBytes);
            byte[] signedBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            log.error("Ошибка при создании цифровой подписи", e);
            throw new RuntimeException("Не удалось подписать полезную нагрузку", e);
        }
    }
    public String signTicket(ru.mtuci.Models.Ticket ticket) {
        return signPayload(ticket);
    }
}