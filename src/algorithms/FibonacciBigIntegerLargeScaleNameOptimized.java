package algorithms;

import java.math.BigInteger;
import java.util.Scanner;

public class FibonacciBigIntegerLargeScaleNameOptimized {

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
        int n = scanner.nextInt();

        System.out.println("Calculating... please wait...");
        long startTime = System.currentTimeMillis();

        BigInteger result = fibOptimized(n);
        long endTime = System.currentTimeMillis();
        System.out.println("Pure computation Time: " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        if (result != null) {
            int digits = result.toString().length();
            // Only print the full number if it's reasonably small to avoid freezing the console
            if (digits < 10000) {
                System.out.println("\nFibonacci number " + n + " is:\n" + result);
            } else {
                System.out.println("\n[Number is too large to print safely to console without lagging]");
                System.out.println("Starts with: " + result.toString().substring(0, 15) + "...");
            }

            System.out.println("\n--- Scale Analysis ---");
            System.out.println("Total Digits: " + digits);
            System.out.println("Estimated Scale Name: " + getIllionName(digits));
            endTime = System.currentTimeMillis();
            System.out.println("Post Computation Time: " + (endTime - startTime) + " ms");
        } else {
            System.out.println("Invalid input.");
        }
    }

    // Fast Doubling Method: Reduces time complexity from O(N) to O(log N)
    public static BigInteger fibOptimized(int n) {
        if (n <= 0) return null;
        return fastDoubling(n)[0];
    }

    private static BigInteger[] fastDoubling(int n) {
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
    public static String getIllionName(int digits) {
        if (digits <= 3) return "Hundreds / Thousands";

        int powerOfTen = digits - 1;
        long targetIndex = (long) (powerOfTen - 3) / 3;
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
            // Ultimate fallback for massive scale indices beyond 33,000 digits
            return "Multi-Millinillion Group (Scale index: " + targetIndex + "-illion)";
        } else {
            finalName = baseName.toLowerCase() + "illion";
        }

        return finalName.replaceAll("ii", "i");
    }
}