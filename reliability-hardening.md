# Reliability Hardening Changes

This document describes the targeted reliability improvements made to
SchemaCrawler-Core.  Each entry covers the affected file, the risk addressed,
the specific change, and why it is the right fix.

---

## 1. `ERModelLoaderRegistry.java` — Overly broad `catch (Throwable)` narrowed

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

## 2. `DatabaseObjectReference.java` — Redundant null-checks on serialization streams removed

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
