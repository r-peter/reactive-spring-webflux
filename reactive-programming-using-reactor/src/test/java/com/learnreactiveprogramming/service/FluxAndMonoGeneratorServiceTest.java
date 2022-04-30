package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {

        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux();

        StepVerifier.create(namesFlux)
                //.expectNext("alex", "ben", "chloe")
                //.expectNextCount(3)
                .expectNext("alex")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFlux_map() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_map();

        StepVerifier.create(namesFlux_map)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_immutability() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_immutability();

        StepVerifier.create(namesFlux_map)
                //.expectNext("ALEX", "BEN", "CHLOE")
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void namesFlux_filter() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_filter(3);

        StepVerifier.create(namesFlux_map)
                .expectNext("ALEX", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_filterMapPipeline() {

        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_filterMapPipeline(3);

        StepVerifier.create(namesFlux_map)
                .expectNext("4-ALEX", "5-CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatMap() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_flatMap(3);

        StepVerifier.create(namesFlux_map)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatMapAsync() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_flatMapAsync(3);

        //This did not fail as expected
        StepVerifier.create(namesFlux_map)
                //.expectNext("G", "A", "V", "A", "S", "K", "A", "R", "W", "A", "L", "S", "H", "R", "O", "B", "I", "N")
                .expectNextCount(18)
                .verifyComplete();
    }

    @Test
    void namesFlux_ConcatMap() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_ConcatMap(3);
        StepVerifier.create(namesFlux_map)
                .expectNext("G", "A", "V", "A", "S", "K", "A", "R", "W", "A", "L", "S", "H", "R", "O", "B", "I", "N")
                .verifyComplete();
    }

    @Test
    void namesMono_flatMap() {
        Mono<List<String>> namesFlux_map = fluxAndMonoGeneratorService.namesMono_flatMap(3);
        StepVerifier.create(namesFlux_map)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    void namesMono_flatMapMany() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesMono_flatMapMany(3);
        StepVerifier.create(namesFlux_map)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_transform(3);
        StepVerifier.create(namesFlux_map)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_defaultIfEmpty() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_defaultIfEmpty(5);
        StepVerifier.create(namesFlux_map)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFlux_switchIfEmpty() {
        Flux<String> namesFlux_map = fluxAndMonoGeneratorService.namesFlux_switchIfEmpty(5);
        StepVerifier.create(namesFlux_map)
                .expectNext("D","E","F","A","U","L","T")
                .verifyComplete();
    }

    @Test
    void explore_concat() {

        Flux<String> abcdef= fluxAndMonoGeneratorService.explore_concat();
        StepVerifier.create(abcdef)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void explore_concatWith() {

        Flux<String> abcdef= fluxAndMonoGeneratorService.explore_concatWith();
        StepVerifier.create(abcdef)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void explore_concatWithMono() {
        Flux<String> ab= fluxAndMonoGeneratorService.explore_concatWithMono();
        StepVerifier.create(ab)
                .expectNext("A","B")
                .verifyComplete();
    }

    @Test
    void explore_merge() {
        Flux<String> adbecf= fluxAndMonoGeneratorService.explore_merge();
        StepVerifier.create(adbecf)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWith() {
        Flux<String> adbecf= fluxAndMonoGeneratorService.explore_mergeWith();
        StepVerifier.create(adbecf)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWithMono() {
        Flux<String> ba= fluxAndMonoGeneratorService.explore_mergeWithMono();
        StepVerifier.create(ba)
                .expectNext("B","A")
                .verifyComplete();
    }

    @Test
    void explore_mergeSequential() {
        Flux<String> abcdef= fluxAndMonoGeneratorService.explore_mergeSequential();
        StepVerifier.create(abcdef)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void explore_zip() {
        Flux<String> adbecf= fluxAndMonoGeneratorService.explore_zip();
        StepVerifier.create(adbecf)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    void explore_zipTuples() {
        Flux<String> t4= fluxAndMonoGeneratorService.explore_zipTuples();
        StepVerifier.create(t4)
                .expectNext("AD14","BE25","CF36")
                .verifyComplete();
    }

    @Test
    void explore_zipWith() {
        Flux<String> adbecf= fluxAndMonoGeneratorService.explore_zipWith();
        StepVerifier.create(adbecf)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    void explore_zipWithMono() {
        Mono<String> ab= fluxAndMonoGeneratorService.explore_zipWithMono();
        StepVerifier.create(ab)
                .expectNext("AB")
                .verifyComplete();
    }
}