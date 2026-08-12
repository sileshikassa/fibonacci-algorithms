package algorithms;

import java.util.Scanner;

/**
    Tested on MacBook Air M2 24GB RAM

   This class will safely generate the fibonacci(95):
     Enter a number to find the Fibonacci sequence element: 95
     Fibonacci of 95 is:
     1293530146158671551

   Where as fibonacci(96) produces an long integer overflow with negative number:
      Enter a number to find the Fibonacci sequence element: 96
      Fibonacci of 96 is:
      -4953053512429003327

 */

public class FibonacciLongInteger {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number to find the Fibonacci sequence element: ");
        int n = scanner.nextInt();

        // Calculate once and store the sequence array
        long[] fibonacciSequence = fib(n);

        if (fibonacciSequence.length > 0) {
            // The N-th Fibonacci number is at index n - 1
            System.out.println("Fibonacci of " + n + " is:\n" + fibonacciSequence[n - 1]);
        } else {
            System.out.println("Invalid input.");
        }
    }

    public static long[] fib(int n) {
        if (n <= 0) {
            return new long[0];
        } else if (n == 1) {
            return new long[]{0};
        } else if (n == 2) {
            return new long[]{0, 1};
        }

        long[] sequence = new long[n];
        sequence[0] = 0;
        sequence[1] = 1;
        for (int i = 2; i < n; i++) {
            sequence[i] = sequence[i - 1] + sequence[i - 2];
        }

        return sequence;
    }
}
