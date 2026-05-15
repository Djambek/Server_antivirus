package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import ru.mtuci.Entities.MalwareSignature;
import ru.mtuci.Entities.SignatureStatus;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BinarySignatureExportService {

    private final SignatureService signatureService;

    public byte[] generateDataBin(List<MalwareSignature> signatures) throws IOException {
        org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream out = new org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream();

        // 6.1. Заголовок data.bin
        // Magic: DB-Тлепбергенов
        out.write("DB-Тлепбергенов".getBytes());
        BinaryUtils.writeU16(out, 1);           // Version
        BinaryUtils.writeU32(out, signatures.size()); // recordCount

        // 6.2. Массив записей данных
        for (MalwareSignature sig : signatures) {
            writeDataEntry(out, sig);
        }
        return out.toByteArray();
    }

    /**
     * Генерирует manifest.bin согласно требованиям раздела 4
     */
    public byte[] generateManifestBin(List<MalwareSignature> signatures, byte[] dataBin, int exportType, long since) throws Exception {
        org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream out = new org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream();

        // 4.1. Заголовок манифеста
        out.write("MF-Тлепбергенов".getBytes()); // Magic
        BinaryUtils.writeU16(out, 1);            // Version
        out.write(exportType);                   // exportType
        BinaryUtils.writeS64(out, System.currentTimeMillis()); // generatedAt
        BinaryUtils.writeS64(out, since);        // sinceEpochMillis
        BinaryUtils.writeU32(out, signatures.size()); // recordCount

        // SHA-256 всего файла data.bin
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(dataBin);
        out.write(hash);

        // 4.2. Состав записей манифеста со смещениями
        long currentOffset = 0;
        for (MalwareSignature sig : signatures) {
            // Чтобы узнать длину записи в data.bin, временно сериализуем её
            org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream temp = new ByteArrayOutputStream();
            writeDataEntry(temp, sig);
            byte[] entryData = temp.toByteArray();

            BinaryUtils.writeUUID(out, sig.getId()); // id
            // statusCode: ACTUAL=1, DELETED=0
            out.write(sig.getStatus() == SignatureStatus.ACTUAL ? 1 : 0);
            BinaryUtils.writeS64(out, sig.getUpdatedAt().toEpochMilli()); // updatedAt
            BinaryUtils.writeS64(out, currentOffset); // dataOffset
            BinaryUtils.writeS64(out, entryData.length); // dataLength

            // Подпись из БД (уже существующая)
            byte[] sigBytes = Base64.getDecoder().decode(sig.getDigitalSignatureBase64());
            BinaryUtils.writeU32(out, sigBytes.length); // recordSignatureLength
            out.write(sigBytes); // recordSignatureBytes

            currentOffset += entryData.length;
        }

        // 4.3. Подпись манифеста (подписываем накопленные байты)
        byte[] manifestBody = out.toByteArray();
        byte[] manifestSig = signatureService.signBytes(manifestBody);

        BinaryUtils.writeU32(out, manifestSig.length); // manifestSignatureLength
        out.write(manifestSig); // manifestSignatureBytes

        return out.toByteArray();
    }

    private void writeDataEntry(org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream out, MalwareSignature sig) throws IOException {
        BinaryUtils.writeString(out, sig.getThreatName()); // threatName (length + bytes)
        BinaryUtils.writeHexAsBytes(out, sig.getFirstBytesHex()); // firstBytes (raw)
        BinaryUtils.writeHexAsBytes(out, sig.getRemainderHashHex()); // remainderHash (raw)
        BinaryUtils.writeS64(out, sig.getRemainderLength()); // remainderLength
        BinaryUtils.writeString(out, sig.getFileType()); // fileType
        BinaryUtils.writeS64(out, sig.getOffsetStart()); // offsetStart
        BinaryUtils.writeS64(out, sig.getOffsetEnd()); // offsetEnd
    }
}