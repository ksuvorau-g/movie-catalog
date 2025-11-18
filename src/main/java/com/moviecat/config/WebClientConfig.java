package com.moviecat.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for external API calls.
 * Configures TMDB API client with timeout settings and error handling.
 */
@Configuration
@Slf4j
public class WebClientConfig {
    
    @Value("${tmdb.api.base-url}")
    private String tmdbBaseUrl;
    
    @Value("${tmdb.access-token}")
    private String tmdbAccessToken;
    
    /**
     * Create WebClient bean for TMDB API calls.
     * Configured with:
     * - Base URL for TMDB API
     * - Bearer token authentication (API Read Access Token)
     * - Connection timeout (5 seconds)
     * - Read timeout (10 seconds)
     * - Write timeout (10 seconds)
     * - SSL verification disabled (for development/testing)
     * - Request/response logging
     * - Error handling
     * 
     * @return configured WebClient instance
     */
    @Bean
    public WebClient tmdbWebClient() {
        try {
            // Create SSL context that trusts all certificates (DISABLE SSL VERIFICATION)
            var sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            
            // Configure HTTP client with timeouts and disabled SSL verification
            HttpClient httpClient = HttpClient.create()
                    .secure(sslSpec -> sslSpec.sslContext(sslContext))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .responseTimeout(Duration.ofSeconds(10))
                    .doOnConnected(conn -> 
                        conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                    );
            
            return WebClient.builder()
                    .baseUrl(tmdbBaseUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .defaultHeader("Accept", "application/json")
                    .defaultHeader("Authorization", "Bearer " + tmdbAccessToken)
                    .filter(logRequest())
                    .filter(logResponse())
                    .build();
        } catch (SSLException e) {
            log.error("Failed to configure SSL context for WebClient", e);
            throw new RuntimeException("Failed to initialize TMDB WebClient", e);
        }
    }
    
    /**
     * Log request details for debugging.
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("TMDB API Request: {} {}", 
                    clientRequest.method(), 
                    clientRequest.url());
            }
            return Mono.just(clientRequest);
        });
    }
    
    /**
     * Log response details and handle errors.
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("TMDB API Response: {} {}", 
                    clientResponse.statusCode(),
                    clientResponse.headers().asHttpHeaders());
            }
            
            if (clientResponse.statusCode().isError()) {
                log.error("TMDB API Error: {}", clientResponse.statusCode());
            }
            
            return Mono.just(clientResponse);
        });
    }
}
