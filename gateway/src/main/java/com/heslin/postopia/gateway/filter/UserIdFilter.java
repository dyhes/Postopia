package com.heslin.postopia.gateway.filter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.UserId;
import org.reactivestreams.Publisher;
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
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@Order(-2)
public class UserIdFilter implements GlobalFilter {
    private final ObjectMapper objectMapper;

    @Autowired
    public UserIdFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest originalRequest = exchange.getRequest();
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        Function<DataBuffer, DataBuffer> processJsonStream = dataBuffer -> {
            try (InputStream inputStream = dataBuffer.asInputStream();
                 JsonParser parser = objectMapper.getFactory().createParser(inputStream);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);

                System.out.println("stream json");
                // 流式处理逻辑
                while (parser.nextToken() != null) {
                    if (parser.currentToken() == JsonToken.FIELD_NAME && "userId".equals(parser.currentName())) {
                        System.out.println("found userId");
                        parser.nextToken();
                        long userId = parser.getLongValue();
                        System.out.println(userId);
                        System.out.println("masked: " + UserId.masked(userId));
                        generator.writeNumberField("userId", UserId.masked(userId));
                    } else {
                        generator.copyCurrentEvent(parser);
                    }
                }

                generator.flush();
                return bufferFactory.wrap(outputStream.toByteArray());
            } catch (IOException e) {
                throw new UncheckedIOException("JSON流处理异常", e);
            }
        };

        ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(originalRequest) {
            @Override
            public Flux<DataBuffer> getBody() {
                String contentType = getHeaders().getFirst("Content-Type");
                System.out.println("request");
                System.out.println("request with content type: " + contentType);
                if (contentType != null && contentType.contains("application/json")) {
                    return super.getBody()
                    .collectList()
                    .filter(list -> !list.isEmpty())  // 过滤空列表
                    .map(bufferFactory::join)
                    .flatMapMany(buffer -> Flux.just(processJsonStream.apply(buffer)))
                    .switchIfEmpty(Flux.empty());
                } else {
                    // 处理非 JSON 请求体
                    return super.getBody();
                }
            }

            @Override
            public URI getURI() {
                MultiValueMap<String, String> queryParams = originalRequest.getQueryParams();

                // 处理加密参数（例如 encryptedUserId）
                if (queryParams.containsKey("userId")) {
                    List<String> userIds = queryParams.get("userId");
                    System.out.println("mask userId queryParam");
                    userIds.forEach(u -> System.out.println("userId: " + u));
                    List<String> maskedUserIds = userIds.stream().map(UserId::masked).toList();
                    URI uri = UriComponentsBuilder.fromUri(super.getURI())
                    .replaceQueryParam("userId", maskedUserIds)
                    .build().toUri();
                    System.out.println("query");
                    System.out.println(uri.getQuery());
                    return uri;
                }
                return super.getURI();
            }



            @Override
            public MultiValueMap<String, String> getQueryParams() {
                System.out.println("getqueryparams");
                MultiValueMap<String, String> queryParams = originalRequest.getQueryParams();

                // 处理加密参数（例如 encryptedUserId）
                if (queryParams.containsKey("userId")) {
                    System.out.println("before");
                    System.out.println(queryParams);
                    List<String> userIds = queryParams.remove("userId");
                    System.out.println("mask userId queryParam");
                    userIds.forEach(u -> System.out.println("userId: " + u));
                    List<String> maskedUserIds = userIds.stream().map(UserId::masked).toList();
                    queryParams.put("userId", maskedUserIds);
                    System.out.println("after");
                    System.out.println(queryParams);
                }
                return queryParams;
            }
        };


        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (!Objects.requireNonNull(originalResponse.getStatusCode()).is2xxSuccessful()) {
                    return super.writeWith(body);
                }
                if (body instanceof Flux) {
                    return super.writeWith(processBody((Flux<DataBuffer>) body));
                }
                System.out.println("not flux");
                return super.writeWith(body);
            }

            private Flux<DataBuffer> processBody(Flux<DataBuffer> body) {
                System.out.println("response");
                return body
                .collectList()
                .map(bufferFactory::join)
                .flatMapMany(buffer -> Flux.just(processJsonStream.apply(buffer)));
            }

        };

        return chain.filter(exchange.mutate().request(decoratedRequest).response(decoratedResponse).build());
    }
}

//
//@Component
//@Order(-2)
//public class UserIdFilter implements GlobalFilter {
//    private final ObjectMapper mapper;
//
//    @Autowired
//    public UserIdFilter(ObjectMapper mapper) {
//        this.mapper = mapper;
//    }
//
//    ObjectNode maskUserId(ObjectNode dataNode) {
//        if (dataNode.has("userId")) {
//            dataNode.put("userId", UserId.masked(dataNode.get("userId").asLong()));
//        }
//        return dataNode;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        ServerHttpResponse originalResponse = exchange.getResponse();
//        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
//
//
//        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
//            @Override
//            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
//                if (body instanceof Flux) {
//                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
//                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
//                        // 合并数据流并转换为 JSON 字符串
//                        String responseBody = dataBuffers.stream()
//                        .map(buffer -> buffer.toString(StandardCharsets.UTF_8))
//                        .collect(Collectors.joining());
//
//                        // 加密 userId
//                        try {
//                            ObjectNode responseNode = (ObjectNode) mapper.readTree(responseBody);
//                            JsonNode dataNode = responseNode.get("data");
//                            if (dataNode.isArray()) {
//                                ArrayNode arrayNode = mapper.createArrayNode();
//                                dataNode.forEach(child -> arrayNode.add(maskUserId((ObjectNode) child)));
//                                responseNode.set("data", arrayNode);
//                            } else {
//                                ObjectNode objectDataNode = (ObjectNode) dataNode;
//                                if (objectDataNode.has("currentPage")) {
//                                    ArrayNode subDataNode = (ArrayNode) dataNode.get("data");
//                                    ArrayNode newSubDataNode = mapper.createArrayNode();
//                                    subDataNode.forEach(child -> newSubDataNode.add(maskUserId((ObjectNode) child)));
//                                    objectDataNode.set("data", newSubDataNode);
//                                    responseNode.set("data", objectDataNode);
//                                } else {
//                                    responseNode.set("data", maskUserId(objectDataNode));
//                                }
//                            }
//                            return bufferFactory.wrap(mapper.writeValueAsBytes(responseNode));
//                        } catch (JsonProcessingException e) {
//                            throw new RuntimeException("JSON 处理异常", e);
//                        }
//                    }));
//                }
//                return super.writeWith(body);
//            }
//        };
//        return chain.filter(exchange.mutate().response(decoratedResponse).build());
//    }
//}
