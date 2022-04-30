package com.reactivespring.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void flux() {
        webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Integer.class)
                .hasSize(3);
    }

    @Test
    void flux_body_approach1() {
        Flux<Integer> body = webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(body)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void flux_body_approach2() {
        webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                    List<Integer> responseBody = listEntityExchangeResult.getResponseBody();
                    assert (responseBody.size() == 3);
                });
    }

    @Test
    void mono() {
        webTestClient
                .get()
                .uri("/mono")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals("hello-world", responseBody);
                });
    }

    @Test
    void flux_body_stream() {
        Flux<Long> body = webTestClient
                .get()
                .uri("/stream")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(body)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();
    }
}