package com.bryan.apiplayground.common;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.bryan.apiplayground.common.exception.PremiumFeatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Validation error");
        return badRequest(message, req);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleParamValidation(HandlerMethodValidationException ex, HttpServletRequest req) {
        return badRequest("Invalid request parameters: " + ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return badRequest(ex.getMessage(), req);
    }

    @ExceptionHandler(ApiKeyNotConfiguredException.class)
    public ResponseEntity<ApiError> handleMissingKey(ApiKeyNotConfiguredException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage(), req.getRequestURI()));
    }

    // 402 Payment Required distinguishes "your tier doesn't cover this endpoint"
    // from a real upstream failure. The body carries requiredPlan + feature so
    // the frontend can render the right paywall without parsing the message.
    @ExceptionHandler(PremiumFeatureException.class)
    public ResponseEntity<PremiumRequiredError> handlePremium(PremiumFeatureException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(PremiumRequiredError.of(
                        HttpStatus.PAYMENT_REQUIRED.value(),
                        ex.getMessage(),
                        req.getRequestURI(),
                        ex.getRequiredPlan(),
                        ex.getFeature()
                ));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiError> handleExternal(ExternalApiException ex, HttpServletRequest req) {
        // upstreamStatus == 0 → timeout/connection failure → 504; otherwise upstream returned an error → 502.
        var status = ex.getUpstreamStatus() == 0 ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Unexpected error: " + ex.getMessage(),
                        req.getRequestURI()));
    }

    private ResponseEntity<ApiError> badRequest(String message, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST.value(), message, req.getRequestURI()));
    }
}
