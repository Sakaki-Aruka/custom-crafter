---
title: About CRecipePredicate
---

## What is CRecipePredicate?

`CRecipePredicate` is a functional interface for inserting inspections that target the recipe as a whole, rather than individual item slots, into a `CRecipe`.
While `CMatterPredicate` inspects the item in each individual slot, `CRecipePredicate` can reference the entire input item arrangement, player information, recipe information, and more all at once.

```kotlin
fun interface CRecipePredicate {
    fun test(ctx: Context): Boolean
}
```

It is held as a list in the `CRecipe.predicates` field. The recipe is considered a match only when all predicates in the list return `true`.

---

## Context

The context passed to the `test` function has the following fields:

| Field | Type | Description |
|-------|------|-------------|
| `input` | `CraftView` | The input state of the crafting UI (item arrangement, result slot) |
| `crafterId` | `UUID` | The UUID of the player who performed the craft |
| `recipe` | `CRecipe` | The recipe being inspected |
| `relation` | `MappedRelation` | The mapping between recipe coordinates and input slot coordinates (generated after CMatter inspections pass) |
| `asyncContext` | `AsyncContext?` | Context for async execution; `null` during synchronous execution (available from 5.0.20 onwards) |

Because execution may occur on an asynchronous thread, it is recommended to check for interrupts via `asyncContext?.isInterrupted()`.
Also, because access to BukkitAPI worlds and entities is not permitted on asynchronous threads, it is recommended to check the execution context with `ctx.isAsync()`.

---

## Implementation examples

### Allow crafting only for specific players

```kotlin
val onlyAdminPredicate = CRecipePredicate { ctx ->
    val player = Bukkit.getPlayer(ctx.crafterId) ?: return@CRecipePredicate false
    player.isOp
}

val recipe = CRecipeImpl(
    name = "admin-only-recipe",
    items = mapOf(CoordinateComponent(0, 0) to CMatterImpl.of(Material.DIAMOND)),
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.NETHERITE_INGOT))),
    type = CRecipe.Type.SHAPED,
    predicates = listOf(onlyAdminPredicate)
)
```

### Referencing an external file or database (with async consideration)

```kotlin
val externalCheckPredicate = CRecipePredicate { ctx ->
    // Check for interrupts when called from an async thread
    if (ctx.asyncContext?.isInterrupted() == true) {
        return@CRecipePredicate false
    }

    // BukkitAPI access is not permitted on async threads
    // Example of querying the database using UUID
    val hasPermission: Boolean = MyDatabase.hasPermission(ctx.crafterId, "special-recipe")
    hasPermission
}
```

### Using the total number of matched items as a condition

You can use `relation` to reference the mapping between recipe coordinates and input coordinates.

```kotlin
val multipleItemsPredicate = CRecipePredicate { ctx ->
    // Check whether all input items have the same stack size
    val inputItems = ctx.input.materials.values
    val firstAmount = inputItems.firstOrNull()?.amount ?: return@CRecipePredicate true
    inputItems.all { it.amount == firstAmount }
}
```

---

## When to use CRecipePredicate vs CMatterPredicate

| | CMatterPredicate | CRecipePredicate |
|--|-----------------|-----------------|
| Inspection scope | A single item in an individual slot | The entire recipe and all input items |
| Available information | The placed item, coordinates, recipe | The full CraftView, player UUID, recipe, MappedRelation |
| Use cases | Per-item checks such as item type, enchantments, potions | Player permissions, logic spanning the entire input arrangement, external data lookups |

:::note
`CRecipePredicate` is executed after all `CMatterPredicate` checks have passed.
Therefore, `CRecipePredicate.Context.relation` holds a finalised coordinate mapping generated from the item arrangement that passed the CMatter inspections.
:::
