---
title: AsyncContext
---

## What is AsyncContext

`AsyncContext` is a class that manages state during asynchronous execution.
It is passed via the context when processing such as `ResultSupplier`, `CMatterPredicate`, and `CRecipePredicate` runs on a virtual thread.

CustomCrafterAPI performs asynchronous processing using Java 21 virtual threads.
Because virtual threads operate cooperatively, they cannot be forcibly terminated from the outside.
Therefore, you must periodically check `isInterrupted()` during processing and voluntarily abort if an interrupt has been requested.

---

## Class Definition

```kotlin
class AsyncContext(interrupted: Boolean) {
    companion object {
        @JvmStatic
        fun ofTurnOff(): AsyncContext
    }

    fun isInterrupted(): Boolean
}
```

| Method | Description |
|--------|-------------|
| `ofTurnOff()` | Creates an `AsyncContext` with no interrupt set (factory method) |
| `isInterrupted()` | Returns whether an interrupt has been requested |

`interrupt()` is `internal` and cannot be called directly from outside.
Interrupts are performed by CustomCrafterAPI's internal processing.

---

## How Asynchronous Execution Works

The processing of `ResultSupplier` and the various `Predicate` types runs on a virtual thread unless the plugin handles the item-creation portion itself.
During asynchronous execution, an `AsyncContext` instance is passed in the `asyncContext` field of the context. During synchronous execution it is `null`.

| Situation | `asyncContext` |
|-----------|----------------|
| Synchronous execution | `null` |
| Asynchronous execution | `AsyncContext` instance |

---

## Usage Examples

### Interrupt check in ResultSupplier

```kotlin
val supplier = ResultSupplier { ctx ->
    val asyncCtx = ctx.asyncContext
    if (asyncCtx != null) {
        // Check for interrupt before a heavy operation
        if (asyncCtx.isInterrupted()) {
            return@ResultSupplier emptyList()
        }

        val data = MyDatabase.fetchData(ctx.crafterID)

        // Also check between multiple steps
        if (asyncCtx.isInterrupted()) {
            return@ResultSupplier emptyList()
        }

        return@ResultSupplier listOf(ItemStack.of(data.material))
    }
    // Processing for synchronous execution
    listOf(ItemStack.of(Material.DIAMOND))
}
```

### Interrupt check in CMatterPredicate

```kotlin
val predicate = CMatterPredicate { ctx ->
    // Check whether running asynchronously
    if (ctx.isAsync) {
        val asyncCtx = ctx.asyncContext ?: return@CMatterPredicate false
        if (asyncCtx.isInterrupted()) {
            return@CMatterPredicate false
        }
    }
    // Item inspection logic
    ctx.input.itemMeta?.displayName() != null
}
```

:::caution
Accessing Bukkit API worlds and entities on an async thread is not safe.
Operations such as giving items to a player or obtaining coordinates should be done on the sync thread, or handled after the fact via a UUID such as `ctx.crafterID`.
:::
