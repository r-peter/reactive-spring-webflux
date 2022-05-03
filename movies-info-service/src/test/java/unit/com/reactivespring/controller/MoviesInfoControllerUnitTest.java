package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoService;

    public static final String MOVIES_INFO_URL = "/v1/movieinfos";

    @Test
    void getAllMovieInfo() {
        List<MovieInfo> movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        Mockito.when(movieInfoService.getAllMovieInfo()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient.get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        String movieInfoId = "abc";

        Mockito.when(movieInfoService.getMovieInfoById(Mockito.eq(movieInfoId)))
                .thenReturn(Mono.just(new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))));

        webTestClient.get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMovieInfo() {
        MovieInfo newMovieInfo = new MovieInfo(null, "Batman V Superman: Dawn of Justice",
                2005, List.of("Ben Affleck", "Henry Cavill"), LocalDate.parse("2016-03-25"));
        String mockMovieInfoId = "mockMovieInfoId";

        Mockito.when(movieInfoService.addMovieInfo(ArgumentMatchers.isA(MovieInfo.class)))
                .thenReturn(Mono.just(new MovieInfo(mockMovieInfoId, "Batman V Superman: Dawn of Justice",
                        2005, List.of("Ben Affleck", "Henry Cavill"), LocalDate.parse("2016-03-25"))));

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
                    Assertions.assertEquals(mockMovieInfoId, movieInfo.getMovieInfoId());
                });
    }

    @Test
    public void updateMovieInfo() {
        String movieInfoId = "abc";
        String additionalCast = "Anne Hathaway";
        MovieInfo movieInfoWithUpdates = new MovieInfo(null, "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy", additionalCast), LocalDate.parse("2012-07-20"));

        Mockito.when(
                        /*This option also works for strict matching
                        movieInfoService.updateMovieInfo(Mockito.eq(movieInfoId), ArgumentMatchers.eq(movieInfoWithUpdates)))
                        */
                        movieInfoService.updateMovieInfo(Mockito.isA(String.class), ArgumentMatchers.isA(MovieInfo.class)))
                .thenReturn(Mono.just(new MovieInfo(movieInfoId, "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy", additionalCast), LocalDate.parse("2012-07-20"))));

        webTestClient.put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
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
    void deleteMovieInfo() {
        String movieInfoId = "abc";

        Mockito.when(movieInfoService.getMovieInfoById(Mockito.eq(movieInfoId)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIES_INFO_URL+"/{id}",movieInfoId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
