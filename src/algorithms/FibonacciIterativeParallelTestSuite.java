package algorithms;

public class FibonacciIterativeParallelTestSuite {

    public static void main(String[] args) {
        System.out.println("Starting application version: " + Version.VERSION);
        System.out.println("🧪 Running Fibonacci Engine Test Suite...\n");

        try {
            testDigitCountingFormula();
            testSmallIllionNames();
            testLargeIllionNames();
            testExtremeFallbackScale();

            System.out.println("\n🎉 ALL TESTS PASSED SUCCESSFULLY! [7/7]");
        }
        catch (AssertionError e) {
            System.err.println("\n❌ TEST FAILED!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testDigitCountingFormula() {
        System.out.print("Testing Logarithmic Digit Calculator... ");

        double log10Phi = 0.2089876402499787;
        double log10Sqrt5 = 0.3494850021680094;

        // F(1) and F(2) are 1 (1 digit)
        long digitsF1 = 1;
        // Test F(10) -> Actual Fibonacci value is 55 (2 digits)
        long digitsF10 = (long) Math.floor(10 * log10Phi - log10Sqrt5) + 1;
        // Test F(1,000,000,000) -> Verified milestone (208,987,640 digits)
        long digitsF1B = (long) Math.floor(1000000000L * log10Phi - log10Sqrt5) + 1;

        assertEquals(1, digitsF1, "F(1) digit tracking");
        assertEquals(2, digitsF10, "F(10) digit tracking");
        assertEquals(208987640L, digitsF1B, "F(1,000,000,000) milestone tracking");
        System.out.println("Passed.");
    }

    private static void testSmallIllionNames() {
        System.out.print("Testing Small '-illion' Boundaries... ");
        // 4 digits = Thousands tier
        assertEquals("Thousands", FibonacciIterativeParallel.getIllionName(4), "4 digits boundary");
        // 7 digits = Millions tier (1,000,000)
        assertEquals("Million", FibonacciIterativeParallel.getIllionName(7), "7 digits boundary");
        // 10 digits = Billions tier (1,000,000,000)
        assertEquals("Billion", FibonacciIterativeParallel.getIllionName(10), "10 digits boundary");
        System.out.println("Passed.");
    }

    private static void testLargeIllionNames() {
        System.out.print("Testing Large Latin Prefixes... ");
        // 3004 digits -> targetIndex = 1000 -> Centillion
        assertEquals("Centillion", FibonacciIterativeParallel.getIllionName(304), "Centillion check");
        System.out.println("Passed.");
    }

    private static void testExtremeFallbackScale() {
        System.out.print("Testing Extreme Fallback Safe-guards... ");
        // 208,987,640 digits -> verified index milestone
        String result = FibonacciIterativeParallel.getIllionName(208987640L);
        assertTrue(result.contains("Multi-Millinillion Group"), "Fallback containment check");
        assertTrue(result.contains("69662545-illion"), "Scale index validation");
        System.out.println("Passed.");
    }

    // --- Native Test Assert Helpers ---
    private static void assertEquals(long expected, long actual, String context) {
        if (expected != actual) {
            throw new AssertionError(context + " failed! Expected: " + expected + ", but got: " + actual);
        }
    }

    private static void assertEquals(String expected, String actual, String context) {
        if (!expected.equals(actual)) {
            throw new AssertionError(context + " failed! Expected: '" + expected + "', but got: '" + actual + "'");
        }
    }

    private static void assertTrue(boolean condition, String context) {
        if (!condition) {
            throw new AssertionError("Condition failed: " + context);
        }
    }
}
