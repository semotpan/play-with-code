package io.awssamples;

public class Benchmarks {

    public static void measureExecution(Runnable runnable) {
        var startTime = System.nanoTime();

        runnable.run();

        var durationInSeconds = (System.nanoTime() - startTime) / 1_000_000_000;

        var hours = durationInSeconds / 3600;
        var minutes = (durationInSeconds % 3600) / 60;
        var seconds = durationInSeconds % 60;

        System.out.printf("Taken time : %02d:%02d:%02d%n", hours, minutes, seconds);
    }
}
