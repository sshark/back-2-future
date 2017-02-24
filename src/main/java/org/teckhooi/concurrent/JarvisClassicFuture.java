package org.teckhooi.concurrent;

import javaslang.concurrent.Future;
import javaslang.control.Option;
import javaslang.control.Try;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.teckhooi.concurrent.Foo.foo;

public class JarvisClassicFuture {
    public static void main(String[] args) throws Throwable {
        System.out.println("Running... " + JarvisClassicFuture.class.getName());

        final long sleepMillis = 1500;

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        executorService.submit(() -> {  // bigF
            Try.run(() -> Thread.sleep(sleepMillis));
            Option.of(-10).toTry().flatMap(t -> Try.run(() -> foo(t))).get();
        });

        executorService.submit(() -> {  // smallF
            Try.run(() -> Thread.sleep(sleepMillis));
            Option.of(200).toTry().flatMap(t -> Try.run(() -> foo(t))).get();
        });

        Future.run(() -> Thread.sleep(sleepMillis)).onComplete(ignored -> System.out.println("Slang future completed " + ignored));

        executorService.shutdown();
        System.out.println("Normal termination? " + executorService.awaitTermination(2, TimeUnit.SECONDS));

    }
}

