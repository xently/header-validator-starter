# Header Validator Starter Presentation

---

## The Challenge: Boilerplate Header Validation

*   **Repetitive Logic:** Manually checking for `X-FeatureCode`, `X-ServiceCode`, etc., in every controller or filter.
*   **Inconsistency:** Different services might handle missing or invalid headers differently.
*   **Maintenance Burden:** Hard to update validation rules across multiple microservices.
*   **Error Handling:** Inconsistent error responses for header validation failures.

---

## The Solution: Header Validator Starter

*   **Spring Boot Starters:** Automated configuration for both **Servlet (MVC)** and **WebFlux**.
*   **Declarative Validation:** Define your header requirements in `application.properties` or `application.yml`.
*   **Pluggable Architecture:** Use built-in validators or plug in your own logic.
*   **Standardized Errors:** Returns a consistent `400 Bad Request` with detailed error information.

---

## Key Features

*   **Auto-Configuration:** Just add the dependency, and it works.
*   **Framework Integration:** Works at the filter/web-filter level, catching errors before they hit your business logic.
*   **Flexible Lookup:** Resolve validators by Fully Qualified Class Name (FQCN) or as Spring Beans.
*   **Stack Agnostic Core:** Shared logic in `header-validator-common`.

---

## Built-in Validators & Defaults

The library comes with sensible defaults for our ecosystem:
*   **Regex Validator:** Used for `X-MinorServiceVersion` (e.g., `v1.0.0`) and `X-CallBackURL`.
*   **Epoch Timestamp:** Specialized validator for `X-TimeStamp`.
*   **Required vs. Optional:** Most headers are required by default but can be made optional via config.
*   **Default Headers:** Automatically validates common headers like `X-ServiceCode`, `X-ChannelName`, etc.

---

## Getting Started: Add Dependency

### For Spring MVC (Servlet):
```xml
<dependency>
  <groupId>ke.co.xently</groupId>
  <artifactId>header-validator-starter-web</artifactId>
  <version>4.3.0</version>
</dependency>
```

### For Spring WebFlux:
```xml
<dependency>
  <groupId>ke.co.xently</groupId>
  <artifactId>header-validator-starter-webflux</artifactId>
  <version>4.3.0</version>
</dependency>
```

---

## Configuration Example (YAML)

```yaml
settings:
  headers:
    validation:
      headers:
        - header-name: X-Custom-Required
        - header-name: X-Custom-Optional
          required: false
        - header-name: X-Validated-Header
          validator: com.example.MyCustomValidator
```

---

## Custom Validators

Implementing a custom validator is easy:

1.  **Implement `HeaderValidator`:**
    ```java
    @Component
    public class MyValidator implements HeaderValidator {
        @Override
        public void validate(String value) throws HeaderValidationException {
            if (!isValid(value)) {
                throw new HeaderValidationException("Invalid value");
            }
        }
    }
    ```
2.  **Reference it in config:**
    `settings.headers.validation.headers[0].validator=MyValidator`

---

## Standardized Error Response

When validation fails, the client receives a clear message:

```json
{
  "messageCode": "4000453",
  "messageDescription": "Invalid or missing request headers",
  "errorInfo": [
    {
      "errorCode": "X-ServiceCode",
      "errorDescription": "is required"
    },
    {
      "errorCode": "X-MinorServiceVersion",
      "errorDescription": "does not match pattern v?\\d+(.\\d+){0,2}"
    }
  ]
}
```

---

## Summary

*   **Consistency** across all services.
*   **Developer Productivity:** Focus on business logic, not boilerplate.
*   **Safety:** Ensure all requests meet the required contract.
*   **Flexibility:** Easy to override and extend.

---

## Q&A

**Thank you!**
Check out the `demo-web` and `demo-webflux` modules for live examples.
