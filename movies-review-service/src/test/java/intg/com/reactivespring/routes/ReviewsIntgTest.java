package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    private static final String MOVIES_REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        List<Review> reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review("1", 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }

    @Test
    void addReview() {
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

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
        webTestClient.delete()
                .uri(MOVIES_REVIEWS_URL + "/{id}", reviewId)
                .exchange()
                .expectStatus().isNoContent();
    }


    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }
}
