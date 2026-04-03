package co.ke.xently.common.utils.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ResponsePayload(
        String messageCode,
        String messageDescription,
        List<ErrorInfo> errorInfo
) {
    @Builder
    public record ErrorInfo(String errorCode, String errorDescription) {
    }
}