# KCB Header Validator Starter

## Overview

This repository contains Spring Boot autoconfiguration starters that validate incoming HTTP request headers for both
Spring MVC (Servlet) and Spring WebFlux applications, plus demo applications showing how to use each variant.

The starters integrate at the framework level and expose configuration properties to declare which headers are required
and how they should be validated. They also ship lightweight request/response filters and an exception handler that
returns a standardised error payload when validation fails.

Modules in this repo are separate Maven projects:

- `header-validator-common`: shared, web‑stack agnostic logic
- `header-validator-starter-web`: Servlet/MVC auto‑configuration starter (updated artefact for the web/servlet variant)
- `header-validator-starter-webflux`: WebFlux auto‑configuration starter
- `demo-web`: Spring MVC demo app consuming the Servlet starter
- `demo-webflux`: Spring WebFlux demo app consuming the WebFlux starter

## Stack

- **Language:** Java 21
- **Frameworks:** Spring Boot 3.5.8 (Servlet and WebFlux)
- **Build tool:** Maven, with Maven Wrapper (`mvnw`, `mvnw.cmd`) in root
- **Testing:** JUnit 5, Spring Boot Test, Rest Assured (demo)

## Key features

- Default header rules are provided and can be extended/overridden with configuration properties.
- Pluggable validation via `HeaderValidator` (functional interface). Built-ins include:
    - Regex-based validator for values like `X-MinorServiceVersion` and `X-CallBackURL`
    - Epoch timestamp validator for `X-TimeStamp`
- Error handling: Invalid or missing headers are collected; a 400 Bad Request is returned with a standardised body
  (fields such as `messageCode=4000453`, `statusDescription=Failed`, and one error per offending header).
- Filters:
    - `KCBRequestFilter` creates a request-scoped context with a conversation ID.
    - `KCBResponseFilter` optionally sets `X-ElapsedTime` based on the request `X-TimeStamp` header.

## Requirements

- Java 21 (JDK 21)
- Maven 3.9+ (or use the included Maven Wrapper)

## Getting started

1) Build and install all modules locally (from the repository root):

   Unix/macOS:

    ```shell
    ./mvnw clean install
    ```

   Windows PowerShell:

    ```shell
    ./mvnw.cmd clean install
    ```

   This installs the starters (`header-validator-starter-web` and `header-validator-starter-webflux`) to your
   local Maven repository so the demos can consume them.

2) Add the dependency (choose ONE per application):

   Servlet/MVC application (Tomcat/Jetty/Undertow):

   ```xml
   <dependency>
     <groupId>com.kcbgroup</groupId>
     <artifactId>header-validator-starter-web</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```

   WebFlux application (Reactor Netty):

   ```xml
   <dependency>
     <groupId>com.kcbgroup</groupId>
     <artifactId>header-validator-starter-webflux</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```

   Important: Do not add both starters to the same application. Use the one that matches your web runtime.

3) Run the demo application(s):

   Unix/macOS:

    ```shell
    cd demo-web # OR demo-webflux
    ./mvnw spring-boot:run
    ```

   Windows PowerShell:

    ```shell
    Set-Location demo-web # OR demo-webflux
    ./mvnw.cmd spring-boot:run
    ```

   Then call the demo endpoint (requires headers; see below):

    ```http request
    GET http://localhost:8080/api/hello
    ```

   Sample call file: [request.http](request.http).

## Configuration

Both starters expose the following configuration properties (Spring Boot relaxed binding applies):

- `kcb.api.headers.validation.headers` — a list of header rules. Each rule supports:
    - `header-name` (string, required): The HTTP header name.
    - `required` (boolean, default true): Whether the header must be present and non-empty.
    - `validator` (string, optional): A `HeaderValidator` implementation. The value can be:
        - a fully qualified class name (FQCN) with a public no-arg constructor, or
        - a Spring bean name of a `HeaderValidator`.
          The lookup order is controlled by `kcb.api.headers.validator.source` (see below).

- `kcb.api.headers.validator.source` — optional, enum controlling how validator strings are resolved:
    - `FQCN` — use FQCN only
    - `BeanDefinition` — use Spring bean name only
    - `FQCNB4BeanDefinition` — try FQCN, then fall back to bean name (default)
    - `BeanDefinitionB4FQCN` — try bean name, then fall back to FQCN

### Default headers

If you don’t configure anything, these headers are validated by default (can be extended/overridden):

- X-FeatureCode (optional)
- X-FeatureName (required)
- X-ServiceCode (required)
- X-ServiceName (required)
- X-ServiceSubCategory (optional)
- X-MinorServiceVersion (required) — regex `v?\d+(\.\d+){0,2}` (case-insensitive)
- X-ChannelCategory (required)
- X-ChannelCode (required)
- X-ChannelName (required)
- X-RouteCode (optional)
- X-TimeStamp (optional) — epoch seconds validated by `EpochTimestampValidator`
- X-ServiceMode (optional)
- X-SubscriberEvents (optional)
- X-CallBackURL (optional) — must match HTTP(S) URL (simple regex)

Example: adding/overriding headers (demo-web; for WebFlux, bean/package names will differ slightly)

[demo-web/src/main/resources/application.properties](demo-web/src/main/resources/application.properties) shows how to:

- Add new headers (required and optional)
- Provide a custom validator by FQCN
- Override a default header with a validator provided via Spring bean name

### Snippet

#### YAML

```yaml
kcb:
  api:
    headers:
      validator:
        source: beandefinitionb4fqcn
      validation:
        headers:
          - header-name: X-Additional-Required
          - header-name: X-Additional-Optional
            required: false
          - header-name: X-Additional-Custom-Validator
            required: false
            validator: CustomValidator # Spring bean name
          - header-name: X-Timestamp # Override the default X-TimeStamp header to accept ISO-8601 instead
            required: false
            validator: com.kcbgroup.demoweb.validators.ISO8601TimestampHeaderValidator
```

#### Properties

```properties
kcb.api.headers.validator.source=beandefinitionb4fqcn
kcb.api.headers.validation.headers.[0].header-name=X-Additional-Required
kcb.api.headers.validation.headers.[1].header-name=X-Additional-Optional
kcb.api.headers.validation.headers.[1].required=false
kcb.api.headers.validation.headers.[2].header-name=X-Additional-Custom-Validator
kcb.api.headers.validation.headers.[2].required=false
# Spring bean name
kcb.api.headers.validation.headers.[2].validator=CustomValidator
# Override the default X-TimeStamp header to accept ISO-8601 instead
kcb.api.headers.validation.headers.[3].header-name=X-Timestamp
kcb.api.headers.validation.headers.[3].required=false
kcb.api.headers.validation.headers.[3].validator=com.kcbgroup.demoweb.validators.ISO8601TimestampHeaderValidator
```

#### Environment variables

All properties can be supplied via environment variables using Spring Boot’s relaxed binding rules. Examples:

- `kcb.api.headers.validator.source` → `KCB_API_HEADERS_VALIDATION_VALIDATOR_SOURCE`
- `kcb.api.headers.validation.headers.[0].header-name` → `KCB_API_HEADERS_VALIDATION_HEADERS_0__HEADER_NAME`

Note the conversion rules: dots to underscores, list indexes like `[0]` to `_0_` (often written as `0__` to separate
levels), and kebab-case keys to upper snake case.

### Custom validators

Implement the [
`HeaderValidator`](header-validator-common/src/main/java/com/kcbgroup/common/headers/validators/HeaderValidator.java)
interface.

You can provide validators by:

- **FQCN:** set `validator=com.example.MyValidator` (class must have a public no-arg constructor), or
- **Bean name:** declare `@Component class MyValidator implements HeaderValidator` and set `validator=MyValidator`.

## Payload conversion and error response customisation

The starters expose a simple extension point via the [
`PayloadConverter`](header-validator-common/src/main/java/com/kcbgroup/common/utils/converter/PayloadConverter.java)
interface to help you:

- Customise the body returned when header validation fails.
- Convert the deserialized request-body into a "request payload" type that can carry a `messageID`.

### Overriding the default behavior

To override, define your own Spring bean of type `PayloadConverter` in your application. Because the default bean is
created with `@ConditionalOnMissingBean`, your bean will take precedence automatically.

Minimal example (works for both MVC and WebFlux apps):

```java
import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.utils.converter.PayloadConverter;
import com.kcbgroup.common.utils.dto.Request;
import com.kcbgroup.common.utils.dto.ResponsePayload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
class CustomPayloadConverterConfig {
    @Bean
    PayloadConverter payloadConverter() {
        return new PayloadConverter() {
            @Override
            public @NonNull Object convertToRequestPayload(@NonNull Object payload) {
                // Optionally map your DTO to Request so the starters can extract messageID.
                // If payload is already a Request, just return it.
                return payload; // replace with mapping logic if needed
            }

            @Override
            public @NonNull Object convertToHeaderValidationErrorResponse(
                    @NonNull ResponsePayload<?> defaultBody,
                    @NonNull HeadersValidationException exception) {
                // Optionally transform the default error payload into a custom structure.
                return defaultBody; // or return your own DTO/map
            }
        };
    }
}
```

## Default error response

On validation failure header validation throws `HeadersValidationException`. The starter’s error handlers convert it
to a 400 response similar to:

```json
{
  "conversationID": "...",
  "messageID": "...",
  "messageCode": "4000453",
  "messageDescription": "Invalid or missing request headers",
  "statusCode": "0",
  "statusDescription": "Failed",
  "additionalData": [],
  "errorInfo": [
    {
      "errorCode": "<header-name>",
      "errorDescription": "<descriptive-error-message>"
    }
  ]
}
```
