package com.kcbgroup.common.utils.converter;

import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.utils.dto.Request;
import com.kcbgroup.common.utils.dto.ResponsePayload;
import org.springframework.lang.NonNull;

public interface PayloadConverter {
    /**
     * Will be used to extract the messageID from the payload if it returns an instance of {@link Request}.
     *
     * @param payload request-body that has been converted to a POJO by the bundled spring message converter
     *                - that is capable of extracting a messageID.
     * @return should be an instance of {@link Request} to be able to extract a messageID that will be used
     * to create a default ({@link ResponsePayload}) header validation error response.
     */
    @NonNull
    default Object convertToRequestPayload(@NonNull Object payload) {
        return payload;
    }

    /**
     * Will be used to convert the {@link HeadersValidationException} to a custom response body if the
     * default does not conform to the expectation.
     *
     * @param payload   the default response body that will be rendered as the API response in case of a
     *                  header validation error.
     * @param exception the exception that was thrown during the request processing.
     * @return a custom response body that will be rendered as the API response.
     */
    @NonNull
    default Object convertToHeaderValidationErrorResponse(
            @NonNull ResponsePayload<?> payload,
            @SuppressWarnings("unused") @NonNull HeadersValidationException exception
    ) {
        return payload;
    }
}
