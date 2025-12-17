package co.ke.xently.common.utils.dto;

import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Builder
public record ResponsePayload<T>(
        String statusCode,
        String statusDescription,
        String messageCode,
        String messageDescription,
        List<ErrorInfo> errorInfo,
        String messageID,
        String conversationID,
        List<AdditionalData> additionalData,
        T primaryData
) {
    public ResponsePayload(T primaryData) {
        this(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                primaryData
        );
    }

    @Override
    public List<AdditionalData> additionalData() {
        return Objects.requireNonNullElseGet(additionalData, Collections::emptyList);
    }

    @Builder
    public record ErrorInfo(String errorCode, String errorDescription) {
    }

    @Builder
    public record AdditionalData(String key, String value) {
    }
}