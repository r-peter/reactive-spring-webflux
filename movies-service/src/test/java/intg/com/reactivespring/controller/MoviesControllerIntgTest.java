package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"wiremock.reset-mappings-after-each-test=true"})
/*
https://stackoverflow.com/questions/55066307/how-to-make-spring-cloud-contract-reset-wiremock-before-or-after-each-test
*/
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084) //spin up http server on port 8084. Use this to simulate the required behavior.
@TestPropertySource(
        //override the urls configured in application.yml so that they can interact with mock http server
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void retrieveMovieById() {
        //given
        String movieId = "abc";
        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("movieinfo.json")
                        ));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/v1/reviews"))
                        /*
                        Note that urlPathEqualTo does not take query parameter into account.
                        This is a shortcut approach.
                        */
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("reviews.json")
                        ));

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(movie);
                    assertThat(movie.getMovieInfo().getName(), is("Batman Begins"));
                    assertThat(movie.getReviewList().size(), is(2));
                });

    }

    @Test
    void retrieveMovieByIdReturns404ErrorWithMovieInfo() {
        //given
        String movieId = "abc";
        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(404)
                        ));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/v1/reviews"))
                        /*
                        Note that urlPathEqualTo does not take query parameter into account.
                        This is a shortcut approach.
                        */
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("reviews.json")
                        ));

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo Available for the passed in Id : abc");

        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/movieinfos/" + movieId)));
    }

    @Test
    void retrieveMovieByIdReturns404ErrorWithMovieReview() {
        //given
        String movieId = "abc";
        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("movieinfo.json")
                        ));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/v1/reviews"))
                        /*
                        Note that urlPathEqualTo does not take query parameter into account.
                        This is a shortcut approach.
                        */
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(404)
                        ));

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(movie);
                    assertThat(movie.getMovieInfo().getName(), is("Batman Begins"));
                    assertThat(movie.getReviewList().size(), is(0));
                });
    }

    @Test
    void retrieveMovieByIdReturns5XXErrorWithMovieInfo() {
        //given
        String movieId = "abc";
        String serverErrorMessage = "MovieInfo Service Unavailable";
        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(500)
                                        .withBody(serverErrorMessage)
                        ));

        //We are skipping mocking of review service behavior here.

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in Movie Info Service " + serverErrorMessage);

        WireMock.verify(4, WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/movieinfos/" + movieId)));
    }

    @Test
    void retrieveMovieByIdReturns5XXErrorWithReview() {
        String movieId = "abc";
        String serverErrorMessage = "Review Service Unavailable";

        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo("/v1/movieinfos/" + movieId))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("movieinfo.json")
                        ));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/v1/reviews"))
                        /*
                        Note that urlPathEqualTo does not take query parameter into account.
                        This is a shortcut approach.
                        */
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(500)
                                        .withBody(serverErrorMessage)
                        ));

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in Movie Reviews Service " + serverErrorMessage);

        /*
        Note that we are using urlPathMatching to accommodate for query parameters.
        This is a relaxed way of asserting behavior.
         */
        WireMock.verify(4, WireMock.getRequestedFor(WireMock.urlPathMatching("/v1/reviews*")));
    }
}
