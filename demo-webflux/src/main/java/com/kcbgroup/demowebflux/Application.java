package com.kcbgroup.demowebflux;

import com.kcbgroup.common.KCBRequestContextHolder;
import com.kcbgroup.common.utils.dto.Request;
import com.kcbgroup.common.utils.dto.ResponsePayload;
import lombok.Builder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
@RestController
@RequestMapping("/api/hello")
@Validated
public class Application {
    @Builder
    public record RequestPayload<T>(
            String messageID,
            T primaryData,
            List<ResponsePayload.AdditionalData> additionalData
    ) implements Request {
        @Override
        public List<ResponsePayload.AdditionalData> additionalData() {
            return Objects.requireNonNullElseGet(additionalData, Collections::emptyList);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static <Request, Response> ResponsePayload<Response> createResponse(
            ResponsePayload.ResponsePayloadBuilder<Response> response,
            RequestPayload<Request> request
    ) {
        response.conversationID(KCBRequestContextHolder.getContext().conversationID())
                .messageCode("200")
                .messageDescription("OK!")
                .statusCode("0")
                .statusDescription("Success")
                .errorInfo(Collections.emptyList());
        if (request != null) {
            response.messageID(request.messageID())
                    .additionalData(request.additionalData());
        }
        return response.build();
    }

    @GetMapping
    Mono<ResponsePayload<String>> hello() {
        var response = ResponsePayload.<String>builder()
                .primaryData("Hello, World!");
        return Mono.just(createResponse(response, null));
    }

    @PostMapping
    Mono<ResponsePayload<String>> hello(@Validated @RequestBody RequestPayload<String> payload) {
        var response = ResponsePayload.<String>builder()
                .primaryData("Hello, %s!".formatted(payload.primaryData()));
        return Mono.just(createResponse(response, payload));
    }

    @PostMapping(value = "/multipart", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    Mono<ResponsePayload<String>> hello(
            @Validated
            @RequestPart(value = "payload")
            RequestPayload<String> payload,
            @Validated
            @RequestPart(value = "file") FilePart file
    ) {
        var response = ResponsePayload.<String>builder()
                .primaryData("""
                        Hello, %s! You uploaded "%s".""".formatted(payload.primaryData(), file.filename()));
        return Mono.just(createResponse(response, payload));
    }
}
