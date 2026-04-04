package co.ke.xently.demoweb;

import lombok.Builder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SpringBootApplication
@RestController
@RequestMapping("/api/hello")
@Validated
public class Application {

    @Builder
    public record RequestPayload<T>(
            String messageID,
            T primaryData
    ) {
    }

    @Builder
    public record ResponsePayload<T>(
            String statusCode,
            String statusDescription,
            String messageCode,
            String messageDescription,
            String messageID,
            T primaryData
    ) {
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static <Request, Response> ResponsePayload<Response> createResponse(
            ResponsePayload.ResponsePayloadBuilder<Response> response,
            RequestPayload<Request> request
    ) {
        response.messageCode("200")
                .messageDescription("OK!")
                .statusCode("0")
                .statusDescription("Success");
        if (request != null) {
            response.messageID(request.messageID());
        }
        return response.build();
    }

    @GetMapping
    ResponsePayload<String> hello() {
        var response = ResponsePayload.<String>builder()
                .primaryData("Hello, World!");
        return createResponse(response, null);
    }

    @PostMapping
    ResponsePayload<String> hello(@Validated @RequestBody RequestPayload<String> payload) {
        var response = ResponsePayload.<String>builder()
                .primaryData("Hello, %s!".formatted(payload.primaryData()));
        return createResponse(response, payload);
    }

    @PostMapping(value = "/multipart", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponsePayload<String> hello(
            @Validated
            @RequestPart(value = "payload")
            RequestPayload<String> payload,
            @Validated
            @RequestPart(value = "file") MultipartFile file
    ) {
        var response = ResponsePayload.<String>builder()
                .primaryData("""
                        Hello, %s! You uploaded "%s".""".formatted(payload.primaryData(), file.getOriginalFilename()));
        return createResponse(response, payload);
    }
}
