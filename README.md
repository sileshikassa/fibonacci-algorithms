# Arbitrary-Precision Fibonacci Computational Engine & Scale Analyzer

A high-performance, multi-threaded Java implementation engineered to calculate and structurally categorize astronomical Fibonacci numbers (validated up to $F_{1,000,000,000}$ and beyond). This project maps a 7-stage architectural optimization journey, progressing from primitive array vulnerabilities into a bit-masked iterative loop combining multi-threaded ForkJoin parallel Karatsuba split-multiplication and base-10 logarithmic scale estimation.

## 🚀 The 7-Stage Evolutionary Roadmap

| Stage | Class Name / Approach | Core Engineering Optimization | Upper Bound / Failure Mode |
| :--- | :--- | :--- | :--- |
| **1** | `FibonacciInteger.java` | Baseline 32-bit primitive loop tracking. | ❌ **Overflows at $F_{48}$** (exceeds $2^{31}-1$, yielding corrupt negative sequences). |
| **2** | `FibonacciLongInteger.java` | Upgrades workspace data width to 64-bit primitive types. | ❌ **Overflows at $F_{93}$** (exceeds $2^{63}-1$). |
| **3** | `FibonacciBigInteger.java` | Transition to arbitrary-precision `BigInteger` object references. | ❌ **Heap OutOfMemoryError (OOM)** at $F_{1,000,000}$ due to massive O(N) sequential array allocations. |
| **4** | `FibonacciBigIntegerLargeScaleName.java` | **Space Optimization**: Removed the tracking array. Used an O(1) scalar pipeline (`a`, `b`, `c`) to capture the single target number. Added custom Latin Short Scale Naming Engine. | ❌ **Time Complexity Bottleneck**: Execution stalled for over 4 hours at N=$10^9$ due to O(N) loop boundaries and a heavy `result.toString().length()` layout-blocking character parsing trap. |
| **5** | `FibonacciBigIntegerLargeScaleNameMoreOptimized.java` | **Algorithmic Leap**: Replaced the linear loop with an $O(\log n)$ **Fast Doubling matrix identity matrix**. Extracted exact digit lengths instantly via high-precision base-10 logarithmic bounds. | ❌ **Single-Thread Bottleneck**: Heaviest matrix multiplications capped onto a single CPU core. Execution required **468,134 ms (~7.8 mins)**. |
| **6** | `FibonacciParallelFastDoubling.java` | **Hardware Saturation**: Configured a **ForkJoinPool** with a custom **Parallel Karatsuba Split-Engine** to segment and route mathematical branches simultaneously. | ⚠️ Finished in **373,035 ms (~6.2 mins)**. Performance restricted by object generation churn from 30 deep recursive stack frame allocations. |
| **7** | `FibonacciIterativeParallel.java` | **Memory-Flattening Bit Loop**: Eliminated recursion completely using a left-to-right iterative loop driven by `Long.highestOneBit`. Minimizes heap structure references to preserve raw internal architecture bandwidth. | 🏆 **Peak Performance: 239,551 ms (~3.99 mins)** on Apple M2 Unified Architecture. |

---

## 🔬 Core Engineering Innovations

### 1. Instant Logarithmic Digit Estimation
To prevent fatal formatting delays caused by converting an arbitrary-precision
binary sequence to an ASCII character array via `toString().length()`, the engine resolves the digit count strictly via a logarithmic derivative of Binet's Formula. Binet’s Formula is a closed-form expression that calculates the nth Fibonacci number directly, without computing all the preceding terms. It uses the golden ratio and produces exact integer results despite involving irrational numbers:

$$F_n \approx \frac{\phi^n}{\sqrt{5}}$$

$$\text{Digits} = \lfloor \log_{10}(F_n) \rfloor + 1 \approx \lfloor n \cdot \log_{10}(\phi) - \log_{10}(\sqrt{5}) \rfloor + 1$$

Using precise tracking parameters ($\log_{10}(\phi) \approx 0.2089876402499787$ and $\log_{10}(\sqrt{5}) \approx 0.3494850021680094$), the engine calculates the scale size of $F_{1,000,000,000}$ (**208,987,640 digits**) in exactly **8 milliseconds**.

### 2. Work-Stealing Parallel Karatsuba Multiplication Pipeline
Java's core `BigInteger.multiply()` operations execute sequentially on a single thread. For astronomical values, the engine intercepts computation steps exceeding a baseline bit constraint and forks them via a thread-stealing `ForkJoinPool`:
```java
ParallelKaratsuba task1 = new ParallelKaratsuba(x1, y1);
ParallelKaratsuba task2 = new ParallelKaratsuba(x0, y0);
task1.fork(); // Asynchronously process high-order bits on available threads
task2.fork(); // Asynchronously process low-order bits on available threads
```

### 3. Re-Engineered Short-Scale Naming Bounds
The Latin short-scale prefix generator handles indices mathematically through remainder parsing blocks. Crucial boundary corrections and array layouts were implemented to ensure strict scale separation:

*   **Hundreds Scale Safety**: Numbers with $\le 3$ digits are locked to `"Hundreds"`, isolating them from higher magnitude groups.
*   **Thousands Scale Guard**: Targets reaching `targetIndex <= 0` map strictly to `"Thousands"`.
*   **Array Realignment**: Pre-indexed `"Thousands"` into index `0` of the `smallIllions` tracking matrix, guaranteeing a flawless, synchronized index handoff straight to `"Million"` (Index 1) and `"Billion"` (Index 2).
*   **0-Based Padding Stability**: Maintained strict empty placeholder elements (`""`) at index `0` of the `UNITS`, `TENS`, and `HUNDREDS` matrices. This maps raw remainder modulo results directly to their Latin counterparts, avoiding scale-shifting alignment bugs.

### 4. Empirical Parallel Threshold Calibration
To maximize multi-core hardware saturation while minimizing thread orchestration overhead on the Apple M2 unified memory architecture, the multiplication pipeline utilizes an empirically calibrated bit-length execution threshold:

*   **The Multithreading Paradox**: Setting the parallel threshold too low (e.g., 25,000 bits) forces the engine to split numbers into millions of microscopic tasks at extreme scales. When computing $F_{1,000,000,000}$ (yielding a 694,241,913-bit sequence), excessive task forks flood the work-stealing queues, wasting CPU clock cycles on queue orchestration, context switching, and heap allocation management.
*   **The 500,000-Bit Sweet Spot**: Performance matrix telemetry under OpenJDK 23 established **500,000 bits** as the optimal boundary for billion-scale calculations. This threshold restricts thread-forking exclusively to massive high-level matrix multiplications, ensuring medium-scale mathematical operations remain on a high-speed sequential path.

### 5. Task Instantiation Churn Reduction (33.3% Heap Optimization)
Standard parallel Karatsuba structures spawn three asynchronous task wrappers (`task1`, `task2`, and `task3`) at every tree split, flooding the garbage collector with short-lived objects. The optimized engine completely eliminates the third task instance object header by routing the cross-product calculation (`(x1+x0)*(y1+y0)`) directly back into the `parallelMultiply` entry router on the parent execution core. This architectural bypass slashes thread pool instantiation allocation by exactly **33.3%** across the tree layout, conserving precious L1/L2 data cache space.

### 6. Non-Blocking Incremental Telemetry Progress
Standard console print functions (`System.out.print`) serve as block synchronization boundaries that halt CPU execution pipelines while drawing characters. To provide real-time calculation visibility without compromising throughput speed, the execution loop dynamically calculates total loop depths from bit masks and drops telemetry markers strictly at matching **10% completion intervals**. This preserves computational flow while maintaining clear terminal visibility.

---

## 📊 Benchmarks & Telemetry ($F_{1,000,000,000}$)

*   **Hardware Baseline**: Apple M2 Silicon (8-Core CPU, 24GB Unified Memory Layout)
*   **Runtime Configuration**: JVM Max Heap specified at `-Xmx16g` / OpenJDK 23.0.1
*   **Core Engagement**: `774.1% CPU Utilization` (Confirming thorough multi-core parallel saturation)
*   **Dynamic Heap Footprint**: `2.51 GB` (Extremely stable allocation via memory-flattened scalars)
*   **Pure Computational Execution Time**: **239,551 ms (~3.99 minutes)** 🚀 *Sub-4-minute milestone cleared*
*   **Metrics & Name Generation**: **6 ms**
*   **Calculated Scale Output**: `Multi-Millinillion Group (Scale index: 69662545-illion)`

---

### 1. Multi-JDK Runtime Matrix
The execution profiles below map the raw impact of upgrading the runtime environment from Java 23 to Java 26 alongside successive JVM tuning stages:

| Environment | JVM Run Configuration Flags | Pure Computation Time | Peak RAM Footprint | System Bottleneck / Behavior |
| :--- | :--- | :--- | :--- | :--- |
| **Java 23** | Default Settings | 239,551 ms (~3.99 mins) | **~2.51 GB - 6 GB** | Clean heap execution; zero storage swap. |
| **Java 26** | Default Settings | 1,104,000 ms (18.4 mins) | Dynamic Allocation | Severe allocation stalling via `MinHeapSize` rules. |
| **Java 26** | `-Xms12g -Xmx16g -XX:+UseG1GC -XX:-UseCompactObjectHeaders` | 266,670 ms (4.44 mins) | **12 GB - 16 GB** | Stable heap allocation; triggered macOS page swapping. |
| **Java 26** | *(See Optimal Config Flags Below)* | **229,050 ms (3.81 mins)** 🏆 | **12 GB - 16 GB** | Native CPU matrix acceleration; overcame swap delay. |

### 2. Isolation of the Java 26 Memory Regression
The dramatic 18.4-minute slowdown encountered on default Java 26 installations stems from structural changes to underlying heap allocation mechanics:

* **The Heap Allocation Trap (`MinHeapSize`):** Java 26 optimizes standard startup overhead by matching the initial heap footprint to the bare minimum threshold (`MinHeapSize`). For an algorithm that rapidly forks and mutates massive `BigInteger` arrays, this forces the JVM to continuously pause execution threads to request memory segment boundary shifts from macOS.
* **Object Lifespan Inflation & Page Swapping:** Java 23 immediately reclaims short-lived mathematical reference structures during parallel Karatsuba splits. Java 26 holds onto these reference arrays slightly longer. Forcing a `-Xms12g -Xmx16g` configuration arena to stabilize the heap starves the Mac's shared 24GB Unified Memory buffer, causing macOS to invoke page swapping to the internal SSD. 

### 3. Conquering Storage Latency via ARM64 Math Intrinsics
The execution time was dropped to a record-breaking **3.81 minutes** by bypassing standard software bytecode loops. By unlocking diagnostic parameters, the JVM forces `java.math.BigInteger` operations to bind directly onto the M2 silicon's native hardware math engines:

* `-XX:+UseMultiplyToLenIntrinsic`: Routes massive array multiplications straight into the ARM64 CPU's assembly-level multiplication registers.
* `-XX:+UseSquareToLenIntrinsic`: Bypasses software routines during the repetitive squaring chains core to Fast Doubling scaling.

The raw computational throughput of native hardware math completely counteracted and outpaced the latency introduced by macOS SSD page swapping.

---

## 🛠️ Optimal JVM Configuration for Extreme Math Workspace

To replicate the accelerated sub-4-minute milestone on Apple Silicon hardware environments under Java 26, execute your application binary using this exact operational profile:

```jvm-option
-Xms12g -Xmx16g -XX:+UseG1GC -XX:-UseCompactObjectHeaders -XX:+UnlockDiagnosticVMOptions -XX:+UseSquareToLenIntrinsic -XX:+UseMultiplyToLenIntrinsic
```

---

## 🧪 Regression Testing Suite
A dependency-free test runner (`FibonacciIterativeParallelTestSuite.java`) is packaged alongside the calculation engine to automatically run regression tests across scale boundaries, prefix constructions, and logarithmic calculations:
```text
Testing Logarithmic Digit Calculator... Passed.
Testing Small '-illion' Boundaries... Passed.
Testing Large Latin Prefixes... Passed.
Testing Extreme Fallback Safe-guards... Passed.

🎉 ALL TESTS PASSED SUCCESSFULLY! [7/7]
```
