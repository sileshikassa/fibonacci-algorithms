package algorithms;

import java.math.BigInteger;
import java.util.Scanner;

/**
    Tested on MacBook Air M2 24GB RAM

    Fibonacci(383885) produces a very large fibonacci number that goes many pages:
    Enter a number to find the Fibonacci sequence element: 383885
    Fibonacci number 383885 is:
    458991582114173115600725298549851139268718378501806578636998756270.......

    Fibonacci(383888) produces OutOfMemoryError:
    Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at java.base/java.math.BigInteger.add(BigInteger.java:1476)
	at java.base/java.math.BigInteger.add(BigInteger.java:1382)
	at algorithms.FibonacciBigInteger.fib(FibonacciBigInteger.java:37)
	at algorithms.FibonacciBigInteger.main(FibonacciBigInteger.java:14)


    BigInteger.add():
     int result[] = new int[xIndex]; <<<---array allocation for large number elements.
 */

public class FibonacciBigInteger {

    public static void main(String[] args) {
        System.out.println("Starting application version: " + Version.VERSION);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number to find the Fibonacci sequence element: ");
        int n = scanner.nextInt();

        // Calculate once and store the sequence array
        BigInteger[] fibonacciSequence = fib(n);

        if (fibonacciSequence.length > 0) {
            // The N-th Fibonacci number is at index n - 1
            System.out.println("Fibonacci number " + n + " is:\n" + fibonacciSequence[n - 1]);
        } else {
            System.out.println("Invalid input.");
        }
    }

    public static BigInteger[] fib(int n) {
        if (n <= 0) {
            return new BigInteger[0];
        } else if (n == 1) {
            return new BigInteger[]{BigInteger.ZERO};
        } else if (n == 2) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE};
        }

        BigInteger[] sequence = new BigInteger[n];
        sequence[0] = BigInteger.ZERO;
        sequence[1] = BigInteger.ONE;
        for (int i = 2; i < n; i++) {
            sequence[i] = sequence[i - 1].add(sequence[i - 2]);
        }
        return sequence;
    }
}