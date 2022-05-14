package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest//No controllers
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class}) //Beans that need to be injected
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    private static final String MOVIES_REVIEWS_URL = "/v1/reviews";

    @Test
    void addReview() {
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).
                thenReturn(
                        Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0))
                );

        webTestClient.post()
                .uri(MOVIES_REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    Review savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(savedReview);
                    Assertions.assertNotNull(savedReview.getReviewId());
                });
    }

    @Test
    public void getAllMovieReview() {
        List<Review> reviews = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review("def", 1L, "Awesome Movie1", 9.0),
                new Review("1", 2L, "Excellent Movie", 8.0));

        when(reviewReactiveRepository.findAll()).
                thenReturn(
                        Flux.fromIterable(reviews)
                );

        webTestClient.get()
                .uri(MOVIES_REVIEWS_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    public void getReviewsByMovieInfoId() {
        Long movieInfoId = 1L;

        when(reviewReactiveRepository.findReviewsByMovieInfoId(Mockito.eq(movieInfoId))).
                thenReturn(
                        Flux.just(new Review("abc", movieInfoId, "Awesome Movie", 9.0),
                                new Review("def", movieInfoId, "Awesome Movie1", 9.0)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(MOVIES_REVIEWS_URL)
                        .queryParam("movieInfoId", movieInfoId)
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    public void updateMovieReview() {
        String reviewId = "1";
        String updatedComment = "Excellent Movie Update";
        Double updatedRating = 8.5;

        Review reviewWithUpdates = new Review(reviewId, 1L, updatedComment, updatedRating);

        when(reviewReactiveRepository.findById(Mockito.eq(reviewId)))
                .thenReturn(
                        Mono.just(new Review(reviewId, 2L, "Excellent Movie", 8.0)));

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(
                        Mono.just(reviewWithUpdates)
                );

        webTestClient.put()
                .uri(MOVIES_REVIEWS_URL + "/{id}", reviewId)
                .bodyValue(reviewWithUpdates)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review updatedReview = reviewEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(updatedReview);
                    assertThat(updatedReview.getComment(), is(updatedComment));
                    assertThat(updatedReview.getRating(), is(updatedRating));
                });
    }

    @Test
    public void deleteReviewById() {
        String reviewId = "1";

        when(reviewReactiveRepository.findById(Mockito.eq(reviewId)))
                .thenReturn(
                        Mono.just(new Review(reviewId, 2L, "Excellent Movie", 8.0)));

        when(reviewReactiveRepository.deleteById(Mockito.eq(reviewId))).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIES_REVIEWS_URL + "/{id}", reviewId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
