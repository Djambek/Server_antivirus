package ru.mtuci.Models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignaturePayload {
    private String threatName;
    private String firstBytesHex;
    private String remainderHashHex;
    private long remainderLength;
    private String fileType;
    private long offsetStart;
    private long offsetEnd;
    private String status;
}