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

    public String signTicket(Ticket ticket) {
        try {
            // 1. Получаем приватный ключ из Key Provider
            PrivateKey privateKey = keyStoreService.getPrivateKey();

            // 2. Канонизация по RFC 8785
            String canonicalJson = jsonCanonicalizer.canonizeJson(ticket);

            // 3. Получаем байты строго в кодировке UTF-8
            byte[] canonicalBytes = canonicalJson.getBytes(StandardCharsets.UTF_8);

            // 4. Инициализация подписи
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            // 5. Вычисление подписи
            signature.update(canonicalBytes);
            byte[] signedBytes = signature.sign();

            // 6. Возврат результата в Base64
            return Base64.getEncoder().encodeToString(signedBytes);

        } catch (Exception e) {
            log.error("Ошибка при создании цифровой подписи тикета", e);
            throw new RuntimeException("Не удалось подписать тикет", e);
        }
    }
}