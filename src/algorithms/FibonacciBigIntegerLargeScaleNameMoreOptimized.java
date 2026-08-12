package algorithms;

import java.math.BigInteger;
import java.util.Scanner;

/**
 The optimization process in this class has produced the first successful running of
 Fibonacci(1,000,000,000) and Fibonacci(2,000,000,000). This journey is a textbook
 case study for software engineering. It demonstrates that high-performance
 programming isn't just about writing "fast code" — it is about understanding memory
 architecture, operational complexity, hidden framework overhead and the platform you
 are running on.

     Enter a number to find the Fibonacci sequence element: 1000000000
     Calculating mathematical value (Warp Speed)... please wait...
     Pure Computation Time: 457780 ms

     --- Scale Analysis ---
     Total Digits: 208987640
     Estimated Scale Name: Multi-Millinillion Group (Scale index: 69662545-illion)
     Post Computation Time: 7 ms


     Enter a number to find the Fibonacci sequence element: 1500000000
     Calculating mathematical value (Warp Speed)... please wait...
     Pure Computation Time: 4355228 ms => 72 minutes

     --- Scale Analysis ---
     Total Digits: 313481461
     Estimated Scale Name: Multi-Millinillion Group (Scale index: 104493819-illion)
     Post Computation Time: 7 ms

     Enter a number to find the Fibonacci sequence element (Parallel Engine): 2000000000
     Calculating mathematical value (Parallel Warp Speed)... please wait...
     Pure Computation Time: 3307117 ms => 55.12 minutes

     --- Scale Analysis ---
     Total Digits: 417975281
     Estimated Scale Name: Multi-Millinillion Group (Scale index: 139325092-illion)
     Post Computation Time: 9 ms

 *
 */
public class FibonacciBigIntegerLargeScaleNameMoreOptimized {

    // Latin prefix arrays for the Short Scale naming system
    private static final String[] UNITS = {"", "Un", "Duo", "Tres", "Quattuor",
                                           "Quinque", "Se", "Septen", "Octo", "Novem"};

    private static final String[] TENS = {"", "Deci", "Viginti", "Triginta", "Quadraginta",
                                          "Quinquaginta", "Sexaginta", "Septuaginta",
                                          "Octoginta", "Nonaginta"};

    private static final String[] HUNDREDS = {"", "Centi", "Ducenti", "Trecenti",
                                              "Quadringenti", "Quingenti", "Sescenti",
                                              "Septingenti", "Octingenti", "Nongenti"};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number to find the Fibonacci sequence element: ");
        // int n = scanner.nextInt();
        // Upgraded to long to avoid overflow on massive inputs
        long n = scanner.nextLong();

        System.out.println("Calculating mathematical value (Warp Speed)... please wait...");
        long startTime = System.currentTimeMillis();

        // Compute the binary value via Fast Doubling
        BigInteger result = fibOptimized(n);
        long endTime = System.currentTimeMillis();
        System.out.println("Pure Computation Time: " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();

        if (result != null) {
            System.out.println("\n--- Scale Analysis ---");

            // Instantly compute the exact digit count mathematically
            double log10Phi = 0.2089876402499787;
            double log10Sqrt5 = 0.3494850021680094;
            //int digits = (int) Math.ceil(n * log10Phi - log10Sqrt5);
            long digits = (int) Math.floor(n * log10Phi - log10Sqrt5) + 1;
            if (n == 1 || n == 2) digits = 1;

            System.out.println("Total Digits: " + digits);
            System.out.println("Estimated Scale Name: " + getIllionName(digits));
            endTime = System.currentTimeMillis();
            System.out.println("Post Computation Time: " +(endTime - startTime) + " ms");
            System.out.println("\n[SUCCESS: Cleared all string formatting traps!]");
        } else {
            System.out.println("Invalid input.");
        }
        scanner.close();
    }

    // Fast Doubling Method: Reduces time complexity from O(N) to O(log N)
    public static BigInteger fibOptimized(long n) {
        if (n <= 0) return BigInteger.ZERO;
        return fastDoubling(n)[0];
    }

    private static BigInteger[] fastDoubling(long n) {
        if (n == 0) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE};
        }

        // Recursive binary split
        BigInteger[] res = fastDoubling(n / 2);
        BigInteger fk = res[0];      // F(k)
        BigInteger fk1 = res[1];     // F(k+1)

        // F(2k) = F(k) * [2 * F(k+1) - F(k)]
        BigInteger f2k = fk.multiply(fk1.shiftLeft(1).subtract(fk));

        // F(2k+1) = F(k+1)^2 + F(k)^2
        BigInteger f2k1 = fk1.multiply(fk1).add(fk.multiply(fk));

        if (n % 2 == 0) {
            return new BigInteger[]{f2k, f2k1};
        } else {
            return new BigInteger[]{f2k1, f2k.add(f2k1)};
        }
    }

    // Upgraded Naming Engine based directly on digit count
    public static String getIllionName(long digits) {
        if (digits <= 3) return "Hundreds / Thousands";

        long powerOfTen = digits - 1;
        int targetIndex = (int) (powerOfTen - 3) / 3;
        if (targetIndex <= 0) return "Thousands";

        String[] smallIllions = {"Million", "Billion", "Trillion", "Quadrillion",
                                 "Quintillion", "Sextillion", "Septillion", "Octillion",
                                 "Nonillion", "Decillion"};
        if (targetIndex < smallIllions.length) {
            return smallIllions[targetIndex];
        }

        int thousandsGroup = targetIndex / 1000;
        int remainder = targetIndex % 1000;

        int u = remainder % 10;
        int t = (remainder / 10) % 10;
        int h = (remainder / 100) % 10;

        String baseName = UNITS[u] + TENS[t] + HUNDREDS[h];
        if (u == 0 && t == 0 && h == 1) baseName = "Centi";

        String[] milliPrefixes = {"", "millin", "dumillin", "tremillin", "quadrimillin", "quinquamillin", "sesmillin", "septimmillin", "octomillin", "novemmillin"};

        String finalName = "";
        if (thousandsGroup > 0 && thousandsGroup < milliPrefixes.length) {
            finalName = (baseName.isEmpty() ? "" : baseName.toLowerCase() + "cet-") + milliPrefixes[thousandsGroup] + "illion";
        } else if (thousandsGroup >= milliPrefixes.length) {
            // Ultimate fallback for massive scale indices beyond 33,000 digits
            return "Multi-Millinillion Group (Scale index: " + targetIndex + "-illion)";
        } else {
            finalName = baseName.toLowerCase() + "illion";
        }

        return finalName.replaceAll("ii", "i");
    }
}