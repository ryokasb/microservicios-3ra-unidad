package com.example.user.webclient;

import com.example.user.model.Dto.TokenRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;

@Component
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(@Value("${producto-service.url}") String productServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    public String eliminarProductosPorUserId(Long idusuario, String token) {

        TokenRequest body = new TokenRequest(token);

        return webClient
                .method(HttpMethod.DELETE)                      
                .uri("products/user/{idusuario}", idusuario)
                .body(BodyInserters.fromValue(body))           
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(msg -> new RuntimeException("Error 4xx al eliminar productos: " + msg))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(msg -> new RuntimeException("Error 5xx del servidor de productos: " + msg))
                )
                .bodyToMono(String.class)
                .block();
    }
}
