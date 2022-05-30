package com.reactivespring.util;

import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RetryUtil {

    public static Retry retrySpec() {
       return Retry.fixedDelay(3, Duration.of(1, ChronoUnit.SECONDS))
                .filter(ex -> ex instanceof MoviesInfoServerException || ex instanceof ReviewsServerException)
                /*  .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                    {
                        //Catch the actual exception, not the retry exhausted error, and then propagate it.
                        return Exceptions.propagate(retrySignal.failure());
                    });*/

                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure())
                );
    }
}
