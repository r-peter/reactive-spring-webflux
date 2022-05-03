package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    public static String MOVIES_INFO_URL = "/v1/movieinfos";

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        movieInfoRepository.saveAll(movieinfos).blockLast();
    }

    @Test
    void addMovieInfo() {
        MovieInfo newMovieInfo = new MovieInfo(null, "Batman V Superman: Dawn of Justice",
                2005, List.of("Ben Affleck", "Henry Cavill"), LocalDate.parse("2016-03-25"));

        webTestClient.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(newMovieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(movieInfo);
                    Assertions.assertNotNull(movieInfo.getMovieInfoId());
                });
    }

    @Test
    public void getAllMovieInfo() {
        webTestClient.get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    public void getMovieInfoById() {
        String movieInfoId="abc";
        webTestClient.get()
                .uri(MOVIES_INFO_URL+"/{id}",movieInfoId)
                .exchange()
                .expectStatus().is2xxSuccessful()
              /*  .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                });*/
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    public void getMovieInfoByIdNotFound() {
        String movieInfoId="abcdef";
        webTestClient.get()
                .uri(MOVIES_INFO_URL+"/{id}",movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void getAllMovieByYear() {
        URI uri =UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                        .queryParam("year", 2005)
                                .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    public void updateMovieInfo(){
        String movieInfoId="abc";
        String additionalCast = "Anne Hathaway";
        MovieInfo movieInfoWithUpdates=new MovieInfo(null, "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy", additionalCast), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .bodyValue(movieInfoWithUpdates)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(updatedMovieInfo);
                    Assertions.assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertThat(updatedMovieInfo.getCast(), hasItem(additionalCast));
                });
    }

    @Test
    public void updateMovieInfoNotFound(){
        String movieInfoId="abcdef";
        String additionalCast = "Anne Hathaway";
        MovieInfo movieInfoWithUpdates=new MovieInfo(null, "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy", additionalCast), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .bodyValue(movieInfoWithUpdates)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void deleteMovieInfoById() {
        String movieInfoId="abc";
        webTestClient.delete()
                .uri(MOVIES_INFO_URL+"/{id}",movieInfoId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }
}