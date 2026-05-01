---
title: AdjacentRecipe
---

## What is AdjacentRecipe

`AdjacentRecipe` sits between a fully shaped recipe and a fully shapeless recipe.

Like a shapeless recipe, items may be placed anywhere in the crafting grid with no fixed positional constraints.
Unlike a shapeless recipe, the directions in which neighbouring items may appear are restricted by `RelationType`:

- **`CROSS`** — No item may have a diagonally adjacent neighbour (↖ ↗ ↙ ↘ are forbidden).
- **`DIAGONAL`** — No item may have a cross-adjacent neighbour (↑ ↓ ← → are forbidden).
- **`BOTH`** — All eight surrounding directions are permitted; no directional constraint is applied.

Every item must have at least one neighbour within distance 1. Placements where any item is isolated (distance ≥ 2 from all others) do not match.

:::note
`AdjacentRecipe` only supports `CRecipe.Type.SHAPELESS`.
:::

---

## Static Methods

### checker(relationType: AdjacentRecipe.RelationType): CRecipePredicate

Returns the core `CRecipePredicate` that enforces the directional constraint for the given `relationType`.

This predicate is added to the recipe automatically during construction.
You do not normally need to call it directly.

---

## Constructor

```kotlin
class AdjacentRecipe @JvmOverloads constructor(
    override val name: String,
    matters: List<CMatter>,
    val relationType: RelationType = RelationType.BOTH,
    customPredicates: List<CRecipePredicate>? = null,
    override val results: List<ResultSupplier>? = null
) : CRecipe
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `name` | — | Unique identifier for the recipe |
| `matters` | — | List of ingredients; placement order within the grid is unconstrained |
| `relationType` | `BOTH` | Adjacency rule applied to every placed item |
| `customPredicates` | `null` | Additional predicates evaluated before the adjacency check |
| `results` | `null` | List of `ResultSupplier` instances that produce the craft output |

The adjacency predicate (`checker(relationType)`) is always appended to `predicates` automatically.

---

## AdjacentRecipe.RelationType

Defines which neighbouring directions are permitted between placed items.

| Value | Forbidden directions | Permitted directions |
|-------|----------------------|----------------------|
| `CROSS` | ↖ ↗ ↙ ↘ (diagonal) | ↑ ↓ ← → (cross) |
| `DIAGONAL` | ↑ ↓ ← → (cross) | ↖ ↗ ↙ ↘ (diagonal) |
| `BOTH` | — (none) | All 8 directions |

---

## Usage Examples

In the diagrams below, `#` represents a placed item and `_` represents an empty slot (excerpt from the top-left of a 6×6 grid).

---

### CROSS

Items must **not** have any diagonally adjacent neighbour (↖ ↗ ↙ ↘).

#### Matching placements

```kotlin
val recipe = AdjacentRecipe(
    name        = "cross_line",
    matters     = listOf(
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
    ),
    relationType = AdjacentRecipe.RelationType.CROSS,
)
```

```
# # # _    ← horizontal line; items are only left/right adjacent, no diagonal neighbours → matches
_ _ _ _
_ _ _ _
```

```
# _ _ _
# _ _ _    ← vertical line; items are only up/down adjacent → matches
# _ _ _
```

#### Non-matching placements

```
# _ _ _    ← (0,0) and (1,1) are diagonally adjacent
# # _ _    ← the L-shape corner creates a diagonal pair → forbidden for CROSS → no match
_ _ _ _
```

```
# _ _ _    ← (0,0) and (1,1): diagonal pair, even with only 2 items → no match
_ # _ _
_ _ _ _
```

---

### DIAGONAL

Items must **not** have any cross-adjacent neighbour (↑ ↓ ← →).

#### Matching placements

```kotlin
val recipe = AdjacentRecipe(
    name        = "diagonal_line",
    matters     = listOf(
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
    ),
    relationType = AdjacentRecipe.RelationType.DIAGONAL,
)
```

```
# _ _ _    ← diagonal line going down-right; items are only diagonally adjacent → matches
_ # _ _
_ _ # _
```

```
_ _ # _    ← diagonal line going down-left → matches
_ # _ _
# _ _ _
```

#### Non-matching placements

```
# # _ _    ← (0,0) and (1,0) are cross-adjacent (left/right) → forbidden for DIAGONAL → no match
_ _ _ _
_ _ _ _
```

```
# _ _ _    ← (0,0)–(1,1) is diagonal (OK), but (1,1)–(2,1) is cross-adjacent → no match
_ # # _
_ _ _ _
```

---

### BOTH

All 8 surrounding directions are permitted; no directional constraint is applied.
However, every item must still have at least one neighbour at distance 1.

#### Matching placements

```kotlin
val recipe = AdjacentRecipe(
    name        = "both_example",
    matters     = listOf(
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
    ),
    relationType = AdjacentRecipe.RelationType.BOTH,
)
```

```
# # _ _    ← cross-adjacent → matches
_ _ _ _
```

```
# _ _ _    ← diagonally adjacent → matches
_ # _ _
```

---

## Maximum Item Count

The table below shows the maximum number of items that can be placed in a 6×6 grid for each `RelationType`.

| RelationType | Maximum | Example pattern |
|--------------|---------|-----------------|
| `CROSS` | 18 | Fill all rows in even-numbered columns (x = 0, 2, 4) |
| `DIAGONAL` | 18 | Fill cells where both x and y are even, or both are odd |
| `BOTH` | 36 | No constraint — all 36 cells are valid |

If `matters` exceeds this limit and `isValidRecipe()` is called, an error is returned together with a visual placement pattern for reference.
