package ru.mtuci.Services;

import io.jsonwebtoken.io.IOException;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

public class BinaryUtils {
    public static void writeU16(ByteArrayOutputStream out, int v) {
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }
    public static void writeU32(ByteArrayOutputStream out, int v) {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }
    public static void writeS64(ByteArrayOutputStream out, long v) {
        for (int i = 7; i >= 0; i--) {
            out.write((int) (v >>> (i * 8)) & 0xFF);
        }
    }
    public static void writeString(ByteArrayOutputStream out, String s) throws IOException, java.io.IOException {
        byte[] bytes = s != null ? s.getBytes(StandardCharsets.UTF_8) : new byte[0];
        writeU32(out, bytes.length);
        out.write(bytes);
    }
    public static void writeUUID(ByteArrayOutputStream out, UUID uuid) {
        writeS64(out, uuid.getMostSignificantBits());
        writeS64(out, uuid.getLeastSignificantBits());
    }
    public static void writeHexAsBytes(ByteArrayOutputStream out, String hex) {
        byte[] bytes = HexFormat.of().parseHex(hex); // Java 17+
        writeU32(out, bytes.length);
        out.write(bytes, 0, bytes.length);
    }
}