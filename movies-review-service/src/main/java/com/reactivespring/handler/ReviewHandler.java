package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactiveRepository reviewReactiveRepository;

    //Sinks.Many<Review> reviewsSink = Sinks.many().replay().all();
    Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                //need flatmap to transform reactive type and return another reactive type
                /* .flatMap(review -> {
                     return reviewReactiveRepository.save(review);
                 })
                 .flatMap(savedReview -> {
                     return ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview);
                 });*/
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(review -> {
                    reviewsSink.tryEmitNext(review);
                })
                .flatMap(ServerResponse.status(HttpStatus.CREATED)
                        ::bodyValue);

    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        log.info("constraintViolations : {}", violations);
        if (violations.size() > 0) {
            String errorMessage = violations.stream().map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining(", "));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        Optional<String> movieInfoId = request.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            Flux<Review> reviewsFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewsResponse(reviewsFlux);
        } else {
            Flux<Review> reviewsFlux = reviewReactiveRepository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }
    }

    private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok()
                .body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        String reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
                /*OPTION 1
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found of the given Review Id " + reviewId)));*/

        return existingReview
                .flatMap(reviewExisting -> request.bodyToMono(Review.class)
                        .map(requestReview -> {
                            reviewExisting.setComment(requestReview.getComment());
                            reviewExisting.setRating(requestReview.getRating());
                            reviewExisting.setMovieInfoId(requestReview.getMovieInfoId());
                            return reviewExisting;
                        })
                        .flatMap(updatedReview -> reviewReactiveRepository.save(updatedReview))
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                )
                /*OPTION 2*/
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview.flatMap(review -> reviewReactiveRepository.deleteById(reviewId)
                .then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(),Review.class)
                .log();
    }
}
