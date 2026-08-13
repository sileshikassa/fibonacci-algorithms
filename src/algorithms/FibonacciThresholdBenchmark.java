package algorithms;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * High-Precision Automated Benchmarking Harness for Fibonacci Parallel Engines.
 * Optimized specifically for OpenJDK 23 runtime evaluation on Apple Silicon architecture.
 *
 * This harness systematically varies the bit-length threshold for the ForkJoin Parallel
 * Karatsuba implementation to identify the sweet spot where thread-orchestration overhead
 * optimally balances against hardware-level core execution efficiency.
 *
 * It incorporates a strict multi-pass warmup sequence per configuration block to ensure
 * the JVM HotSpot C2 compiler has completed native optimizations prior to telemetry collection.
 */
public class FibonacciThresholdBenchmark {

    // Target Fibonacci number to evaluate.
    // Default set to 200,000,000 to allow multiple threshold passes without extreme delay.
    // Scale this up to 1,000,000,000 or 1,500,000,000 for your final production run testing.
    private static final long TARGET_N = 1_000_000_000L;

    // Benchmarking parameters to isolate JIT compilation skew
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 3;

    // Dynamic threshold variable altered by the runner execution loops
    private static int currentParallelThreshold = 25000;
    private static final ForkJoinPool POOL = ForkJoinPool.commonPool();

    public static void main(String[] args) {
        // Define standard testing boundary tiers starting at 65,536 bits
        int[] thresholdsToTest = {
                65_536,
                100_000,
                150_000,
                250_000,
                500_000,
                1_000_000
        };

        // Storage tracking metrics layout for final tabular generation
        Map<Integer, Long> performanceResults = new LinkedHashMap<>();

        System.out.println("======================================================================");
        System.out.println("  FIBONACCI MULTI-THREADED THRESHOLD TUNING AUTOMATION ENGINE");
        System.out.println("  Runtime Environment: OpenJDK " + System.getProperty("java.version"));
        System.out.println("  Evaluating Target Sequence F_" + TARGET_N);
        System.out.println("======================================================================");

        for (int threshold : thresholdsToTest) {
            currentParallelThreshold = threshold;
            System.out.printf("\n[CONFIG] Activating Bit Threshold Boundary: %,d bits\n", threshold);

            // 1. Warmup Pass execution to trigger internal HotSpot compilation pipelines
            for (int i = 1; i <= WARMUP_RUNS; i++) {
                System.out.print("   -> Executing JIT Warmup Pass #" + i + "... ");
                long start = System.currentTimeMillis();
                BigInteger res = fibIterativeParallel(TARGET_N);
                long duration = System.currentTimeMillis() - start;
                System.out.printf("Done (%,d ms, Result bits: %,d)\n", duration, res.bitLength());
                System.gc(); // Request explicitly to clean memory tables before the next iteration
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }

            // 2. Measured Pass tracking iterations
            long accumulatedDuration = 0;
            for (int i = 1; i <= MEASURED_RUNS; i++) {
                System.out.print("   -> Executing Measured Telemetry Pass #" + i + "... ");
                long start = System.currentTimeMillis();
                fibIterativeParallel(TARGET_N);
                long duration = System.currentTimeMillis() - start;
                accumulatedDuration += duration;
                System.out.printf("Captured: %,d ms\n", duration);
                System.gc();
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }

            long averageDuration = accumulatedDuration / MEASURED_RUNS;
            performanceResults.put(threshold, averageDuration);
            System.out.printf("[RESULT] Average Compute Time for %,d bits: %,d ms\n", threshold, averageDuration);
        }

        // Output structural data matrix formatting block
        System.out.println("\n\n======================================================================");
        System.out.println("                      FINAL PERFORMANCE MATRIX REPORT");
        System.out.println("======================================================================");
        System.out.printf("%-25s | %-25s\n", "Parallel Threshold (Bits)", "Avg Execution Time (ms)");
        System.out.println("----------------------------------------------------------------------");
        for (Map.Entry<Integer, Long> entry : performanceResults.entrySet()) {
            System.out.printf("%-25s | %-25s\n",
                    String.format("%,d", entry.getKey()),
                    String.format("%,d ms", entry.getValue()));
        }
        System.out.println("======================================================================");
        System.out.println("[COMPLETE] Hardware optimization matrix parsing successfully compiled.");
    }

    /**
     * Memory-Flattened Iterative Fast Doubling Core (Stage 7 Design)
     */
    public static BigInteger fibIterativeParallel(long n) {
        if (n <= 0) return BigInteger.ZERO;

        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;

        long mask = Long.highestOneBit(n);

        while (mask > 0) {
            BigInteger intermediate2k = b.shiftLeft(1).subtract(a);
            BigInteger d = parallelMultiply(a, intermediate2k);

            BigInteger bSq = parallelMultiply(b, b);
            BigInteger aSq = parallelMultiply(a, a);
            BigInteger e = bSq.add(aSq);

            if ((n & mask) != 0) {
                a = e;
                b = d.add(e);
            } else {
                a = d;
                b = e;
            }
            mask >>>= 1;
        }
        return a;
    }

    /**
     * Parallel Multiplication router pointing to runtime parameterized thresholds.
     */
    private static BigInteger parallelMultiply(BigInteger x, BigInteger y) {
        if (x.bitLength() < currentParallelThreshold || y.bitLength() < currentParallelThreshold) {
            return x.multiply(y);
        }
        return POOL.invoke(new ParallelKaratsuba(x, y));
    }

    /**
     * Parallel Karatsuba Task Implementation mapping splits directly to worker loops.
     */
    private static class ParallelKaratsuba extends RecursiveTask<BigInteger> {
        private final BigInteger x;
        private final BigInteger y;

        ParallelKaratsuba(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        @Override
        protected BigInteger compute() {
            int m = Math.max(x.bitLength(), y.bitLength());
            if (m < currentParallelThreshold) {
                return x.multiply(y);
            }

            m = m / 2;
            BigInteger x1 = x.shiftRight(m);
            BigInteger x0 = x.subtract(x1.shiftLeft(m));
            BigInteger y1 = y.shiftRight(m);
            BigInteger y0 = y.subtract(y1.shiftLeft(m));

            ParallelKaratsuba task1 = new ParallelKaratsuba(x1, y1);
            ParallelKaratsuba task2 = new ParallelKaratsuba(x0, y0);
            ParallelKaratsuba task3 = new ParallelKaratsuba(x1.add(x0), y1.add(y0));

            task1.fork();
            task2.fork();

            BigInteger z2 = task3.compute();
            BigInteger z0 = task2.join();
            BigInteger z1 = task1.join();

            BigInteger middle = z2.subtract(z1).subtract(z0);
            return z1.shiftLeft(2 * m).add(middle.shiftLeft(m)).add(z0);
        }
    }
}

