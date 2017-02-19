package org.teckhooi.concurrent;

import static org.teckhooi.concurrent.Foo.*;

import javaslang.Function1;
import javaslang.concurrent.Future;
import javaslang.control.Option;
import javaslang.control.Try;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by sshark on 18/2/17.
 */

public class JayFuture {
    public static void main(String[] args) throws Throwable {
        System.out.println("Running...");

        Future<Void> bigF = Future.run(() -> {
                Try.run(() -> Thread.sleep(3000));
                Option.of(100).toTry().flatMap(t -> Try.run(() -> foo(t))).getOrElseThrow(Function.identity());
                System.out.println("BigF is fine");
            });

        Future<Void> smallF = Future.run(() -> {
            Try.run(() -> Thread.sleep(3000));
            Option.of(-200).toTry().flatMap(t -> Try.run(() -> foo(t))).getOrElseThrow(Function.identity());
            System.out.println("SmallF is fine");
        });

        Try.run(() -> Thread.sleep(2000));

        System.out.print("Results, ");

        ExecutorService executorService = bigF.executorService();
        System.out.println("Await terminataion...");
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}

