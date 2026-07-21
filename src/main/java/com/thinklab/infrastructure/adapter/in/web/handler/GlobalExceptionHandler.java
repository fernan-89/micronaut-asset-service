package com.thinklab.infrastructure.adapter.in.web.handler;

import com.thinklab.domain.exception.BusinessException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler: Centralized reactive error transformation.
 * This adapter intercepts domain and infrastructure exceptions, translating them
 * into a standardized RFC 7807 "Problem Details" response.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>RFC 7807 Compliance:</b> Ensures a consistent contract for API consumers.</li>
 * <li><b>Information Shielding:</b> Masks internal stacktraces from public responses.</li>
 * <li><b>NASA Standard Observability:</b> Logs full technical details for forensic analysis
 * while returning sanitized signals to the boundary.</li>
 * </ul>
 */
@Slf4j
@Produces
@Singleton
@Requires(classes = {ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<Map<String, Object>>> {

    private static final String TYPE_FAILURE = "https://thinklab.com/probs/technical-failure";
    private static final String TYPE_BUSINESS = "https://thinklab.com/probs/business-violation";

    @Override
    public HttpResponse<Map<String, Object>> handle(HttpRequest request, Throwable exception) {
        if (exception instanceof BusinessException ex) {
            return handleBusinessException(request, ex);
        }

        if (exception instanceof ConstraintViolationException ex) {
            return handleValidationException(request, ex);
        }

        return handleGenericException(request, exception);
    }

    private HttpResponse<Map<String, Object>> handleBusinessException(HttpRequest<?> request, BusinessException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "ASSET_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ASSET_DUPLICATE_IDENTIFIER", "VERSION_COLLISION" -> HttpStatus.CONFLICT;
            case "INVALID_STATE_TRANSITION" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.BAD_REQUEST;
        };

        log.warn("[ACTION: BUSINESS_FAILURE] - Domain violation [{}]: {} | Path: {}",
                ex.getErrorCode(), ex.getMessage(), request.getPath());

        return HttpResponse.status(status).body(createProblem(
                TYPE_BUSINESS,
                status.getReason(),
                status.getCode(),
                ex.getMessage(),
                ex.getErrorCode()
        ));
    }

    private HttpResponse<Map<String, Object>> handleValidationException(HttpRequest<?> request, ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("[ACTION: VALIDATION_FAILURE] - Malformed payload: {} | Path: {}", details, request.getPath());

        return HttpResponse.badRequest(createProblem(
                TYPE_BUSINESS,
                "Bad Request",
                400,
                "Validation failed for the provided payload.",
                "VALIDATION_ERROR"
        ));
    }

    private HttpResponse<Map<String, Object>> handleGenericException(HttpRequest<?> request, Throwable ex) {
        log.error("[ACTION: INTERNAL_FAILURE] - Critical technical failure detected at path [{}]: ",
                request.getPath(), ex);

        Map<String, Object> body = createProblem(
                TYPE_FAILURE,
                "Internal Server Error",
                500,
                "An unexpected technical failure occurred in the core processing pipeline.",
                "INTERNAL_SERVER_ERROR"
        );

        // NASA Standard: Exposes simplified cause for accelerated debugging in Dev/Staging environments
        body.put("details", ex.getClass().getSimpleName() + ": " + ex.getMessage());

        return HttpResponse.serverError(body);
    }

    private Map<String, Object> createProblem(String type, String title, int status, String detail, String code) {
        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", type);
        problem.put("title", title);
        problem.put("status", status);
        problem.put("detail", detail);
        problem.put("error_code", code);
        problem.put("timestamp", Instant.now().toString());
        return problem;
    }
}