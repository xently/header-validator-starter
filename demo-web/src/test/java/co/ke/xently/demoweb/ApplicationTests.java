package co.ke.xently.demoweb;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {
    @LocalServerPort
    private int port = 0;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.replaceFiltersWith(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.config = RestAssured.config()
                .encoderConfig(EncoderConfig.encoderConfig()
                        .encodeContentTypeAs("multipart/form-data", ContentType.MULTIPART));
    }

    private static List<Header> validHeaders() {
        return List.of(
                new Header("X-FeatureName", "Test"),
                new Header("X-ServiceCode", "10001"),
                new Header("X-ServiceName", "Example Service"),
                new Header("X-MinorServiceVersion", "v1.0"),
                new Header("X-ChannelCategory", "102"),
                new Header("X-ChannelCode", "10"),
                new Header("X-ChannelName", "App"),
                new Header("X-Additional-Required", "X-Additional-Required"),
                new Header("X-Timestamp", Instant.now().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT))
        );
    }

    @Nested
    @DisplayName("GET - /api/hello")
    class GET {
        @Test
        void shouldRespondWith200() {
            var headers = new Headers(
                    List.of(
                            new Header("X-FeatureName", "Test"),
                            new Header("X-ServiceCode", "10001"),
                            new Header("X-ServiceName", "Example Service"),
                            new Header("X-MinorServiceVersion", "v1.0"),
                            new Header("X-ChannelCategory", "102"),
                            new Header("X-ChannelCode", "10"),
                            new Header("X-ChannelName", "App"),
                            new Header("X-Additional-Required", "X-Additional-Required"),
                            new Header("X-Timestamp", Instant.now().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT))
                    )
            );

            given()
                    .headers(headers)
                    .get("/api/hello")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("statusCode", equalTo("0"))
                    .body("messageCode", equalTo("200"))
                    .body("messageDescription", equalTo("OK!"))
                    .body("messageID", nullValue())
                    .body("errorInfo", emptyIterable())
                    .body("additionalData", emptyIterable())
                    .body("conversationID", not(blankOrNullString()))
                    .body("primaryData", is("Hello, World!"));
        }

        @ParameterizedTest
        @MethodSource
        void shouldRespondWith400(TestCase testCase) {
            var headers = new Headers(testCase.headers());

            given()
                    .headers(headers)
                    .get("/api/hello")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("statusCode", is("0"))
                    .body("statusDescription", is("Failed"))
                    .body("messageCode", is("4000453"))
                    .body("errorInfo.errorCode", testCase.expectedErrorCodes());
        }

        static Stream<TestCase> shouldRespondWith400() {
            List<Header> headers = new ArrayList<>(
                    List.of(
                            new Header("X-MinorServiceVersion", "v1.0"),
                            new Header("X-Additional-Required", "value")
                    )
            );
            Set.of(
                    "X-FeatureCode",
                    "X-FeatureName",
                    "X-ServiceCode",
                    "X-ServiceName",
                    "X-ServiceSubCategory",
                    "X-ChannelCategory",
                    "X-ChannelCode",
                    "X-ChannelName",
                    "X-RouteCode",
                    "X-ServiceMode",
                    "X-SubscriberEvents"
            ).forEach(h -> headers.add(new Header(h, "  ")));
            return Stream.of(
                    new TestCase(
                            List.of(),
                            containsInAnyOrder(
                                    "X-FeatureName",
                                    "X-ServiceName",
                                    "X-ServiceCode",
                                    "X-MinorServiceVersion",
                                    "X-ChannelCode",
                                    "X-ChannelName",
                                    "X-ChannelCategory",
                                    "X-Additional-Required"
                            )
                    ),
                    new TestCase(
                            headers,
                            containsInAnyOrder(
                                    "X-FeatureCode",
                                    "X-FeatureName",
                                    "X-ServiceCode",
                                    "X-ServiceName",
                                    "X-ServiceSubCategory",
                                    "X-ChannelCategory",
                                    "X-ChannelCode",
                                    "X-ChannelName",
                                    "X-RouteCode",
                                    "X-ServiceMode",
                                    "X-SubscriberEvents"
                            )
                    ),
                    new TestCase(
                            List.of(
                                    new Header("X-FeatureName", "Test"),
                                    new Header("X-ServiceCode", "10001"),
                                    new Header("X-ServiceName", "Example Service"),
                                    new Header("X-MinorServiceVersion", "v1.0"),
                                    new Header("X-ChannelCategory", "102"),
                                    new Header("X-ChannelCode", "10"),
                                    new Header("X-ChannelName", "App"),
                                    new Header("X-CallBackURL", "invalid"),
                                    new Header("X-Additional-Required", "       ")
                            ),
                            containsInAnyOrder("X-CallBackURL", "X-Additional-Required")
                    ),
                    new TestCase(
                            List.of(
                                    new Header("X-FeatureName", "Test"),
                                    new Header("X-ServiceCode", "10001"),
                                    new Header("X-ServiceName", "Example Service"),
                                    new Header("X-MinorServiceVersion", "v1.0"),
                                    new Header("X-ChannelCategory", "102"),
                                    new Header("X-ChannelCode", "10"),
                                    new Header("X-ChannelName", "App"),
                                    new Header("X-Additional-Required", "value"),
                                    new Header("X-Additional-Optional", "  ")
                            ),
                            containsInAnyOrder("X-Additional-Optional")
                    ),
                    new TestCase(
                            List.of(
                                    new Header("X-FeatureName", "Test"),
                                    new Header("X-ServiceCode", "10001"),
                                    new Header("X-ServiceName", "Example Service"),
                                    new Header("X-MinorServiceVersion", "v1.0"),
                                    new Header("X-ChannelCategory", "102"),
                                    new Header("X-ChannelCode", "10"),
                                    new Header("X-ChannelName", "App"),
                                    new Header("X-Additional-Required", "value"),
                                    new Header("X-Additional-Custom-Validator", "  ")
                            ),
                            containsInAnyOrder("X-Additional-Custom-Validator")
                    ),
                    new TestCase(
                            List.of(
                                    new Header("X-FeatureName", "Test"),
                                    new Header("X-ServiceCode", "10001"),
                                    new Header("X-ServiceName", "Example Service"),
                                    new Header("X-MinorServiceVersion", "v1.0"),
                                    new Header("X-ChannelCategory", "102"),
                                    new Header("X-ChannelCode", "10"),
                                    new Header("X-ChannelName", "App"),
                                    new Header("X-Additional-Required", "value"),
                                    new Header("X-Additional-Custom-Validator", "invalid"),
                                    new Header("X-Timestamp", String.valueOf(System.currentTimeMillis()))
                            ),
                            containsInAnyOrder("X-Additional-Custom-Validator", "X-Timestamp")
                    )
            );
        }

        private record TestCase(
                List<Header> headers,
                Matcher<Iterable<? extends String>> expectedErrorCodes
        ) {
        }
    }

    @Nested
    class POST {
        @Nested
        @DisplayName("POST - /api/hello")
        class Plain {
            @Test
            void shouldRespondWith200AndEchoMessageId() {
                var headers = new Headers(validHeaders());
                var requestBody = Map.of(
                        "messageID", "mid-123",
                        "primaryData", "World"
                );

                given()
                        .headers(headers)
                        .contentType("application/json")
                        .body(requestBody)
                        .post("/api/hello")
                        .then()
                        .statusCode(HttpStatus.OK.value())
                        .body("messageID", is("mid-123"))
                        .body("primaryData", is("Hello, World!"));
            }

            @Test
            void shouldRespondWith400ForMissingHeaders() {
                var requestBody = Map.of(
                        "messageID", "mid-123",
                        "primaryData", "World"
                );

                given()
                        .contentType("application/json")
                        .body(requestBody)
                        .post("/api/hello")
                        .then()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }
        }

        @Nested
        @DisplayName("POST - /api/hello/multipart")
        class Multipart {
            @Test
            void shouldRespondWith200AndEchoMessageIdForMultipart() {
                var headers = new Headers(validHeaders());
                // language=JSON
                var payloadJson = """
                        {
                          "messageID": "msg-123",
                          "primaryData": "John Doe"
                        }""";

                given()
                        .headers(headers)
                        .multiPart("payload", payloadJson, "application/json")
                        .multiPart("file", "greeting.txt", "content".getBytes())
                        .post("/api/hello/multipart")
                        .then()
                        .statusCode(HttpStatus.OK.value())
                        .body("statusCode", equalTo("0"))
                        .body("messageCode", equalTo("200"))
                        .body("messageDescription", equalTo("OK!"))
                        .body("messageID", is("msg-123"))
                        .body("errorInfo", emptyIterable())
                        .body("additionalData", emptyIterable())
                        .body("conversationID", not(blankOrNullString()))
                        .body("primaryData", is("""
                                Hello, John Doe! You uploaded "greeting.txt"."""));
            }

            @Test
            void shouldRespondWith400WhenFileMissing() {
                var headers = new Headers(validHeaders());
                // language=JSON
                var payloadJson = """
                        {
                          "messageID": "msg-123",
                          "primaryData": "John Doe"
                        }""";

                given()
                        .headers(headers)
                        .multiPart("payload", payloadJson, "application/json")
                        .post("/api/hello/multipart")
                        .then()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }
        }
    }
}
