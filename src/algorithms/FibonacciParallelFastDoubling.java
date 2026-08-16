package algorithms;

import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 *

     Enter a number to find the Fibonacci sequence element (Parallel Engine): 1000000000
     Calculating mathematical value (Iteration 7 Warp Speed)... please wait...
     ...
     ...
     [PROGRESS] Bitmask Processing Tree: 70% completed...
     [PROGRESS] Bitmask Processing Tree: 80% completed...
     [PROGRESS] Bitmask Processing Tree: 90% completed...
     [PROGRESS] Bitmask Processing Tree: 100% completed...
     Pure Computation Time: 220473 ms -> 3.67 minutes

     --- Scale Analysis ---
     Total Digits: 208987640
     Estimated Scale Name: Multi-Millinillion Group (Scale index: 69662545-illion)
     Post Computation Time: 2 ms


     Enter a number to find the Fibonacci number (Iteration 7 - Loop Parallel): 2000000000
     Calculating mathematical value (Iteration 7 Warp Speed)... please wait...
     [PROGRESS] Bitmask Processing Tree: 70% completed...
     [PROGRESS] Bitmask Processing Tree: 80% completed...
     [PROGRESS] Bitmask Processing Tree: 90% completed...
     [PROGRESS] Bitmask Processing Tree: 100% completed...
     Pure Computation Time: 1785221 ms -> 29.75 minutes

    --- Scale Analysis ---
    Total Digits: 417975281
    Estimated Scale Name: Multi-Millinillion Group (Scale index: 139325092-illion)
    Post Computation Time: 1 ms

 */
public class FibonacciParallelFastDoubling {

    private static final String[] UNITS = {"", "Un", "Duo", "Tres", "Quattuor",
            "Quinque", "Se", "Septen", "Octo", "Novem"};
    private static final String[] TENS = {"", "Deci", "Viginti", "Triginta","Quadraginta",
             "Quinquaginta", "Sexaginta", "Septuaginta", "Octoginta", "Nonaginta"};
    private static final String[] HUNDREDS = {"", "Centi", "Ducenti", "Trecenti",
            "Quadringenti", "Quingenti", "Sescenti", "Septingenti", "Octingenti",
            "Nongenti"};

    // Threshold below which standard single-threaded multiplication is faster (in bits)
    private static final int PARALLEL_THRESHOLD = 20000;
    private static final ForkJoinPool POOL = ForkJoinPool.commonPool();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number to find the Fibonacci sequence element (Parallel Engine): ");
        long n = scanner.nextLong();

        System.out.println("Calculating mathematical value (Parallel Warp Speed)... please wait...");
        long startTime = System.currentTimeMillis();

        BigInteger result = fibParallel(n);
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
            System.out.println("\n[SUCCESS: Parallel Compute Pipeline Cleared!]");
        } else {
            System.out.println("Invalid input.");
        }
        scanner.close();
    }

    public static BigInteger fibParallel(long n) {
        if (n <= 0) return BigInteger.ZERO;
        return fastDoubling(n)[0];
    }

    private static BigInteger[] fastDoubling(long n) {
        if (n == 0) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE};
        }

        BigInteger[] res = fastDoubling(n / 2);
        BigInteger fk = res[0];
        BigInteger fk1 = res[1];

        // F(2k) = F(k) * [2 * F(k+1) - F(k)]
        BigInteger intermediate2k = fk1.shiftLeft(1).subtract(fk);
        BigInteger f2k = parallelMultiply(fk, intermediate2k);

        // F(2k+1) = F(k+1)^2 + F(k)^2
        BigInteger fk1Sq = parallelMultiply(fk1, fk1);
        BigInteger fkSq = parallelMultiply(fk, fk);
        BigInteger f2k1 = fk1Sq.add(fkSq);

        if (n % 2 == 0) {
            return new BigInteger[]{f2k, f2k1};
        } else {
            return new BigInteger[]{f2k1, f2k.add(f2k1)};
        }
    }

    // Orchestrates parallel multiplication tasks if the numbers are large enough
    private static BigInteger parallelMultiply(BigInteger x, BigInteger y) {
        if (x.bitLength() < PARALLEL_THRESHOLD || y.bitLength() < PARALLEL_THRESHOLD) {
            return x.multiply(y);
        }
        return POOL.invoke(new ParallelKaratsuba(x, y));
    }

    // Custom ForkJoin Task implementing Parallel Karatsuba Multiplication
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

            // Base case: if numbers get small during splitting, drop back to single-thread
            if (m < PARALLEL_THRESHOLD) {
                return x.multiply(y);
            }

            m = m / 2;

            // Split the numbers in half: x = x1 * 2^m + x0
            BigInteger x1 = x.shiftRight(m);
            BigInteger x0 = x.subtract(x1.shiftLeft(m));
            BigInteger y1 = y.shiftRight(m);
            BigInteger y0 = y.subtract(y1.shiftLeft(m));

            // Create asynchronous child tasks for the three Karatsuba sub-multiplications
            ParallelKaratsuba task1 = new ParallelKaratsuba(x1, y1);
            ParallelKaratsuba task2 = new ParallelKaratsuba(x0, y0);
            ParallelKaratsuba task3 = new ParallelKaratsuba(x1.add(x0), y1.add(y0));

            // Fork task1 and task2 to run on other CPU threads simultaneously
            task1.fork();
            task2.fork();

            // Compute task3 on the current thread to maximize resource utilization
            BigInteger z2 = task3.compute();
            BigInteger z0 = task2.join();
            BigInteger z1 = task1.join();

            // Karatsuba formula assembly: z2*2^(2m) + (z2 - z1 - z0)*2^m + z0
            BigInteger middle = z2.subtract(z1).subtract(z0);
            return z1.shiftLeft(2 * m).add(middle.shiftLeft(m)).add(z0);
        }
    }

    public static String getIllionName(long digits) {
        if (digits <= 3) return "Hundreds / Thousands";
        long powerOfTen = digits - 1;
        long targetIndex = (powerOfTen - 3) / 3;
        if (targetIndex < 0) return "Thousands";

        String[] smallIllions = {"Million", "Billion", "Trillion", "Quadrillion", "Quintillion", "Sextillion", "Septillion", "Octillion", "Nonillion", "Decillion"};
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

        return finalName.replaceAll("ii", "i");
    }
}