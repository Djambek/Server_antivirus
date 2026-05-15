package ru.mtuci.Models;

import lombok.Data;

@Data
public class SignatureRequest {
    private String threatName;
    private String firstBytesHex;
    private String remainderHashHex;
    private long remainderLength;
    private String fileType;
    private long offsetStart;
    private long offsetEnd;
}