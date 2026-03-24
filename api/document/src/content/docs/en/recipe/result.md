---
title: About result items
---

## About result items

In CustomCrafterAPI recipes, it is not strictly required to specify items to return for a given input.
In other words, it is possible to provide a recipe that returns nothing.

However, since all vanilla recipes return an item, and in most situations you will want to create recipes that return items, this page describes `ResultSupplier`, which is used for exactly that purpose.

## ResultSupplier

`ResultSupplier` is a functional interface with a single function, used as the mechanism by which recipes return items.
Unless item creation is handled directly in your own plugin, `ResultSupplier` is executed asynchronously on a virtual thread.

Its definition in Kotlin is as follows:

```kotlin
fun interface ResultSupplier {
    fun supply(ctx: Context): List<ItemStack>
}
```

Only `supply` is defined: it receives a context and returns a list of `ItemStack` items.
The context holds the player's item arrangement at the time `supply` is called, the click type, the number of invocations, and — during async calls — the interrupt state, among other values.

## Context

As noted above, the context provides a variety of state:

| Field | Type | Description |
|-------|------|-------------|
| `recipe` | `CRecipe` | The recipe that contains this ResultSupplier |
| `relation` | `MappedRelation` | The mapping between the recipe's arrangement and the actual item arrangement |
| `mapped` | `Map<CoordinateComponent, ItemStack>` | The actual item arrangement |
| `shiftClicked` | `Boolean` | Whether bulk crafting mode is active (i.e., whether the Shift key was held) |
| `calledTimes` | `Int` | The number of crafts (if in bulk crafting mode, the maximum craftable quantity) |
| `crafterID` | `UUID` | The UUID of the player who performed the craft |
| `isMultipleDisplayCall` | `Boolean` | Whether this call originates from the "show all crafting candidates" feature |
| `asyncContext` | `AsyncContext?` | Context for async execution; `null` during synchronous execution |

You can use these values inside `ResultSupplier` to decide what items to produce.

### Calculating calledTimes

`calledTimes` is calculated as the minimum value of "input count ÷ CMatter's `amount`" across all items whose corresponding CMatter has `mass = false`.

For example, suppose a recipe requires "2 Stones + 1 Gold Ingot" and the player inputs "64 Stones + 32 Gold Ingots" using Shift bulk crafting:

- Stone: 64 ÷ 2 = 32
- Gold Ingot: 32 ÷ 1 = 32
- `calledTimes` = min(32, 32) = **32**

Using `ResultSupplier.timesSingle` multiplies the result item count by `calledTimes`.

### About isMultipleDisplayCall

When `UseMultipleResultCandidateFeature` is enabled, there is a feature that previews the result items of multiple matching recipes for the given input.
During a call made for this preview, `isMultipleDisplayCall = true`.

It is recommended to check this flag according to your logic so that side effects (such as database writes) do not occur during preview calls.

```kotlin
val result = ResultSupplier { ctx ->
    if (ctx.isMultipleDisplayCall) {
        // Do not trigger side effects for preview-only calls
        return@ResultSupplier listOf(ItemStack.of(Material.DIAMOND))
    }
    // Update the database only during actual crafting
    MyDatabase.recordCraft(ctx.crafterID)
    listOf(ItemStack.of(Material.DIAMOND))
}
```

### Notes on asynchronous execution

When `asyncContext` is non-`null`, `supply` is running on a virtual thread.
On an asynchronous thread, access to BukkitAPI worlds and entities is not permitted, so operations such as giving items to a player or retrieving coordinates are not possible.

Furthermore, because virtual threads work cooperatively, they cannot be forcibly interrupted.
Periodically check `asyncContext.isInterrupted()` and abort processing if an interrupt request has been received.

```kotlin
val asyncAwareResult = ResultSupplier { ctx ->
    val asyncCtx = ctx.asyncContext
    if (asyncCtx != null) {
        // Check for interrupts during async execution
        if (asyncCtx.isInterrupted()) {
            return@ResultSupplier emptyList()
        }
        // BukkitAPI world access is not permitted in async threads
        // Use ctx.crafterID to fetch data instead
        val data = MyDatabase.fetchData(ctx.crafterID)
        return@ResultSupplier listOf(ItemStack.of(data.material))
    }
    // Normal processing during synchronous execution
    listOf(ItemStack.of(Material.DIAMOND))
}
```

## Predefined convenience objects

Because `ResultSupplier` has a very simple definition, any logic can be written inside it. However, for common cases such as simply returning a single item in response to the input, a significant amount of boilerplate may be needed to achieve the desired behaviour.
To avoid this and make `ResultSupplier` easy to use, predefined objects are provided for these situations.

`ResultSupplier.single(vararg items: ItemStack)` creates a `ResultSupplier` that simply gives the player the specified items upon a crafting call from a recipe.
Even when multiple sets of materials are placed and crafted in bulk crafting mode, the items given to the player are exactly those specified in `items` — the count is not multiplied.

`ResultSupplier.timesSingle(vararg items: ItemStack)` behaves the same as `single` except that it adjusts the number of items provided.
When crafted in bulk crafting mode, the count of each item in `items` is multiplied by the "number of crafts."
For example, if materials for 2 crafts are placed and the player crafts in bulk mode, the items specified in `items` are provided with their counts doubled.
You can think of this object's behaviour as essentially the same as vanilla item crafting.

```kotlin
// Example of when to use single vs timesSingle
val stone = CMatterImpl(
    name = "stone",
    candidate = setOf(Material.STONE),
    amount = 2,  // requires 2 stones
    mass = false,
    predicates = null
)

// single: always returns exactly 1 Diamond regardless of how many are crafted
val singleResult = ResultSupplier.single(ItemStack.of(Material.DIAMOND))

// timesSingle: Shift bulk crafting with 64 Stones returns 32 Diamonds
val timesResult = ResultSupplier.timesSingle(ItemStack.of(Material.DIAMOND))

val recipe = CRecipeImpl(
    name = "stone-to-diamond",
    items = mapOf(CoordinateComponent(0, 0) to stone),
    results = listOf(timesResult),
    type = CRecipe.Type.SHAPED
)
```

## Fully custom implementation example

An example that changes the returned item based on the player's UUID.

```kotlin
val customResult = ResultSupplier { ctx ->
    // Return a special item to a specific player
    val specialPlayer = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
    if (ctx.crafterID == specialPlayer) {
        return@ResultSupplier listOf(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE))
    }
    listOf(ItemStack.of(Material.GOLDEN_APPLE))
}
```
