package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"));
    }

    public Mono<String> nameMono() {
        return Mono.just("alex");
    }

    public Flux<String> namesFlux_map() {
        return namesFlux().
                //map(String::toUpperCase);
                        map(s -> s.toUpperCase());
    }

    public Flux<String> namesFlux_immutability() {
        Flux<String> namesFlux = namesFlux();
        namesFlux.map(s -> s.toUpperCase());
        return namesFlux;
    }

    public Flux<String> namesFlux_filter(int stringLength) {
        return namesFlux()
                .map(s -> s.toUpperCase())
                .filter(s -> s.length() > stringLength);
    }

    public Flux<String> namesFlux_filterMapPipeline(int stringLength) {
        return namesFlux()
                .map(s -> s.toUpperCase())
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + "-" + s);
    }

    public Flux<String> namesFlux_flatMap(int stringLength) {
        return namesFlux()
                .map(s -> s.toUpperCase())
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitString(s));
    }

    public Flux<String> namesFlux_flatMapAsync(int stringLength) {
        Flux<String> names = Flux.fromIterable(List.of("gavaskar", "walsh", "robin"));

        return names
                .map(s -> s.toUpperCase())
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitStringWithDelay(s));
    }

    public Flux<String> namesFlux_ConcatMap(int stringLength) {
        Flux<String> names = Flux.fromIterable(List.of("gavaskar", "walsh", "robin"));

        return names
                .map(s -> s.toUpperCase())
                .filter(s -> s.length() > stringLength)
                .concatMap(s -> splitStringWithDelay(s));
    }


    public Mono<List<String>> namesMono_flatMap(int length) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3)
                //.map(this::splitStringMono); - transformation returns another reactive type Mono. Hence, we have to flatten it.
                .flatMap(this::splitStringMono);
        //.flatMap(s -> splitStringMono(s)); - alternative way to write above code

    }

    public Flux<String> namesMono_flatMapMany(int length) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3)
                //.map(this::splitString); - transformation returns another reactive type Flux. Hence, we have to flatten it.
                .flatMapMany(this::splitString);
        //.flatMapMany(s -> splitString(s));// - alternative way to write above code

    }

    public Flux<String> namesFlux_transform(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap =
                name -> name.map(s -> s.toUpperCase())
                        .filter(s -> s.length() > stringLength);

        return namesFlux()
                /*
                This function has been moved as Function Functional interface
                .map(s -> s.toUpperCase())
                .filter(s -> s.length() > stringLength)
                */
                .transform(filterMap)
                .flatMap(s -> splitString(s));
    }

    public Flux<String> namesFlux_defaultIfEmpty(int stringLength) {
        return namesFlux_transform(stringLength).defaultIfEmpty("default");
    }

    public Flux<String> namesFlux_switchIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap =
                name -> name.map(s -> s.toUpperCase())
                        .filter(s -> s.length() > stringLength)
                        //Note that the flatmap has been moved here to make it common for default flux and return
                        .flatMap(s -> splitString(s));

        Flux<String> defaultFlux = Flux.just("default").transform(filterMap);

        return namesFlux().transform(filterMap).switchIfEmpty(defaultFlux);

    }

    public Flux<String> explore_concat(){
        Flux<String> abc=Flux.just("A","B","C");
        Flux<String> def= Flux.just("D","E","F");

        return Flux.concat(abc,def);
    }

    public Flux<String> explore_concatWith(){
        Flux<String> abc=Flux.just("A","B","C");
        Flux<String> def= Flux.just("D","E","F");

        return abc.concatWith(def);
    }

    public Flux<String> explore_concatWithMono(){
        Mono<String> a=Mono.just("A");
        Mono<String> b= Mono.just("B");

        return a.concatWith(b);
    }

    public Flux<String> explore_merge(){
        Flux<String> abc=Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> def= Flux.just("D","E","F").delayElements(Duration.ofMillis(125));

        return Flux.merge(abc,def);
    }

    public Flux<String> explore_mergeWith(){
        Flux<String> abc=Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> def= Flux.just("D","E","F").delayElements(Duration.ofMillis(125));

        return abc.mergeWith(def);
    }

    public Flux<String> explore_mergeWithMono(){
        Mono<String> abc=Mono.just("A").delayElement(Duration.ofMillis(125));
        Mono<String> def= Mono.just("B").delayElement(Duration.ofMillis(100));

        return abc.mergeWith(def);
    }

    public Flux<String> explore_mergeSequential(){
        Flux<String> abc=Flux.just("A","B","C").delayElements(Duration.ofMillis(125));
        Flux<String> def= Flux.just("D","E","F").delayElements(Duration.ofMillis(100));

        //Merge is sequential even if abc is having more delay than def
        return Flux.mergeSequential(abc,def).log();
    }

    public Flux<String> explore_zip(){
        Flux<String> abc=Flux.just("A","B","C");
        Flux<String> def= Flux.just("D","E","F");

        return Flux.zip(abc, def, (first,second) -> first+second);
    }

    public Flux<String> explore_zipTuples(){
        Flux<String> abc=Flux.just("A","B","C");
        Flux<String> def= Flux.just("D","E","F");

        Flux<String> _123Flux=Flux.just("1","2","3");
        Flux<String> _456Flux= Flux.just("4","5","6");

        return Flux.zip(abc, def,_123Flux,_456Flux)
                .map(t4 -> t4.getT1()+t4.getT2()+t4.getT3()+t4.getT4());
    }

    public Flux<String> explore_zipWith(){
        Flux<String> abc=Flux.just("A","B","C");
        Flux<String> def= Flux.just("D","E","F");

        return abc.zipWith(def, (first,second) -> first+second);
    }

    public Mono<String> explore_zipWithMono(){
        Mono<String> a=Mono.just("A");
        Mono<String> b= Mono.just("B");

        //This returns Mono
        return a.zipWith(b)
                .map(t2 -> t2.getT1()+t2.getT2());
    }


    private Flux<String> splitString(String name) {
        return Flux.fromArray(name.split(""));
    }

    private Flux<String> splitStringWithDelay(String name) {
        Random random = new Random();
        int delay = random.nextInt(1000);
        return Flux.fromArray(name.split("")).delayElements(Duration.ofMillis(delay));
    }

    private Mono<List<String>> splitStringMono(String s) {
        return Mono.just(List.of(s.split("")));
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux().subscribe(System.out::println);
        fluxAndMonoGeneratorService.nameMono().subscribe(System.out::println);
    }
}
