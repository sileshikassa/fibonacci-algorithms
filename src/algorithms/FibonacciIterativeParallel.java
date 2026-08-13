package algorithms;

import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FibonacciIterativeParallel {

    private static final String[] UNITS = {"", "Un", "Duo", "Tres", "Quattuor", "Quinque",
                                           "Se", "Septen", "Octo", "Novem"};
    private static final String[] TENS = {"", "Deci", "Viginti", "Triginta", "Quadraginta",
                                          "Quinquaginta", "Sexaginta", "Septuaginta",
                                          "Octoginta", "Nonaginta"};
    private static final String[] HUNDREDS = {"", "Centi", "Ducenti", "Trecenti",
                                              "Quadringenti", "Quingenti", "Sescenti",
                                              "Septingenti", "Octingenti", "Nongenti"};

    // OPTIMAL HARDWARE THRESHOLD FOR EXTREME SCALING (VALIDATED ON MAC M2
    private static final int PARALLEL_THRESHOLD = 500000;
    private static final ForkJoinPool POOL = ForkJoinPool.commonPool();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number to find the Fibonacci numer (Iteration 7 - " +
                         "Loop Parallel): ");
        long n = scanner.nextLong();

        System.out.println("Calculating mathematical value (Iteration 7 Warp Speed)..." +
                           " please wait...");
        long startTime = System.currentTimeMillis();

        BigInteger result = fibIterativeParallel(n);
        long endTime = System.currentTimeMillis();
        System.out.println("Pure Computation Time: " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();

        if (result != null) {
            System.out.println("\n--- Scale Analysis ---");
            double log10Phi = 0.2089876402499787;
            double log10Sqrt5 = 0.3494850021680094;
            long digits = (long) Math.floor(n * log10Phi - log10Sqrt5) + 1;
            if (n == 1 || n == 2) digits = 1;

            System.out.println("Total Digits: " + digits);
            System.out.println("Estimated Scale Name: " + getIllionName(digits));
            endTime = System.currentTimeMillis();
            System.out.println("Post Computation Time: " + (endTime - startTime) + " ms");
            System.out.println("\n[SUCCESS: Iteration 7 Standard Cleared!]");
        }
        scanner.close();
    }

    // ITERATION 7 OPTIMIZATION: Left-to-right bit processing loop (No Recursive Methods)
    public static BigInteger fibIterativeParallel(long n) {
        if (n <= 0) return BigInteger.ZERO;

        BigInteger a = BigInteger.ZERO; // F(k)
        BigInteger b = BigInteger.ONE;  // F(k+1)

        long mask = Long.highestOneBit(n);

        while (mask > 0) {
            // F(2k) = F(k) * [2 * F(k+1) - F(k)]
            BigInteger intermediate2k = b.shiftLeft(1).subtract(a);
            BigInteger d = parallelMultiply(a, intermediate2k);

            // F(2k+1) = F(k+1)^2 + F(k)^2
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

    private static BigInteger parallelMultiply(BigInteger x, BigInteger y) {
        if (x.bitLength() < PARALLEL_THRESHOLD || y.bitLength() < PARALLEL_THRESHOLD) {
            return x.multiply(y);
        }
        return POOL.invoke(new ParallelKaratsuba(x, y));
    }

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
            if (m < PARALLEL_THRESHOLD) {
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

    public static String getIllionName(long digits) {
        //if (digits <= 3) return "Hundreds / Thousands"; Assertion failure
        if (digits <= 3) return "Hundreds";
        long powerOfTen = digits - 1;
        long targetIndex = (powerOfTen - 3) / 3;
        //if (targetIndex < 0) return "Thousands";  Assertion failure
        if (targetIndex <= 0) return "Thousands";


        String[] smallIllions = {"Thousands", "Million", "Billion", "Trillion",
                                 "Quadrillion", "Quintillion", "Sextillion",
                                 "Septillion", "Octillion", "Nonillion", "Decillion"};
        if (targetIndex < smallIllions.length) {
            return smallIllions[(int) targetIndex];
        }

        long thousandsGroup = targetIndex / 1000;
        int remainder = (int) (targetIndex % 1000);

        int u = remainder % 10;
        int t = (remainder / 10) % 10;
        int h = (remainder / 100) % 10;

        String baseName = UNITS[u] + TENS[t] + HUNDREDS[h];
        if (u == 0 && t == 0 && h == 1) baseName = "Centi";

        String[] milliPrefixes = {"", "millin", "dumillin", "tremillin", "quadrimillin", "quinquamillin", "sesmillin", "septimmillin", "octomillin", "novemmillin"};

        String finalName = "";
        if (thousandsGroup > 0 && thousandsGroup < milliPrefixes.length) {
            finalName = (baseName.isEmpty() ? "" : baseName.toLowerCase() + "cet-") + milliPrefixes[(int) thousandsGroup] + "illion";
        } else if (thousandsGroup >= milliPrefixes.length) {
            return "Multi-Millinillion Group (Scale index: " + targetIndex + "-illion)";
        } else {
            finalName = baseName.toLowerCase() + "illion";
        }
        //return finalName.replaceAll("ii", "i");
        // Clear vowel double-ups and automatically capitalize the first letter
        String result = finalName.replaceAll("ii", "i");
        if (result.isEmpty()) return "";
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }
}