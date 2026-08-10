package algorithms;

import java.util.Arrays;
import java.util.Scanner;

/*
   a) Fibonacci(47) is maximum n value that produce correct Fibonacci sequence:
      [0, 1, 1, 2, 3, 5, 8, 13, 21, 34,...433494437, 701408733, 1134903170, 1836311903]

   b) Fibonacci(48) is first n value that produce integer overflow with sequence
      containing negative number:
      [0, 1, 1, 2, 3, 5, 8, 13, 21, 34,...701408733, 1134903170, 1836311903, -1323752223]
 */
public class FibonacciInteger {

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a number to find the Fibonacci sequence: ");
            int n = scanner.nextInt();
            int[] fibonacciSequence = fib(n);
            System.out.println(Arrays.toString(fib(n)));
        }

        public static int[] fib(int n) {
            if (n <= 0) {
                return new int[0];
            } else if (n == 1) {
                return new int[]{0};
            } else if (n == 2) {
                return new int[]{0, 1};
            }
            int[] sequence = new int[n];
            sequence[0] = 0;
            sequence[1] = 1;
            for (int i = 2; i < n; i++) {
                sequence[i] = sequence[i - 1] + sequence[i - 2];
            }
            return sequence;
        }
}
