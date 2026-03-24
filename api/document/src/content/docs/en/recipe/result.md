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

- `recipe`: The recipe (`CRecipe`) that contains this `ResultSupplier`
- `relation`: The mapping between the recipe's arrangement and the actual item arrangement
- `mapped`: The actual item arrangement
- `shiftClicked`: Whether bulk crafting mode is active (i.e., whether Shift was held at craft time)
- `calledTimes`: The number of crafts (if in bulk crafting mode, the maximum craftable quantity)
- `crafterID`: The UUID of the player who performed the craft
- `isMultipleDisplayCall`: Whether this call originates from the "show all crafting candidates" feature
- `asyncContext` (Nullable): Context for async execution

You can use these values inside `ResultSupplier` to decide what items to produce.
Note that `asyncContext` is provided only when `ResultSupplier#supply` is running asynchronously; it is `null` otherwise.

## Predefined convenience objects

Because `ResultSupplier` has a very simple definition, any logic can be written inside it. However, for cases such as simply returning a single item in response to the input, a lot of code may be needed to achieve the desired behavior.
To avoid this and make `ResultSupplier` easy to use, predefined objects are provided for common situations.

`ResultSupplier.single(vararg items: ItemStack)` creates a `ResultSupplier` that simply gives the player the specified items upon a crafting call from a recipe.
Even when multiple sets of materials are placed and crafted in bulk crafting mode, the items given to the player are exactly those specified in `items` — the count is not multiplied.

`ResultSupplier.timesSingle(vararg items: ItemStack)` behaves the same as `single` except that it adjusts the number of items provided.
When crafted in bulk crafting mode, the count of each item in `items` is multiplied by the "number of crafts."
For example, if materials for 2 crafts are placed and the player crafts in bulk mode, the items specified in `items` are provided with their counts doubled.
You can think of this object's behavior as essentially the same as vanilla item crafting.
