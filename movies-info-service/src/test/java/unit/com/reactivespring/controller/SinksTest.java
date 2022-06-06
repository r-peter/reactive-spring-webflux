package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        //when

        //If failure happens, fail fast
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux1=replaySink.asFlux();
        integerFlux1.subscribe( i -> {
            System.out.println("Subscriber 1 : "+ i);
        });

        Flux<Integer> integerFlux2=replaySink.asFlux();
        integerFlux2.subscribe( i -> {
            System.out.println("Subscriber 2 : "+ i);
        });

        replaySink.tryEmitNext(3);

        Flux<Integer> integerFlux3=replaySink.asFlux();
        integerFlux3.subscribe( i -> {
            System.out.println("Subscriber 3 : "+ i);
        });
    }

    @Test
    void sinks_multicast() {
        //given
        Sinks.Many<Integer> multicastSink = Sinks.many().multicast().onBackpressureBuffer();

        //when
        //If failure happens, fail fast
        multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux1=multicastSink.asFlux();
        integerFlux1.subscribe( i -> {
            System.out.println("Subscriber 1 : "+ i);
        });

        Flux<Integer> integerFlux2=multicastSink.asFlux();
        integerFlux2.subscribe( i -> {
            System.out.println("Subscriber 2 : "+ i);
        });

        multicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    void sinks_unicast() {
        //given
        Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();

        //when
        //If failure happens, fail fast
        unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux1=unicastSink.asFlux();
        integerFlux1.subscribe( i -> {
            System.out.println("Subscriber 1 : "+ i);
        });

        Flux<Integer> integerFlux2=unicastSink.asFlux();
        integerFlux2.subscribe( i -> {
            System.out.println("Subscriber 2 : "+ i);
        });

        unicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
