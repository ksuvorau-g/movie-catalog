package com.moviecat.exception;

/**
 * Exception thrown when an external API call fails.
 */
public class ExternalApiException extends RuntimeException {
    
    private final String apiName;
    private final Integer statusCode;
    
    public ExternalApiException(String apiName, String message) {
        super(String.format("%s API error: %s", apiName, message));
        this.apiName = apiName;
        this.statusCode = null;
    }
    
    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(String.format("%s API error: %s", apiName, message), cause);
        this.apiName = apiName;
        this.statusCode = null;
    }
    
    public ExternalApiException(String apiName, Integer statusCode, String message) {
        super(String.format("%s API error (HTTP %d): %s", apiName, statusCode, message));
        this.apiName = apiName;
        this.statusCode = statusCode;
    }
    
    public String getApiName() {
        return apiName;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
}
