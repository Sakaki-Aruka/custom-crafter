---
title: CraftView
---

## What is CraftView

`CraftView` is a data class that represents the current state of the crafting UI.
It holds the mapping of items placed by the player in each slot.

It is passed as part of the return value of `Search.search()`, and is also used as the `input` field of `CRecipePredicate.Context`.

---

## Class Definition

```kotlin
data class CraftView(
    val materials: Map<CoordinateComponent, ItemStack>
)
```

| Field | Type | Description |
|-------|------|-------------|
| `materials` | `Map<CoordinateComponent, ItemStack>` | Mapping of items placed in the crafting slots |

---

## Methods

### `getDecremented(shiftUsed, recipe, relations)`

Returns the slot state after a craft is executed (the `CraftView` after consumption).
Items corresponding to CMatter with `anyAmount = false` are consumed by their `amount`, while `anyAmount = true` items are consumed by 1.
For bulk crafting (`shiftUsed = true`), the consumption is multiplied by the craft count.

```kotlin
val decremented: CraftView = craftView.getDecremented(
    shiftUsed = true,
    recipe = recipe,
    relations = mappedRelation
)
```

### `clone()`

Returns a deep copy of the `CraftView`.
Each `ItemStack` in `materials` is also `clone()`d.

### `drop(world, location)`

Drops all non-empty items in `materials` at the specified world coordinates.

```kotlin
craftView.drop(player.world, player.location)
```

### `excludeAir()`

Returns a `CraftView` with air (empty slots) excluded.

```kotlin
val filtered: CraftView = craftView.excludeAir()
```

---

## Usage Examples

### Combining with Search.search()

```kotlin
val craftView = CraftView(
    materials = mapOf(
        CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)
    )
)

val results: List<SearchResult> = Search.search(craftView, player.uniqueId)
```

### Usage in CRecipePredicate

```kotlin
val predicate = CRecipePredicate { ctx ->
    // ctx.input is the current CraftView
    val placedCount = ctx.input.materials.values.count { !it.type.isAir }
    placedCount >= 3
}
```
