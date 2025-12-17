package com.kcbgroup.common.headers.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public final class HeadersValidationException extends ResponseStatusException {
    @NonNull
    private final List<HeaderException> headerExceptions;

    public HeadersValidationException() {
        super(HttpStatus.BAD_REQUEST);
        headerExceptions = new ArrayList<>();
    }

    public void addHeaderException(@NonNull HeaderException headerException) {
        headerExceptions.add(headerException);
    }
}
