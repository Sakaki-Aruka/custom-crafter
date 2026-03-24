---
title: About recipe
---

## Recipes in CustomCrafterAPI

As written on the [Getting Started page](/getting-started/), recipes in CustomCrafterAPI are entirely separate from Minecraft's standard recipe system and are composed of different components.

The fields of `CRecipe` are defined as follows:
```kotlin
interface CRecipe {
    val name: String
    val items: Map<CoordinateComponent, CMatter>
    val predicates: List<CRecipePredicate>?
    val results: List<ResultSupplier>?
    val type: Type // CRecipe.Type
}
```

`name`, `items`, and `type` are required fields representing the recipe name, item arrangement, and recipe type (shaped or shapeless) respectively.

## name
`name` is the recipe's identifier and is used in features such as the "show all matching recipes" display and the registered recipe list command.
It is recommended to use a name that is unique across all registered recipes and easy for users to recognize.

## items
`items` represents the item arrangement: a mapping of `CoordinateComponent` (which denotes a position on the recipe grid) to `CMatter` (which defines the required item at that position).

`CoordinateComponent` represents a position on the 6×6 grid using two integers, `X` and `Y`:
```
// Coordinates expressed as (X, Y)

(0,0) (1,0) (2,0) (3,0) (4,0) (5,0)
(0,1) (1,1) (2,1) (3,1) (4,1) (5,1)
(0,2) (1,2) (2,2) (3,2) (4,2) (5,2)
(0,3) (1,3) (2,3) (3,3) (4,3) (5,3)
(0,4) (1,4) (2,4) (3,4) (4,4) (5,4)
(0,5) (1,5) (2,5) (3,5) (4,5) (5,5)
```

For shaped recipes (`CRecipe.Type.SHAPED`), the coordinates define the *shape* of the arrangement. To express a 2×2 square, specifying:
```
(0,0), (1,0)
(0,1), (1,1)
```
is sufficient. With this specification, placing items at:
```
(4,0), (5,0)
(4,1), (5,1)
```
will also pass the check, since it is the same shape.

---

`CMatter` is a set of inspection conditions for the item placed in the corresponding slot.
The simplest condition — checking only the item type — will pass as long as the `Material` matches; quantity, enchantments, metadata, etc. are not checked.

```kotlin
// How to get a CMatter that only checks the item type
// Passes if the placed item is Stone or Cobblestone

val matter1 = CMatterImpl(
    name = "only-stone",
    candidate = setOf(Material.STONE, Material.COBBLESTONE),
    amount = 1,
    mass = false,
    predicates = CRecipeImpl.defaultMatterPredicates()
)

// A simpler way to get the same CMatter
val matter2 = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
```

The default `CMatterImpl` has fields `name`, `candidate`, `amount`, `mass`, and `predicates`, which work as follows:
- `name`: The name of the CMatter (rarely used, any value is fine)
- `candidate`: The placed item's type must match one of the listed materials
- `amount`: The placed item's count must be equal to or greater than this value
- `mass`: When `true`, ignores `amount` and only requires 1 or more items; also affects bulk crafting quantity calculations.
  When `false`, follows the `amount` quantity specification.
- `predicates`: Allows inserting custom inspection logic beyond the fields above. If even one predicate returns `false`, the entire inspection fails. By default, predicates for enchantments, potions, etc. are inserted.

Each input item at the corresponding position is checked against the conditions above. If even one item does not satisfy all conditions, the recipe is considered not to match the input.

### candidate
In CustomCrafterAPI recipes, you can specify multiple `Material` types for a single slot.
This eliminates the need to define multiple similar recipes.
```kotlin
// Reproduce the materials for a recipe that crafts planks from any log
val log = CMatterImpl.of(*Tag.LOGS.getValues().toTypedArray())

// Accept both Cobblestone and Stone
val stones = CMatterImpl.of(Material.COBBLESTONE, Material.STONE)
```

### predicates
`predicates` can hold inspection logic for conditions that `candidate` and `amount` cannot cover.
It is a set of `CMatterPredicate` instances, each of which receives a `CMatterPredicate.Context` and must return a `Boolean` from its `test` function.

Since you can write arbitrary logic here, it is possible to call an external database or API and use the result for matching decisions.

## type
`type` indicates the recipe type.
CustomCrafterAPI supports two types: **shaped** (`CRecipe.Type.SHAPED`), where the item arrangement must match the recipe exactly, and **shapeless** (`CRecipe.Type.SHAPELESS`), where the arrangement does not matter — the recipe passes as long as all specified items are present.

To create a shapeless recipe, using `CRecipeImpl.shapeless` lets you define the recipe with fewer parameters.

## predicates (Nullable)
`predicates` allows inserting checks that return `Boolean` based on the recipe and the input items.
If even one predicate returns `false`, the recipe containing it is excluded from crafting candidates.
Set this to `null` if you do not need this feature.

## results (Nullable)
`results` holds lambdas that generate the items (result items) to give the player when the recipe matches their input.
In addition to generating result items, you can also use this as a function executed at crafting time — for example, to grant a buff or execute a database query.
Like `predicates`, set this to `null` if you do not need it.

---

:::note
`CRecipePredicate` (held by `predicates`) and `ResultSupplier` (held by `results`) each have their own dedicated documentation pages.
:::
