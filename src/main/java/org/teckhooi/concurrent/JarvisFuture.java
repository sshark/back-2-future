package org.teckhooi.concurrent;

import javaslang.collection.List;
import javaslang.concurrent.Future;
import javaslang.control.Option;
import javaslang.control.Try;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.teckhooi.concurrent.Foo.foo;

/**
 * This code is the Java code imitating its Scala counterpart `org.teckhooi.concurrent.ScarletFuture`
 * but it is not successful  because there is no equivalent of `Await.ready(...)` in Java yet
 */

public class JarvisFuture {
    public static void main(String[] args) throws Throwable {
        System.out.println("Running... " + JarvisFuture.class.getName());

        final long sleepMillis = 1500;

        Future<Void> bigF = Future.run(() -> {
            Try.run(() -> Thread.sleep(sleepMillis));
            Option.of(-10).toTry().flatMap(t -> Try.run(() -> foo(t))).getOrElseThrow(Function.identity());
        });

        Future<Void> smallF = Future.run(() -> {
            Try.run(() -> Thread.sleep(sleepMillis));
            Option.of(200).toTry().flatMap(t -> Try.run(() -> foo(t))).getOrElseThrow(Function.identity());
        });

        bigF.map(t -> "BigF is ok")
            .recover(t -> "BigF is facing some problem")
            .forEach(System.out::println);

        Future<String> newSmallF = smallF.map(t -> {
            Try.run(() -> Thread.sleep(sleepMillis));
            return "SmallF is ok";
        }).recover(t -> "SmallF is facing some problem");
        newSmallF.forEach(System.out::println);

        waitAndShutdown(bigF.executorService(), bigF, newSmallF);
    }

    /**
     * This crude method (from @danieldietrich of JavaSlang) is used to mimic Scala
     * `Await.ready(...)`. Careful as not to pass unrelated `executorService` and
     * `futures`. It will initial shutdown all `Future`s have completed.
     *
     * The expected (expected because it is running concurrently) output is,
     *
     * [info] Foo running with -10
     * [info] Foo running with 200
     * [info] Failure(java.lang.Exception: Must be +ve)
     * [info] Shutting down...
     * [info] BigF is facing some problem
     * [info] Shutdown.
     *
     * @param executorService the executor service used by the `Future`s
     * @param futures the `Future`s to monitor
     */
    static void waitAndShutdown(ExecutorService executorService, Future<?>... futures) {
        Future.sequence(executorService, List.of(futures)).onComplete(result -> Try.run(() -> {
            System.out.println(result);
            System.out.println("Shutting down...");
            executorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.SECONDS);
            System.out.println("Shutdown.");
        }));
    }
}

