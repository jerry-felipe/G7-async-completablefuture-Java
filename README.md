<p align="center">
  <img src="G7-async-completablefuture.png" alt="G7-async-completablefuture-Java" width="100%">
</p>

# Java Async CompletableFuture

A simple Java project that demonstrates the difference between **blocking sequential execution** and **asynchronous concurrent execution** using `CompletableFuture`.

The project processes three independent report-generation tasks and compares how execution time changes when those tasks are performed sequentially versus asynchronously.

It then presents two additional flavors of the same solution using the most recent Java concurrency features: **virtual threads** and **structured concurrency**.

## Overview

Applications frequently perform operations that spend significant time waiting, such as:

* External service calls
* File operations
* Remote queries
* Network requests
* Other I/O-bound or waiting operations

When independent operations are executed sequentially, each task must finish before the next one starts.

This project demonstrates how Java's `CompletableFuture` can be used to start independent tasks asynchronously, wait for them as a group, and reduce the overall elapsed time.

It also shows how the same result can be achieved with **virtual threads** (Project Loom) and with **structured concurrency** (`StructuredTaskScope`), two newer approaches that keep the code readable while running tasks concurrently.

## Project Objective

The objective is to demonstrate:

1. The cost of executing independent slow operations sequentially.
2. How asynchronous execution allows several independent tasks to progress concurrently.
3. How `CompletableFuture.supplyAsync()` can be used to start asynchronous computations.
4. How `CompletableFuture.allOf()` can coordinate multiple asynchronous operations.
5. How `join()` can retrieve their results after completion.
6. Why independent waiting operations should not necessarily be processed one after another.
7. How **virtual threads** allow blocking-style code to run concurrently at very low cost.
8. How **structured concurrency** binds the lifetime of concurrent tasks to a code block and handles failure and cancellation automatically.

## Project Structure

```text
G7-async-completablefuture-Java/
├── pom.xml
└── src/
    └── main/
        └── java/
            ├── BlockingProblem.java
            ├── NonBlockingSolution.java
            ├── VirtualThreadsSolution.java
            └── StructuredConcurrencySolution.java
```

The project is a standard Maven project and contains four implementations of the same report-processing scenario:

1. `BlockingProblem` – sequential execution (the problem).
2. `NonBlockingSolution` – asynchronous execution with `CompletableFuture`.
3. `VirtualThreadsSolution` – concurrent execution with virtual threads.
4. `StructuredConcurrencySolution` – concurrent execution with structured concurrency.

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

## 3. Virtual Threads Solution

The third implementation uses **virtual threads**, introduced as a final feature in Java 21.

A virtual thread is a lightweight thread managed by the JVM rather than by the operating system. Creating one is so cheap that the application no longer needs to size a thread pool: it can simply create **one thread per task**.

The executor is obtained with:

```java
Executors.newVirtualThreadPerTaskExecutor()
```

Each report is submitted as a task:

```java
List<Future<String>> reports =
        List.of("Reporte-1", "Reporte-2", "Reporte-3")
            .stream()
            .map(name -> executor.submit(
                () -> service.generateReport(name)
            ))
            .toList();
```

The results are then read with a plain blocking call:

```java
for (Future<String> report : reports) {
    System.out.println(report.get());
}
```

The executor is opened in a `try-with-resources` block. When the block ends, the executor is closed and **waits for every submitted task to finish**.

### Complete implementation

```java
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualThreadsSolution {

    static class ReportService {

        public String generateReport(String reportName) {
            System.out.println("Iniciando " + reportName
                    + " en " + Thread.currentThread());
            try {
                // Simula una operación lenta: llamada externa, archivo, espera, etc.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return reportName + " completado";
        }

    }

    public static void main(String[] args) throws Exception {
        ReportService service = new ReportService();
        long start = System.currentTimeMillis();

        // Un hilo virtual por tarea: son tan baratos que no hace falta pool.
        // El try-with-resources cierra el executor y espera a que todas las
        // tareas terminen (concurrencia con ciclo de vida acotado).
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Future<String>> reports = List.of("Reporte-1", "Reporte-2", "Reporte-3")
                    .stream()
                    .map(name -> executor.submit(() -> service.generateReport(name)))
                    .toList();

            // Código de estilo secuencial/bloqueante, pero la espera es barata:
            // bloquear un hilo virtual no ocupa un hilo del sistema operativo.
            for (Future<String> report : reports) {
                System.out.println(report.get());
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Tiempo total aproximado: " + (end - start) + " ms");
    }

}
```

### Execution behavior

Conceptually:

```text
                 ┌── VirtualThread#1: Reporte-1 ──┐
Main Thread ─────┼── VirtualThread#2: Reporte-2 ──┼──── get() ──── results
                 └── VirtualThread#3: Reporte-3 ──┘
```

Each `Future.get()` call blocks, but blocking a virtual thread is inexpensive: the JVM parks the virtual thread and releases the underlying operating-system thread for other work.

The elapsed time is again approximately one second.

### What changes compared to CompletableFuture

The code reads like the sequential version: submit, then get. There are no callbacks, no `allOf()`, and no composition chain.

The concurrency comes from the runtime, not from the shape of the code.

> Virtual threads make it possible to write simple blocking code that scales like asynchronous code.

## 4. Structured Concurrency Solution

The fourth implementation uses **structured concurrency** through `StructuredTaskScope`.

Structured concurrency treats a group of concurrent tasks as a single unit of work with a well-defined beginning and end: the tasks are started inside a block, and they cannot outlive that block.

The scope is opened with:

```java
try (var scope = StructuredTaskScope.<String>open()) {
    ...
}
```

Each report is started as a subtask:

```java
Subtask<String> report1 = scope.fork(() -> service.generateReport("Reporte-1"));
Subtask<String> report2 = scope.fork(() -> service.generateReport("Reporte-2"));
Subtask<String> report3 = scope.fork(() -> service.generateReport("Reporte-3"));
```

The application then waits for the whole group:

```java
scope.join();
```

And reads the results:

```java
System.out.println(report1.get());
System.out.println(report2.get());
System.out.println(report3.get());
```

### Complete implementation

```java
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

public class StructuredConcurrencySolution {

    static class ReportService {

        public String generateReport(String reportName) {
            System.out.println("Iniciando " + reportName
                    + " en " + Thread.currentThread());
            try {
                // Simula una operación lenta: llamada externa, archivo, espera, etc.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return reportName + " completado";
        }

    }

    public static void main(String[] args) throws Exception {
        ReportService service = new ReportService();
        long start = System.currentTimeMillis();

        // Concurrencia estructurada (API de JDK 25+, preview): las tareas viven
        // dentro del scope, como variables locales dentro de un bloque.
        // open() sin argumentos usa la política "todas deben tener éxito":
        // si una falla, las demás se cancelan y join() lanza la excepción.
        // Nada puede quedar "huérfano" ejecutándose fuera del try.
        try (var scope = StructuredTaskScope.<String>open()) {

            Subtask<String> report1 = scope.fork(() -> service.generateReport("Reporte-1"));
            Subtask<String> report2 = scope.fork(() -> service.generateReport("Reporte-2"));
            Subtask<String> report3 = scope.fork(() -> service.generateReport("Reporte-3"));

            // Espera a todas y propaga el fallo de la primera que falle.
            scope.join();

            System.out.println(report1.get());
            System.out.println(report2.get());
            System.out.println(report3.get());
        }

        long end = System.currentTimeMillis();
        System.out.println("Tiempo total aproximado: " + (end - start) + " ms");
    }

}
```

### Execution behavior

Conceptually:

```text
             open()                              close()
               │                                    │
               │   ┌── fork: Reporte-1 ──┐          │
Main Thread ───┼───┼── fork: Reporte-2 ──┼── join() ┼──── results
               │   └── fork: Reporte-3 ──┘          │
               │                                    │
               └──────── scope lifetime ────────────┘
```

The three subtasks run concurrently on virtual threads, and the elapsed time is again approximately one second.

### What changes compared to the previous solutions

`StructuredTaskScope.open()` without arguments applies the policy **all subtasks must succeed**:

* If every subtask completes, `join()` returns and the results can be read.
* If any subtask fails, the remaining subtasks are **cancelled automatically** and `join()` throws the failure.

Neither `CompletableFuture.allOf()` nor the executor-based solution cancels the other tasks when one of them fails.

The `try-with-resources` block guarantees that **no subtask keeps running after the block ends**. Concurrent work therefore has the same lifetime rules as a local variable.

> Structured concurrency makes concurrent code as easy to reason about as sequential code: what starts in a block ends in that block.

### Preview feature note

`StructuredTaskScope` is a **preview API** in current Java releases. Compiling and running it requires:

```text
--enable-preview
```

The project's `pom.xml` already applies this flag during compilation.

## Comparing the Four Flavors

| Characteristic              | BlockingProblem     | NonBlockingSolution    | VirtualThreadsSolution              | StructuredConcurrencySolution |
| --------------------------- | ------------------- | ---------------------- | ----------------------------------- | ----------------------------- |
| Execution model             | Sequential          | Asynchronous           | Concurrent (blocking style)         | Concurrent (structured)       |
| Main API                    | Direct method calls | `CompletableFuture`    | `newVirtualThreadPerTaskExecutor()` | `StructuredTaskScope`         |
| Task creation               | Synchronous calls   | `supplyAsync()`        | `submit()`                          | `fork()`                      |
| Coordination                | Sequential flow     | `allOf()`              | Executor close                      | `join()`                      |
| Result retrieval            | Direct return value | `join()`               | `Future.get()`                      | `Subtask.get()`               |
| Cancels others on failure   | N/A                 | No                     | No                                  | Yes                           |
| Code style                  | Sequential          | Callback / composition | Sequential                          | Sequential                    |
| Task lifetime bound to code | N/A                 | No                     | Yes (executor block)                | Yes (scope block)             |
| Java version                | Any                 | 8+                     | 21+                                 | Preview (`--enable-preview`)  |
| Approximate elapsed time    | ~3 seconds          | ~1 second              | ~1 second                           | ~1 second                     |

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

Main APIs:

```java
java.util.concurrent.CompletableFuture
java.util.concurrent.Executors
java.util.concurrent.StructuredTaskScope
```

No external framework, database, HTTP service, or third-party library is required by the example.

The project is built with **Maven** and targets **Java 26**, because `StructuredTaskScope` is a preview API and Java only allows preview features when the source level matches the JDK in use.

The first three classes work on Java 21 or later; only `StructuredConcurrencySolution` requires the preview flag.

## Running the Project

Compile the project with Maven (requires JDK 26 as `JAVA_HOME`):

```bash
mvn compile
```

Run the blocking example:

```bash
java -cp target/classes BlockingProblem
```

Run the asynchronous example:

```bash
java -cp target/classes NonBlockingSolution
```

Run the virtual threads example:

```bash
java -cp target/classes VirtualThreadsSolution
```

Run the structured concurrency example (preview feature):

```bash
java --enable-preview -cp target/classes StructuredConcurrencySolution
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

### Virtual threads version

The three operations run on three virtual threads.

Expected elapsed time:

```text
≈ 1000 ms
```

### Structured concurrency version

The three operations run as subtasks of a single scope.

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
* Virtual threads
* `newVirtualThreadPerTaskExecutor()`
* Structured concurrency
* `StructuredTaskScope`
* `fork()`
* Independent tasks
* Thread utilization
* Latency reduction
* Task coordination

## Key Takeaway

> Independent slow tasks do not need to be executed sequentially.

When several operations can execute independently, Java's `CompletableFuture` provides a mechanism for starting them asynchronously and coordinating their results afterward.

Virtual threads make the same concurrency available to plain blocking code, and structured concurrency adds clear lifetime, failure, and cancellation rules on top of it.

The important architectural principle is not simply **“use more threads.”**

It is:

> **Avoid unnecessary sequential waiting when independent work can progress concurrently.**

## Autor

**Work Order IT**  
Soluciones tecnológicas, arquitectura de software y formación técnica para equipos de desarrollo.

Este repositorio forma parte de una iniciativa educativa orientada a explicar cómo la concurrencia en **Java 26** puede acelerar un sistema o volverlo impredecible cuando el estado compartido no se controla correctamente.

Website: [www.workorder-it.net](https://www.workorder-it.net)
