package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    private WebClient webClient;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieInfoId) {
        String url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieInfoId)
                .buildAndExpand().toUriString();
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        //No need to throw errors. There may not be any reviews available.
                        return Mono.empty();
                    }
                    //get response error message and create exception with that message
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage ->  Mono.error(new ReviewsClientException(responseMessage)));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    //get response error message and create exception with that message
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage ->  Mono.error(new ReviewsServerException("Server Exception in Movie Reviews Service "+ responseMessage)));
                })
                .bodyToFlux(Review.class);
    }
}
