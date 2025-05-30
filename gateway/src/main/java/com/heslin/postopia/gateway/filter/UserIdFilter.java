package com.heslin.postopia.gateway.filter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.UserId;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@Component
@Order(-10086)
public class UserIdFilter implements GlobalFilter {
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(UserIdFilter.class);

    @Autowired
    public UserIdFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest originalRequest = exchange.getRequest();
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        // Process request
        ServerHttpRequest decoratedRequest = decorateRequest(originalRequest, bufferFactory);

        // Process response
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (!Objects.requireNonNull(getStatusCode()).is2xxSuccessful()) {
                    return super.writeWith(body);
                }

                if (!(body instanceof Flux)) {
                    return super.writeWith(body);
                }

                Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;
                return super.writeWith(fluxBody
                .collectList()
                .map(dataBuffers -> {
                    DataBuffer joinedBuffer = bufferFactory.join(dataBuffers);
                    try (InputStream inputStream = joinedBuffer.asInputStream();
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                        JsonParser parser = objectMapper.getFactory().createParser(inputStream);
                        JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);

                        while (parser.nextToken() != null) {
                            if (parser.currentToken() == JsonToken.FIELD_NAME && "userId".equals(parser.currentName())) {
                                generator.writeFieldName("userId");
                                parser.nextToken();
                                String userId = parser.getValueAsString();
                                generator.writeNumber(UserId.masked(userId));
                            } else {
                                generator.copyCurrentEvent(parser);
                            }
                        }

                        generator.flush();
                        return bufferFactory.wrap(outputStream.toByteArray());
                    } catch (IOException e) {
                        log.error("Error processing JSON response", e);
                        return joinedBuffer;
                    }
                })
                .flatMapMany(Flux::just));
            }
        };

        System.out.println("mutated");
        return chain.filter(exchange.mutate()
        .request(decoratedRequest)
        .response(decoratedResponse)
        .build());
    }

    private ServerHttpRequest decorateRequest(ServerHttpRequest originalRequest, DataBufferFactory bufferFactory) {
        return new ServerHttpRequestDecorator(originalRequest) {
            private URI cachedUri = null;

            @Override
            public URI getURI() {
                if (cachedUri != null) {
                    return cachedUri;
                }

                MultiValueMap<String, String> queryParams = originalRequest.getQueryParams();
                if (queryParams.containsKey("userId")) {
                    // Get the base URI without query parameters
                    UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(super.getURI().toString())
                    .replaceQuery(null);

                    // Add all query parameters back, replacing just the userId
                    queryParams.forEach((key, values) -> {
                        if ("userId".equals(key)) {
                            List<String> maskedUserIds = values.stream()
                            .map(UserId::masked)
                            .toList();
                            builder.queryParam(key, maskedUserIds.toArray());
                        } else {
                            builder.queryParam(key, values.toArray());
                        }
                    });

                    cachedUri = builder.build(false).toUri(); // false means don't encode twice
                    return cachedUri;
                }

                cachedUri = super.getURI();
                return cachedUri;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                String contentType = getHeaders().getFirst("Content-Type");
                if (contentType != null && contentType.contains("application/json")) {
                    return super.getBody()
                    .collectList()
                    .filter(list -> !list.isEmpty())
                    .map(bufferFactory::join)
                    .flatMapMany(buffer -> {
                        try (InputStream inputStream = buffer.asInputStream();
                             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                            JsonParser parser = objectMapper.getFactory().createParser(inputStream);
                            JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
                            while (parser.nextToken() != null) {
                                if (parser.currentToken() == JsonToken.FIELD_NAME && "userId".equals(parser.currentName())) {
                                    generator.writeFieldName("userId");
                                    parser.nextToken();
                                    String userId = parser.getValueAsString();
                                    generator.writeNumber(UserId.masked(userId));
                                } else {
                                    generator.copyCurrentEvent(parser);
                                }
                            }

                            generator.flush();
                            return Flux.just(bufferFactory.wrap(outputStream.toByteArray()));
                        } catch (IOException e) {
                            log.error("Error processing JSON request", e);
                            return Flux.just(buffer);
                        }
                    })
                    .switchIfEmpty(Flux.empty());
                }

                return super.getBody();
            }
        };
    }


}