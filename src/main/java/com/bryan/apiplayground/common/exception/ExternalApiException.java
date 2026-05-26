package com.bryan.apiplayground.common.exception;

public class ExternalApiException extends RuntimeException {

    private final int upstreamStatus;

    public ExternalApiException(String message, int upstreamStatus) {
        super(message);
        this.upstreamStatus = upstreamStatus;
    }

    public ExternalApiException(String message, int upstreamStatus, Throwable cause) {
        super(message, cause);
        this.upstreamStatus = upstreamStatus;
    }

    public int getUpstreamStatus() {
        return upstreamStatus;
    }
}
