<p align="center">
  <img src="G7-async-completablefuture.png" alt="G7-async-completablefuture-Java" width="100%">
</p>

# Java Async CompletableFuture

A simple Java project that demonstrates the difference between **blocking sequential execution** and **asynchronous concurrent execution** using `CompletableFuture`.

The project processes three independent report-generation tasks and compares how execution time changes when those tasks are performed sequentially versus asynchronously.

## Overview

Applications frequently perform operations that spend significant time waiting, such as:

* External service calls
* File operations
* Remote queries
* Network requests
* Other I/O-bound or waiting operations

When independent operations are executed sequentially, each task must finish before the next one starts.

This project demonstrates how Java's `CompletableFuture` can be used to start independent tasks asynchronously, wait for them as a group, and reduce the overall elapsed time.

## Project Objective

The objective is to demonstrate:

1. The cost of executing independent slow operations sequentially.
2. How asynchronous execution allows several independent tasks to progress concurrently.
3. How `CompletableFuture.supplyAsync()` can be used to start asynchronous computations.
4. How `CompletableFuture.allOf()` can coordinate multiple asynchronous operations.
5. How `join()` can retrieve their results after completion.
6. Why independent waiting operations should not necessarily be processed one after another.

## Project Structure

```text
java-async-completablefuture/
└── src/
    ├── BlockingProblem.java
    └── NonBlockingSolution.java
```

The project contains two implementations of the same report-processing scenario.

## 1. BlockingProblem

`BlockingProblem` demonstrates sequential execution.

Three independent reports are generated:

```java
String report1 = service.generateReport("Reporte-1");
String report2 = service.generateReport("Reporte-2");
String report3 = service.generateReport("Reporte-3");
```

Each report simulates a slow operation using:

```java
Thread.sleep(1000);
```

Because each method invocation is executed synchronously, the next report does not begin until the previous one finishes.

### Complete implementation

```java
public class BlockingProblem {

    static class ReportService {

        public String generateReport(String reportName) {
            System.out.println("Iniciando " + reportName);

            try {
                // Simula una operación lenta: llamada externa, archivo, espera, etc.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return reportName + " completado";
        }
    }

    public static void main(String[] args) {

        ReportService service = new ReportService();

        long start = System.currentTimeMillis();

        String report1 = service.generateReport("Reporte-1");
        String report2 = service.generateReport("Reporte-2");
        String report3 = service.generateReport("Reporte-3");

        System.out.println(report1);
        System.out.println(report2);
        System.out.println(report3);

        long end = System.currentTimeMillis();

        System.out.println(
            "Tiempo total aproximado: " + (end - start) + " ms"
        );
    }
}
```

### Execution behavior

Conceptually:

```text
Main Thread
    |
    |---- Reporte-1 ----|
                        |---- Reporte-2 ----|
                                            |---- Reporte-3 ----|
```

Each task takes approximately one second.

Therefore:

```text
Reporte-1 ≈ 1 second
Reporte-2 ≈ 1 second
Reporte-3 ≈ 1 second
-------------------------
Total     ≈ 3 seconds
```

The reports are independent, but they are processed as though each one depended on the previous result.

## 2. Asynchronous Solution

The second implementation uses Java `CompletableFuture`.

Instead of waiting for one report before starting another, all three tasks are submitted asynchronously:

```java
CompletableFuture<String> report1 =
        CompletableFuture.supplyAsync(
            () -> service.generateReport("Reporte-1")
        );

CompletableFuture<String> report2 =
        CompletableFuture.supplyAsync(
            () -> service.generateReport("Reporte-2")
        );

CompletableFuture<String> report3 =
        CompletableFuture.supplyAsync(
            () -> service.generateReport("Reporte-3")
        );
```

The application then creates a synchronization point:

```java
CompletableFuture<Void> allReports =
        CompletableFuture.allOf(
            report1,
            report2,
            report3
        );
```

And waits until all report operations finish:

```java
allReports.join();
```

The individual results are then obtained with:

```java
System.out.println(report1.join());
System.out.println(report2.join());
System.out.println(report3.join());
```

## Complete Asynchronous Implementation

```java
import java.util.concurrent.CompletableFuture;

public class NonBlockingSolution {

    static class ReportService {

        public String generateReport(String reportName) {

            System.out.println(
                "Iniciando " + reportName +
                " en " + Thread.currentThread().getName()
            );

            try {
                // Simula una operación lenta: llamada externa, archivo, espera, etc.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return reportName + " completado";
        }
    }

    public static void main(String[] args) {

        ReportService service = new ReportService();

        long start = System.currentTimeMillis();

        CompletableFuture<String> report1 =
                CompletableFuture.supplyAsync(
                    () -> service.generateReport("Reporte-1")
                );

        CompletableFuture<String> report2 =
                CompletableFuture.supplyAsync(
                    () -> service.generateReport("Reporte-2")
                );

        CompletableFuture<String> report3 =
                CompletableFuture.supplyAsync(
                    () -> service.generateReport("Reporte-3")
                );

        CompletableFuture<Void> allReports =
                CompletableFuture.allOf(
                    report1,
                    report2,
                    report3
                );

        allReports.join();

        System.out.println(report1.join());
        System.out.println(report2.join());
        System.out.println(report3.join());

        long end = System.currentTimeMillis();

        System.out.println(
            "Tiempo total aproximado: " + (end - start) + " ms"
        );
    }
}
```

## Execution Behavior

Conceptually, the asynchronous version behaves like this:

```text
                 ┌── Reporte-1 ──┐
Main Thread ─────┼── Reporte-2 ──┼──── allOf() ──── results
                 └── Reporte-3 ──┘
```

The three independent tasks can progress concurrently instead of being started sequentially.

If each task takes approximately one second:

```text
Reporte-1 ────────── ≈ 1 second
Reporte-2 ────────── ≈ 1 second
Reporte-3 ────────── ≈ 1 second
                     -----------
Elapsed time          ≈ 1 second
```

The exact execution time depends on the runtime environment and available resources.

## Blocking vs. Asynchronous Execution

| Characteristic                     | BlockingProblem     | NonBlockingSolution |
| ---------------------------------- | ------------------- | ------------------- |
| Execution model                    | Sequential          | Asynchronous        |
| Independent tasks started together | No                  | Yes                 |
| Main API                           | Direct method calls | `CompletableFuture` |
| Task creation                      | Synchronous calls   | `supplyAsync()`     |
| Coordination                       | Sequential flow     | `allOf()`           |
| Result retrieval                   | Direct return value | `join()`            |
| Approximate elapsed time           | ~3 seconds          | ~1 second           |
| Tasks                              | 3 reports           | 3 reports           |
| Simulated delay per task           | 1 second            | 1 second            |

## How CompletableFuture Changes the Flow

### Sequential approach

```java
String report1 = service.generateReport("Reporte-1");
String report2 = service.generateReport("Reporte-2");
String report3 = service.generateReport("Reporte-3");
```

The execution is effectively:

```text
Task 1 → wait → Task 2 → wait → Task 3 → wait
```

### Asynchronous approach

```java
CompletableFuture.supplyAsync(...);
CompletableFuture.supplyAsync(...);
CompletableFuture.supplyAsync(...);
```

The execution becomes conceptually:

```text
          ┌→ Task 1 ─┐
Start ────├→ Task 2 ─┼→ Wait for all → Results
          └→ Task 3 ─┘
```

The application does not need to wait for one independent report before submitting the next one.

## `CompletableFuture.supplyAsync()`

The project uses:

```java
CompletableFuture.supplyAsync(...)
```

because `generateReport()` returns a value:

```java
String
```

Each asynchronous computation therefore produces a:

```java
CompletableFuture<String>
```

Example:

```java
CompletableFuture<String> report1 =
        CompletableFuture.supplyAsync(
            () -> service.generateReport("Reporte-1")
        );
```

## `CompletableFuture.allOf()`

The project coordinates the three asynchronous operations using:

```java
CompletableFuture.allOf(
    report1,
    report2,
    report3
);
```

`allOf()` represents the completion of the complete group of tasks.

The project then uses:

```java
allReports.join();
```

as the synchronization point before reading the individual results.

## `join()`

After all tasks have completed, their values are obtained with:

```java
report1.join();
report2.join();
report3.join();
```

The synchronization therefore occurs after the three independent tasks have already been started.

## Important Technical Note

This project demonstrates **asynchronous task execution**, but the simulated operation itself is still blocking:

```java
Thread.sleep(1000);
```

`CompletableFuture.supplyAsync()` moves that work to asynchronous execution, but it does not transform `Thread.sleep()` into a non-blocking operation.

Therefore, the most technically accurate interpretation of this example is:

> Do not block the caller by processing independent waiting tasks sequentially. Submit independent work asynchronously and coordinate the results when they are needed.

A production system performing truly non-blocking I/O would require APIs whose underlying I/O operations are themselves non-blocking.

## Why This Matters

Consider a service that needs information from several independent systems:

```text
Request
   |
   +---- Customer Service
   |
   +---- Account Service
   |
   +---- Transaction Service
```

If every call takes one second and the calls are made sequentially:

```text
Total ≈ 3 seconds
```

If the operations are independent and can be started concurrently:

```text
Total ≈ slowest operation
```

This execution model can significantly reduce end-to-end latency for workloads dominated by independent waiting operations.

## Resource Considerations

Asynchronous programming does not mean creating unlimited work.

The purpose is not simply to create more threads.

The objective is to structure independent operations so the application does not unnecessarily process every waiting operation sequentially.

When applying this pattern in larger systems, concurrency must still be controlled according to the resources available to the application.

## Requirements

The example uses only Java standard-library functionality.

Main API:

```java
java.util.concurrent.CompletableFuture
```

No external framework, database, HTTP service, or third-party library is required by the example.

## Running the Project

Compile the classes:

```bash
javac src/BlockingProblem.java
javac src/NonBlockingSolution.java
```

Run the blocking example:

```bash
java -cp src BlockingProblem
```

Run the asynchronous example:

```bash
java -cp src NonBlockingSolution
```

## Expected Result

### Blocking version

The three operations are executed one after another.

Expected elapsed time:

```text
≈ 3000 ms
```

### Asynchronous version

The three operations are started asynchronously.

Expected elapsed time:

```text
≈ 1000 ms
```

Actual values may vary depending on the machine and runtime environment.

## Key Concepts

This project demonstrates:

* Asynchronous programming
* Blocking execution
* Sequential processing
* Concurrent task execution
* `CompletableFuture`
* `supplyAsync()`
* `allOf()`
* `join()`
* Independent tasks
* Thread utilization
* Latency reduction
* Task coordination

## Key Takeaway

> Independent slow tasks do not need to be executed sequentially.

When several operations can execute independently, Java's `CompletableFuture` provides a mechanism for starting them asynchronously and coordinating their results afterward.

The important architectural principle is not simply **“use more threads.”**

It is:

> **Avoid unnecessary sequential waiting when independent work can progress concurrently.**

## Autor

**Work Order IT**  
Soluciones tecnológicas, arquitectura de software y formación técnica para equipos de desarrollo.

Este repositorio forma parte de una iniciativa educativa orientada a explicar cómo la concurrencia en **Python 3.13** puede acelerar un sistema o volverlo impredecible cuando el estado compartido no se controla correctamente.

Website: [www.workorder-it.net](https://www.workorder-it.net)
