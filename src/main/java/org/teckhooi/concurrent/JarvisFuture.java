package org.teckhooi.concurrent;

import javaslang.concurrent.Future;
import javaslang.control.Option;
import javaslang.control.Try;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.teckhooi.concurrent.Foo.foo;

/**
 * Created by sshark on 18/2/17.
 */

public class JarvisFuture {
    public static void main(String[] args) throws Throwable {
        System.out.println("Running... " + JarvisFuture.class.getName());

        final long sleepMillis = 1500;

        Future<Void> bigF = Future.run(() -> {
            Try.run(() -> Thread.sleep(sleepMillis));
            Option.of(-100).toTry().flatMap(t -> Try.run(() -> foo(t))).getOrElseThrow(Function.identity());
        });

        Future<Void> smallF = Future.run(() -> {
            Try.run(() -> Thread.sleep(sleepMillis));
            Option.of(200).toTry().flatMap(t -> Try.run(() -> foo(t))).getOrElseThrow(Function.identity());
        });

//        Try.run(() -> Thread.sleep(sleepMillis + 500));

        bigF.map(t -> "BigF is ok")
            .recover(t -> "BigF is facing some problem")
            .forEach(System.out::println);

        smallF.map(t -> "SmallF is ok")
            .recover(t -> "SmallF is facing some problem")
            .forEach(System.out::println);

        ExecutorService executorService = bigF.executorService();

        System.out.println("Shutdown...");
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}

