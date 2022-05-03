package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    public void findAll() {
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll();

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void findById() {
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc");

        StepVerifier.create(movieInfoMono)
                //.expectNextCount(1)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    public void saveMovieInfo() {
        MovieInfo newMovieInfo = new MovieInfo(null, "Batman V Superman: Dawn of Justice",
                2005, List.of("Ben Affleck", "Henry Cavill"), LocalDate.parse("2016-03-25"));

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(newMovieInfo);

        StepVerifier.create(movieInfoMono)
                //.expectNextCount(1)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals("Batman V Superman: Dawn of Justice", movieInfo.getName());
                    Assertions.assertNotNull(movieInfo.getMovieInfoId());
                })
                .verifyComplete();
    }

    @Test
    public void updateMovieInfo() {
        String cast = "Anne Hathaway";
        MovieInfo movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.getCast().add(cast);

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo);

        StepVerifier.create(movieInfoMono)
                .assertNext(movie -> {
                    assertThat(movie.getCast(), hasItem(cast));
                })
                .verifyComplete();
    }

    @Test
    public void deleteMovieInfo() {
        movieInfoRepository.deleteById("abc").block();

        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll();

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void findByYear() {
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findByYear(2005);

        StepVerifier.create(movieInfoFlux)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals("Batman Begins", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    public void findByName() {
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findByName("Batman Begins");

        StepVerifier.create(movieInfoMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }
}