package org.teckhooi.concurrent;

import javaslang.concurrent.Future;
import javaslang.control.Option;
import javaslang.control.Try;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by sshark on 2017/03/01.
 */

public class BigFTest {

    public static void main(String[] args) throws Throwable {
        final long sleepMillis = 1500;

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {  // bigF
            Try.run(() -> Thread.sleep(sleepMillis));
            Option.of(-10).toTry().flatMap(t -> Try.run(() -> foo(t))).get();
        });

        Future.run(executorService, () -> {
            System.out.println("#1");
            Thread.sleep(sleepMillis);
        }).onComplete(ignored -> System.out.println("Slang future completed " + ignored));  // #1

        System.out.println("#2");
        executorService.shutdown();
        System.out.println("Normal termination? " + executorService.awaitTermination(2, TimeUnit.SECONDS));

    }

    private static void foo(Integer t) {
    }
}