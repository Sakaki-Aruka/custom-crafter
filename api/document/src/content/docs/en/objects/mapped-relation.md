---
title: MappedRelation / MappedRelationComponent
---

## Overview

`MappedRelation` and `MappedRelationComponent` are data classes that hold the correspondence between recipe coordinates and the actual input slot coordinates.

For shaped recipes (`SHAPED`), a match is found regardless of where on the grid the player places the items as long as the shape matches.
`MappedRelation` records the mapping between those "coordinates in the recipe definition" and "the coordinates where items are actually placed."

---

## Class Definition

```kotlin
data class MappedRelation(
    val components: Set<MappedRelationComponent>
)

data class MappedRelationComponent(
    val recipe: CoordinateComponent,
    val input: CoordinateComponent
)
```

| Field | Type | Description |
|-------|------|-------------|
| `MappedRelation.components` | `Set<MappedRelationComponent>` | Set of correspondence components |
| `MappedRelationComponent.recipe` | `CoordinateComponent` | Coordinate in the recipe definition |
| `MappedRelationComponent.input` | `CoordinateComponent` | Coordinate of the actually placed slot |

---

## Where It Is Used

`MappedRelation` is passed in the following contexts:

| Context | Field Name |
|---------|-----------|
| `ResultSupplier.Context` | `relation` |
| `CRecipePredicate.Context` | `relation` |
| `SearchResult` | `relation` (via getter method) |

---

## Usage Example

An example of checking which slot contains which item inside a `ResultSupplier`:

```kotlin
val supplier = ResultSupplier { ctx ->
    // Get the input coordinate that corresponds to recipe coordinate (0,0)
    val recipeCoord = CoordinateComponent(0, 0)
    val inputCoord: CoordinateComponent? = ctx.relation.components
        .firstOrNull { it.recipe == recipeCoord }
        ?.input

    val inputItem: ItemStack = inputCoord?.let { ctx.mapped[it] } ?: ItemStack.empty()

    listOf(ItemStack.of(Material.DIAMOND, inputItem.amount))
}
```
