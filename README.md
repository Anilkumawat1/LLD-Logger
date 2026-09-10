# 🪵 LLD — Logging System (Java)

> A production-grade, extensible logging framework built from scratch in Java — featuring async logging, log rotation, pluggable formatters, and level-based filtering.

---

## 📋 Table of Contents

- [Functional Requirements](#-functional-requirements)
- [Non-Functional Requirements](#-non-functional-requirements)
- [Class Diagram](#-class-diagram)
- [Design Patterns Used & Why](#-design-patterns-used--why)
- [OOP Concepts Applied](#-oop-concepts-applied)
- [System Flow — How It Works](#-system-flow--how-it-works)
- [Project Structure](#-project-structure)
- [Code Output](#-code-output)
- [Future Extensibility](#-future-extensibility)
- [How to Run](#-how-to-run)

---

## ✅ Functional Requirements

| # | Requirement |
|---|-------------|
| FR-1 | Support multiple log levels: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL` |
| FR-2 | Allow configuring a **global minimum log level** — events below it are dropped |
| FR-3 | Support **multiple appenders** simultaneously (Console, File, Async) |
| FR-4 | Each appender must support its own **filter chain** (e.g., only ERROR+ to console) |
| FR-5 | Support **multiple output formats**: Pattern (human-readable) and JSON (structured) |
| FR-6 | Support **formatted messages** using `String.format`-style placeholders |
| FR-7 | Support **exception logging** with full stack trace |
| FR-8 | Provide **asynchronous logging** via a dedicated worker thread and bounded queue |
| FR-9 | Support **log file rotation** by file size and/or elapsed time |
| FR-10 | Maintain a configurable number of **backup/rolled log files** |
| FR-11 | Allow multiple **named logger instances** per class/service |
| FR-12 | Support **multi-threaded logging** safely without data corruption |

---

## ⚡ Non-Functional Requirements

| # | Requirement | Detail |
|---|-------------|--------|
| NFR-1 | **Thread-Safety** | `FileAppender.append()` is `synchronized`; `LogManager.loggerConfig` is `volatile`; `LoggerFactory` uses `ConcurrentHashMap` |
| NFR-2 | **Low Latency** | `AsyncAppender` decouples caller thread from I/O via a bounded `LinkedBlockingQueue`; falls back to sync only when queue is full |
| NFR-3 | **Extensibility** | New formatters, filters, appenders, and rotation strategies can be added without modifying existing code (Open/Closed Principle) |
| NFR-4 | **Zero External Dependencies** | Pure Java — no third-party logging libraries |
| NFR-5 | **Graceful Shutdown** | `AsyncAppender.close()` drains remaining queued events before stopping the worker thread |
| NFR-6 | **Configurability** | All behaviour (level, appenders, formatters, rotation) is wired at startup via a fluent Builder API |
| NFR-7 | **Bounded Memory** | Async queue is bounded (`queueSize`) preventing unbounded memory growth under heavy load |
| NFR-8 | **Durability** | `FileAppender` uses `BufferedWriter.flush()` after every write ensuring no silent data loss |

---

## 📐 Class Diagram

```mermaid
classDiagram
    direction TB

    class Logger {
        <<interface>>
        +trace(message, args)
        +debug(message, args)
        +info(message, args)
        +warn(message, args)
        +error(message, args)
        +error(message, throwable, args)
        +fatal(message, args)
    }

    class LoggerFactory {
        -loggerMap ConcurrentMap
        -LoggerFactory()
        +getLogger(clazz) Logger
    }

    class SimpleLogger {
        -loggerName String
        -loggerConfig LoggerConfig
        +SimpleLogger(name, config)
        -log(level, message, throwable, args)
    }

    class LogManager {
        -INSTANCE LogManager
        -loggerConfig LoggerConfig
        -LogManager()
        +getInstance() LogManager
        +log(logEvent)
        +getLoggerConfig() LoggerConfig
        +setLoggerConfig(config)
    }

    class LogEvent {
        -level LogLevel
        -message String
        -timestamp Instant
        -throwable Throwable
        -loggerName String
        -threadName String
        +getLevel() LogLevel
        +getMessage() String
        +getTimestamp() Instant
        +getThrowable() Throwable
        +getLoggerName() String
        +getThreadName() String
    }

    class LoggerConfig {
        -logLevel LogLevel
        -appenders List
        +getLogLevel() LogLevel
        +getAppenders() List
        +builder() Builder
    }

    class Builder {
        -logLevel LogLevel
        -appenders List
        +setLogLevel(level) Builder
        +addAppender(appender) Builder
        +build() LoggerConfig
    }

    class LogLevel {
        <<enumeration>>
        TRACE
        DEBUG
        INFO
        WARN
        ERROR
        FATAL
        -priority int
        +isEnabled(configuredLevel) boolean
    }

    class Appender {
        <<interface>>
        +append(logEvent)
        +close()
    }

    class AbstractAppender {
        <<abstract>>
        #formatter Formatter
        #filters List
        #shouldAppend(event) boolean
    }

    class ConsoleAppender {
        +append(logEvent)
        +close()
    }

    class FileAppender {
        -filePath Path
        -rotationManager RotationManager
        -maxBackupFiles int
        -writer BufferedWriter
        +append(logEvent)
        -rotate()
        +close()
    }

    class AsyncAppender {
        -delegate Appender
        -queue BlockingQueue
        -worker Thread
        -running boolean
        +append(logEvent)
        -processLogs()
        +close()
    }

    class Formatter {
        <<interface>>
        +format(event) String
    }

    class PatternFormatter {
        +format(event) String
        -getStackTrace(throwable) String
    }

    class JsonFormatter {
        +format(event) String
        -escape(value) String
        -getStackTrace(throwable) String
    }

    class Filter {
        <<interface>>
        +accept(logEvent) boolean
    }

    class LevelFilter {
        -level LogLevel
        +accept(logEvent) boolean
    }

    class RotationStrategy {
        <<interface>>
        +shouldRotate(currentFileSize, formattedMessage) boolean
    }

    class RotationManager {
        -strategies List
        +shouldRotate(currentFileSize, formattedMessage) boolean
    }

    class SizeRotationStrategy {
        -maxFileSize long
        +shouldRotate(currentFileSize, formattedMessage) boolean
    }

    class TimeRotationStrategy {
        -rotationIntervalMillis long
        -lastRotationTime long
        +shouldRotate(currentFileSize, formattedMessage) boolean
    }

    Logger <|.. SimpleLogger
    LoggerFactory ..> SimpleLogger : creates
    LoggerFactory ..> LogManager : uses
    SimpleLogger ..> LogManager : dispatches via
    SimpleLogger ..> LogEvent : creates
    SimpleLogger --> LoggerConfig : holds ref
    LogManager --> LoggerConfig : manages
    LogManager --> Appender : iterates
    LoggerConfig --> LogLevel
    LoggerConfig --> Appender
    LoggerConfig +-- Builder
    Appender <|.. AbstractAppender
    AbstractAppender <|-- ConsoleAppender
    AbstractAppender <|-- FileAppender
    Appender <|.. AsyncAppender
    AsyncAppender --> Appender : delegates to
    AbstractAppender --> Formatter
    AbstractAppender --> Filter
    Formatter <|.. PatternFormatter
    Formatter <|.. JsonFormatter
    Filter <|.. LevelFilter
    LevelFilter --> LogLevel
    FileAppender --> RotationManager
    RotationManager --> RotationStrategy
    RotationStrategy <|.. SizeRotationStrategy
    RotationStrategy <|.. TimeRotationStrategy
```

---

## 🎨 Design Patterns Used & Why

### 1. Singleton — `LogManager`

```java
private static final LogManager INSTANCE = new LogManager();
public static LogManager getInstance() { return INSTANCE; }
```

**Why?** There must be exactly one central dispatch point for all log events. A singleton ensures a single shared configuration and single coordination point across the entire JVM, regardless of how many `Logger` instances exist.

---

### 2. Factory — `LoggerFactory`

```java
public static Logger getLogger(Class<?> clazz) {
    return loggerMap.computeIfAbsent(clazz.getName(),
        name -> new SimpleLogger(name, LogManager.getInstance().getLoggerConfig()));
}
```

**Why?** Callers should not instantiate `SimpleLogger` directly. The factory hides construction complexity, caches loggers per class name to avoid redundant object creation, and keeps the API clean (`LoggerFactory.getLogger(MyClass.class)`).

---

### 3. Builder — `LoggerConfig.Builder`

```java
LoggerConfig config = LoggerConfig.builder()
    .setLogLevel(LogLevel.INFO)
    .addAppender(consoleAppender)
    .addAppender(fileAppender)
    .build();
```

**Why?** `LoggerConfig` has multiple optional parameters. The Builder pattern provides a fluent, readable configuration API, prevents partially-constructed objects, and enforces validation at `build()` time (e.g., `logLevel` must be set).

---

### 4. Strategy — `RotationStrategy` / `Formatter` / `Filter`

```java
public interface RotationStrategy {
    boolean shouldRotate(long currentFileSize, String formattedMessage);
}

public interface Formatter {
    String format(LogEvent event);
}

public interface Filter {
    boolean accept(LogEvent logEvent);
}
```

**Why?** Each represents a *behaviour* that varies independently. New strategies can be plugged in without modifying `FileAppender`, `AbstractAppender`, or any existing code — perfect Open/Closed compliance.

---

### 5. Decorator — `AsyncAppender`

```java
public AsyncAppender(Appender delegate, int queueSize) {
    this.delegate = delegate; // wraps any Appender
}
```

**Why?** `AsyncAppender` wraps *any* `Appender` and adds asynchronous behaviour transparently. The caller uses the same `Appender` interface — the async buffering is a hidden extra layer, classic Decorator pattern.

---

### 6. Template Method — `AbstractAppender`

```java
public abstract class AbstractAppender implements Appender {
    protected boolean shouldAppend(LogEvent event) {
        for (Filter filter : filters) {
            if (!filter.accept(event)) return false;
        }
        return true;
    }
}
```

**Why?** The filter-evaluation algorithm is identical for all appenders. `AbstractAppender` defines the template (`shouldAppend`), and subclasses call it before writing. This eliminates code duplication while allowing subclasses to define the actual write behaviour.

---

### 7. Composite — `RotationManager`

```java
public class RotationManager {
    private final List<RotationStrategy> strategies;
    public boolean shouldRotate(long size, String msg) {
        for (RotationStrategy strategy : strategies) {
            if (strategy.shouldRotate(size, msg)) return true;
        }
        return false;
    }
}
```

**Why?** Multiple rotation strategies (size + time) are composed into one decision-maker. `FileAppender` talks only to `RotationManager` — it doesn't know how many strategies are active. New strategies can be added without changing `FileAppender`.

---

## 🧩 OOP Concepts Applied

| Concept | Where & How |
|---------|-------------|
| **Abstraction** | `Logger`, `Appender`, `Formatter`, `Filter`, `RotationStrategy` are interfaces — callers depend on contracts, never implementations |
| **Encapsulation** | `LoggerConfig` is `final` with private constructor; fields exposed only via getters; `LoggerFactory` has a private constructor |
| **Inheritance** | `ConsoleAppender` and `FileAppender` both extend `AbstractAppender`, inheriting filter-chain logic |
| **Polymorphism** | `LogManager` holds `List<Appender>` and calls `appender.append(event)` — at runtime dispatches to `ConsoleAppender`, `FileAppender`, or `AsyncAppender` transparently |
| **Interface Segregation** | Each interface is minimal: `Appender` has 2 methods, `Filter` has 1, `Formatter` has 1 — no fat interfaces |
| **Composition over Inheritance** | `AsyncAppender` composes an `Appender` delegate instead of inheriting; `AbstractAppender` composes a `Formatter` and `List<Filter>` |
| **Immutability** | `LogEvent` is fully immutable (all fields final); `LoggerConfig.appenders` uses `List.copyOf()` |
| **Enum with Behaviour** | `LogLevel` carries its own `isEnabled()` comparison logic using `priority` values |

---

## 🔄 System Flow — How It Works

```
Application Code
      │
      │  log.info("User %s logged in", "Anil")
      ▼
┌─────────────────────────────────────────────┐
│              SimpleLogger                   │
│  1. Check: level.isEnabled(configuredLevel) │
│  2. Format: String.format(message, args)    │
│  3. Create: new LogEvent(...)               │
│  4. Dispatch: LogManager.getInstance().log()│
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│               LogManager (Singleton)        │
│  Iterates over all configured Appenders     │
│  and calls appender.append(logEvent)        │
└──────┬─────────────────┬────────────────────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────────────────────┐
│ConsoleAppender│  │        AsyncAppender          │
│              │  │  queue.offer(event)            │
│ 1. Filter?   │  │  (non-blocking enqueue)        │
│ 2. Format    │  │                                │
│ 3. println   │  │   Virtual Thread Worker        │
└──────────────┘  │   queue.take() --> delegate    │
                  └──────────────────────────────┘
                                │
                                ▼
                  ┌──────────────────────────────┐
                  │        FileAppender           │
                  │  1. Filter check              │
                  │  2. Format message            │
                  │  3. Ask RotationManager       │
                  │     ├─ SizeRotationStrategy   │
                  │     └─ TimeRotationStrategy   │
                  │  4. rotate() if needed        │
                  │     └─ shift .1 > .2 > .3     │
                  │        app.log > app.log.1    │
                  │  5. writer.write() + flush()  │
                  └──────────────────────────────┘
```

### Async Queue Behaviour

```
                     ┌──────────────┐
Calling Thread ─────►│ Bounded Queue│◄──── Worker Thread (drains queue)
                     │  (10,000)    │           │
                     └──────────────┘           ▼
                          │ full?         FileAppender
                          │ yes
                          ▼
                   Synchronous fallback
                   (direct delegate call)
```

### Log Rotation Flow

```
Before rotation (maxBackupFiles = 3):

  application.log        ← current (being written)
  application.log.1      ← most recent backup
  application.log.2
  application.log.3      ← oldest backup

After rotation trigger (size OR time threshold hit):

  application.log.3  →  DELETED
  application.log.2  →  application.log.3
  application.log.1  →  application.log.2
  application.log    →  application.log.1
  application.log    ←  new empty file (fresh writer opened)
```

---

## 📁 Project Structure

```
LLD-Logger/
├── src/
│   ├── Main.java                          # Entry point & full demo
│   │
│   ├── api/
│   │   ├── Logger.java                    # Public logger interface
│   │   └── LoggerFactory.java             # Factory + logger cache (ConcurrentHashMap)
│   │
│   ├── config/
│   │   └── LoggerConfig.java              # Immutable config + inner Builder
│   │
│   ├── core/
│   │   ├── LogEvent.java                  # Immutable log event model
│   │   ├── LogManager.java                # Singleton dispatcher
│   │   ├── SimpleLogger.java              # Logger interface implementation
│   │   │
│   │   ├── appender/
│   │   │   ├── Appender.java              # Output destination interface
│   │   │   ├── AbstractAppender.java      # Base class: filter-chain logic
│   │   │   ├── ConsoleAppender.java       # Writes to stdout
│   │   │   ├── FileAppender.java          # File I/O with rotation support
│   │   │   └── AsyncAppender.java         # Decorator: async buffering via queue
│   │   │
│   │   ├── filter/
│   │   │   ├── Filter.java                # Filter interface
│   │   │   └── LevelFilter.java           # Minimum-level filter
│   │   │
│   │   ├── formatter/
│   │   │   ├── Formatter.java             # Formatter interface
│   │   │   ├── PatternFormatter.java      # Human-readable text format
│   │   │   └── JsonFormatter.java         # Structured JSON format
│   │   │
│   │   └── rotation/
│   │       ├── RotationStrategy.java      # Rotation decision interface
│   │       ├── RotationManager.java       # Composite: OR logic over strategies
│   │       ├── SizeRotationStrategy.java  # Rotate when file exceeds size limit
│   │       └── TimeRotationStrategy.java  # Rotate after time interval
│   │
│   └── level/
│       └── LogLevel.java                  # Enum: TRACE → FATAL with priority
│
├── application.log                        # Active JSON log file
├── application.log.1                      # Rotated backup #1
├── application.log.2                      # Rotated backup #2
├── application.log.3                      # Rotated backup #3
├── async.log                              # Active Pattern-format async log
├── async.log.1                            # Rotated async backup #1
├── async.log.2                            # Rotated async backup #2
└── async.log.3                            # Rotated async backup #3
```

---

## 💻 Code Output

### Console Output

> Only `ERROR` and `FATAL` reach the console — the `ConsoleAppender` has a `LevelFilter(ERROR)`.

```
2026-09-08T19:25:43.123Z [ERROR] Main - This is ERROR
2026-09-08T19:25:43.124Z [FATAL] Main - This is FATAL
2026-09-08T19:25:43.125Z [ERROR] Main - ERROR filtering test
2026-09-08T19:25:43.126Z [FATAL] Main - FATAL filtering test
2026-09-08T19:25:43.127Z [ERROR] Main - Division failed
java.lang.ArithmeticException: / by zero
    at Main.main(Main.java:217)
2026-09-08T19:25:43.128Z [ERROR] Main - Operation failed for user Anil: null
java.lang.NullPointerException
    at Main.main(Main.java:238)
2026-09-08T19:25:43.129Z [ERROR] Main - Array operation failed
java.lang.ArrayIndexOutOfBoundsException: Index 10 out of bounds for length 3
    at Main.main(Main.java:261)
2026-09-08T19:25:43.130Z [ERROR] java.lang.String - Payment failed paymentId=7
2026-09-08T19:25:43.131Z [ERROR] java.lang.String - Payment failed paymentId=14
2026-09-08T19:25:43.132Z [ERROR] Main - Testing final error message
2026-09-08T19:25:43.133Z [FATAL] Main - Application shutting down
====================================
Logging test completed!
Check:
  application.log       application.log.1
  async.log             async.log.1
====================================
```

### `application.log` — JSON Format

```json
{"timestamp":"2026-09-08T19:26:48.566976Z","level":"INFO","logger":"java.lang.String","thread":"pool-1-thread-1","message":"Thread=1 processing task=950"}
{"timestamp":"2026-09-08T19:26:48.567334Z","level":"WARN","logger":"java.lang.String","thread":"pool-1-thread-3","message":"Thread=3 reached task=200"}
```

**Exception event in JSON:**
```json
{
  "timestamp": "2026-09-08T19:25:43.127Z",
  "level": "ERROR",
  "logger": "Main",
  "thread": "main",
  "message": "Division failed",
  "exception": {
    "type": "java.lang.ArithmeticException",
    "message": "/ by zero",
    "stackTrace": "java.lang.ArithmeticException: / by zero\n\tat Main.main(Main.java:217)\n"
  }
}
```

### `async.log` — Pattern Format

```
2026-09-08T19:26:48.566575Z [INFO] java.lang.String - Thread=1 processing task=924
2026-09-08T19:26:48.566585Z [INFO] java.lang.String - Thread=1 processing task=925
2026-09-08T19:26:48.569001Z [WARN] java.lang.String - Thread=5 reached task=200
```

### Rotated Files After Run

```
-rw-r--r--  application.log      8.0 KB   (current)
-rw-r--r--  application.log.1   10.0 KB   (latest backup)
-rw-r--r--  application.log.2   10.0 KB
-rw-r--r--  application.log.3   10.0 KB   (oldest — deleted on next rotation)
```

---

## 🚀 Future Extensibility

The architecture is open for extension without modifying existing classes (OCP):

| Extension Point | How to Add |
|----------------|------------|
| **New Appender** | Implement `Appender` or extend `AbstractAppender` — e.g., `DatabaseAppender`, `ElasticsearchAppender`, `SlackAppender` |
| **New Formatter** | Implement `Formatter` — e.g., `XmlFormatter`, `CsvFormatter`, `ProtobufFormatter` |
| **New Filter** | Implement `Filter` — e.g., `RegexFilter`, `MDCFilter`, `ThreadFilter`, `ThrottleFilter` |
| **New Rotation Strategy** | Implement `RotationStrategy` — e.g., `DailyRotationStrategy`, `HourlyRotationStrategy` |
| **Per-Logger Config** | `SimpleLogger` already holds its own `LoggerConfig` ref — scope configs per logger name |
| **Log Sampling** | Add a `SamplingFilter` that accepts only 1-in-N events for high-volume metric logs |
| **MDC Support** | Add thread-local `MDC` map; include in `LogEvent`; update formatters to include MDC fields |
| **Remote Logging** | Implement `Appender` to ship events over HTTP/TCP to log aggregators (Loki, Fluentd, etc.) |
| **Hot Reload Config** | `LogManager.setLoggerConfig()` already accepts new config at runtime — wrap it in a file watcher |
| **Metrics / Counters** | Add a `MetricsAppender` that counts events per level and exposes Prometheus metrics |

---

## ▶️ How to Run

### Prerequisites

- **Java 21+** — uses Virtual Threads (`Thread.ofVirtual()` in `AsyncAppender`)
- No Maven / Gradle needed — pure Java

### Step 1 — Compile

```bash
# From the project root directory
javac -sourcepath src -d out src/Main.java
```

### Step 2 — Run

```bash
java -cp out Main
```

### Step 3 — Observe Output

```bash
# Console will show only ERROR + FATAL

# Inspect log files:
cat application.log      # JSON format  — INFO and above
cat async.log            # Pattern format — async writer

# Check rotated backups:
ls -lh application.log*
ls -lh async.log*
```

### Configuration Used in Demo (`Main.java`)

| Setting | Value |
|---------|-------|
| Global log level | `INFO` |
| Console filter | `ERROR` and above only |
| `application.log` | `INFO`+, JSON format, rotates at **10 KB** or **30 seconds** |
| `async.log` | `INFO`+, Pattern format, async queue size **10,000** |
| Max backup files | `3` per log file |

---

> **Author:** Anil Kumawat | **Domain:** Low-Level Design (LLD) | **Language:** Java 21
>
> **Patterns:** Singleton · Factory · Builder · Strategy · Decorator · Template Method · Composite
