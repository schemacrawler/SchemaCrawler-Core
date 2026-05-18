# Reliability Hardening Changes

This document describes the targeted reliability improvements made to
SchemaCrawler-Core.  Each entry covers the affected file, the risk addressed,
the specific change, and why it is the right fix.

---

## 1. `IOUtility.java` — Redundant/null-message double-logging removed

**File:** `schemacrawler-utility/src/main/java/us/fatehi/utility/IOUtility.java`

### Risk

`Throwable.getMessage()` is permitted to return `null` by the Java specification.
Three catch blocks in `copy()`, `readFully()`, and `readResourceFully()` called
`e.getMessage()` twice — once as a bare log message and once as the message
parameter alongside the exception object.  This produced either a `"null"` log
entry (unhelpful) or a redundant pair of log records carrying the same text.

### Change

Collapsed the double-`log` pattern into a single call that passes the exception
directly, combined with a static descriptive message:

```java
// Before
LOGGER.log(Level.INFO, e.getMessage());
LOGGER.log(Level.FINE, e.getMessage(), e);

// After
LOGGER.log(Level.INFO, "Error copying stream", e);
```

### Why this is correct

`Logger.log(Level, String, Throwable)` always logs the full exception chain
(message + stack trace at appropriate levels) and never produces a `"null"`
message.  Using a static literal instead of `getMessage()` prevents the
null-message issue while keeping the log entry informative.

---

## 2. `SqlScript.java` — Null-safe error reporting in `run()`

**File:** `schemacrawler-utility/src/main/java/us/fatehi/utility/database/SqlScript.java`

### Risk

The outer `catch (Exception e)` block in `run()` called `throwable.getMessage()`
(where `throwable` is the root cause obtained by traversing the cause chain).
For exceptions whose message is `null` (e.g. a bare `NullPointerException`)
this results in printing `"null"` to `System.err` and passing `null` as the
log message string — both are misleading to operators.

### Change

Replaced `throwable.getMessage()` with `throwable.toString()`, which always
returns a non-null string containing at least the class name:

```java
// Before
System.err.println(throwable.getMessage());
LOGGER.log(Level.WARNING, throwable.getMessage(), throwable);

// After
System.err.println(throwable.toString());
LOGGER.log(Level.WARNING, throwable.toString(), throwable);
```

### Why this is correct

`Throwable.toString()` is specified to return `getClass().getName()` when
`getMessage()` is null, and `getClass().getName() + ": " + getMessage()`
otherwise.  It is therefore always non-null and consistently informative.

---

## 3. `InputResourceUtility.java` — Silent exception swallowing replaced with `FINE`-level logging

**File:** `schemacrawler-utility/src/main/java/us/fatehi/utility/ioresource/InputResourceUtility.java`

### Risk

`createInputResource()` used two `catch (Exception e) { // No-op }` blocks.
Exceptions thrown while probing the file system or classpath were completely
discarded.  During debugging, this made it impossible to tell *why* a resource
could not be found (permission error, invalid path characters, missing jar,
etc.).

### Change

Added `FINE`-level logging in each catch block, using the existing
`StringFormat` supplier pattern:

```java
} catch (final Exception e) {
    LOGGER.log(
        Level.FINE,
        e,
        new StringFormat("Could not read file <%s>", inputResourceName));
}
```

### Why this is correct

The fallthrough logic (try file, then try classpath, then log INFO) is
intentional and preserved exactly.  The `FINE` level means the messages are
invisible at default log settings but appear when diagnosing a
`Could not locate input resource` failure, providing actionable context at
no cost to normal operation.

---

## 4. `ERModelLoaderRegistry.java` — Overly broad `catch (Throwable)` narrowed

**File:** `schemacrawler-loader/src/main/java/schemacrawler/loader/ermodel/ERModelLoaderRegistry.java`

### Risk

`configureERModelLoaders()` caught `Throwable`, which includes `Error` and all
its subclasses such as `OutOfMemoryError`, `StackOverflowError`, and
`ThreadDeath`.  Catching these JVM-level errors, wrapping them in an
`InternalRuntimeException`, and continuing is incorrect: the JVM may be in an
unrecoverable state, and masking the original `Error` makes diagnosis far harder.

The comment acknowledged the intent was to catch `NoClassDefFoundError` for
missing optional third-party jars.

### Change

Narrowed the catch clause to `Exception | LinkageError`:

```java
// Before
} catch (final Throwable e) {

// After
} catch (final Exception | LinkageError e) {
```

`LinkageError` is the superclass of `NoClassDefFoundError`,
`ClassCircularityError`, `IncompatibleClassChangeError`, and similar class-loading
failures that arise from missing or incompatible jars — exactly the cases the
original comment described.  Fatal JVM errors (`OutOfMemoryError`,
`StackOverflowError`, etc.) are now left to propagate naturally.

### Why this is correct

Catching `LinkageError` covers all class-loading failures at one level of
abstraction, without swallowing unrelated JVM errors.  The behaviour for the
intended case (missing optional jars) is unchanged.

---

## 5. `DatabaseObjectReference.java` — Redundant null-checks on serialization streams removed

**File:** `schemacrawler-api/src/main/java/schemacrawler/crawl/DatabaseObjectReference.java`

### Risk

`readObject(ObjectInputStream)` and `writeObject(ObjectOutputStream)` guarded
all their work with `if (in != null)` / `if (out != null)` checks.  The Java
Object Serialization Specification guarantees that these methods are only
invoked by the serialization framework with non-null, fully-initialised stream
arguments.  If the guards were `false` (which they cannot be under normal
operation), the object would be silently deserialized as `null`/empty without
any exception — a harder-to-diagnose silent-data-loss bug than a
`NullPointerException` would be.

### Change

Removed the redundant null guards so that the fields are always assigned
unconditionally:

```java
// Before
private void readObject(final ObjectInputStream in) throws ... {
    if (in != null) {
        partial = (D) in.readObject();
        databaseObjectRef = new WeakReference<>((D) in.readObject());
    }
}

// After
private void readObject(final ObjectInputStream in) throws ... {
    partial = (D) in.readObject();
    databaseObjectRef = new WeakReference<>((D) in.readObject());
}
```

### Why this is correct

Removing the dead guard aligns the code with the serialization contract, makes
the intent clear (the fields must be populated on deserialization), and ensures
a `NullPointerException` with a meaningful stack trace if the invariant is ever
violated, rather than a silent no-op that leaves the object in a broken state.
