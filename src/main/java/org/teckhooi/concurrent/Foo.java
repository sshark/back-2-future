package org.teckhooi.concurrent;

public class Foo {
    public static void foo(int t) throws Exception {
        System.out.println("Foo running with " + t);
        if (t < 0) {
            throw new Exception("Must be +ve");
        }
    }
}

