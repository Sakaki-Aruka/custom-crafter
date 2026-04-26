---
title: About recipe
---

## Recipes in CustomCrafterAPI

As written on the [Getting Started page](/ja/getting-started/), recipes in CustomCrafterAPI are entirely separate from Minecraft's standard recipe system and are composed of completely different components.

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

`name`, `items`, and `type` are required fields, representing the recipe name, item arrangement, and recipe type (shaped or shapeless) respectively.

## name
`name` is the recipe's identifier and is used in features such as the "show all matching recipes" display and the registered recipe list command.
It is recommended to use a name that is unique across all registered recipes and easy for users to recognise.

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
(4,4), (5,4)
(4,5), (5,5)
```
will also pass the check, since it is the same shape.

### CoordinateComponent factory methods

`CoordinateComponent` provides factory methods to efficiently generate sets of coordinates.

#### `squareFill(size, dx, dy)` — fill a square

Returns the full set of coordinates that fills a square of the specified size.

```kotlin
// Generate fill coordinates for a 3×3 square
CoordinateComponent.squareFill(3)
// → (0,0) (1,0) (2,0)
//   (0,1) (1,1) (2,1)
//   (0,2) (1,2) (2,2)

// Specify an offset to start at (1,1)
CoordinateComponent.squareFill(3, dx = 1, dy = 1)
```

#### `square(size, dx, dy)` — square outline

Returns only the perimeter coordinates of a square of the specified size (interior not included).

```kotlin
// Generate outline coordinates for a 3×3 square
CoordinateComponent.square(3)
// → (0,0) (1,0) (2,0)
//   (0,1)       (2,1)
//   (0,2) (1,2) (2,2)
```

#### `getN(n)` — first N coordinates

Returns a list of N coordinates starting from the top-left. Primarily used when building `items` for shapeless recipes.

```kotlin
// Get coordinates for 3 slots
CoordinateComponent.getN(3)
// → [(0,0), (1,0), (2,0)]
```

#### `recipeMapFromStringList(lines, map)` — convert from a list of strings

Generates an `items` map in bulk from a comma-separated list of strings and a character-to-CMatter mapping.
Empty elements in a slot are skipped. Both row count and column count can be up to 6.

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val apple = CMatterImpl.of(Material.APPLE)

val lines = listOf(
    "s,s,s",
    "s,a,s",
    "s,s,s"
)
val map = mapOf("s" to stone, "a" to apple)

val items = CoordinateComponent.recipeMapFromStringList(lines, map)
// The 9 slots from (0,0) to (2,2) become stone, with (1,1) being apple
```

To include empty slots, leave the entry between commas blank.

```kotlin
val lines = listOf(
    "s,s",
    ",s"   // (0,1) is skipped
)
```

#### `mapToRecipeMap(source)` — convert from a CMatter-keyed mapping

Generates `items` from a reverse mapping of `CMatter → Set<CoordinateComponent>`.
This reduces the amount of code needed when assigning the same CMatter to multiple slots.

```kotlin
val gold = CMatterImpl.of(Material.GOLD_BLOCK)
val apple = CMatterImpl.of(Material.APPLE)

val source = mapOf(
    gold to CoordinateComponent.squareFill(3).minus(CoordinateComponent(1, 1)),
    apple to setOf(CoordinateComponent(1, 1))
)

val items = CoordinateComponent.mapToRecipeMap(source)
// The border of the 3×3 square is gold; the centre (1,1) is apple
```

---

`CMatter` is a collection of inspection conditions for the item placed in the corresponding slot.
The simplest condition — checking only the item type — will pass as long as the `Material` matches; quantity, enchantments, metadata, etc. are not checked.

```kotlin
// How to obtain a CMatter that only checks the item type
// Passes if the placed item is Stone or Cobblestone

val matter1 = CMatterImpl(
    name = "only-stone",
    candidate = setOf(Material.STONE, Material.COBBLESTONE),
    amount = 1,
    anyAmount = false,
    predicates = CRecipeImpl.defaultMatterPredicates()
)

// A simpler way to obtain the same CMatter
val matter2 = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
```

The default `CMatterImpl` has the fields `name`, `candidate`, `amount`, `anyAmount`, and `predicates`, which work as follows:
- `name`: The name of the CMatter (rarely used; any value is fine)
- `candidate`: The placed item's type must match one of the listed materials
- `amount`: The placed item's count must be equal to or greater than this value
- `anyAmount`: When `true`, ignores `amount` and only requires 1 or more items; also affects bulk crafting quantity calculations.
  When `false`, follows the `amount` quantity specification.
- `predicates`: Allows inserting custom inspection logic beyond the fields above. If even one predicate returns `false`, the entire inspection fails. By default, predicates for enchantments, potions, etc. are inserted.

Each input item at the corresponding position is checked against the conditions above. If even one item does not satisfy all conditions, the recipe is considered not to match the input.

### candidate
In CustomCrafterAPI recipes, you can specify multiple `Material` types (`Material`) for a single slot.
This eliminates the need to define multiple similar recipes.
```kotlin
// Reproduce the material for a recipe that crafts planks from any log
val log = CMatterImpl.of(*Tag.LOGS.getValues().toTypedArray())

// Accept both Cobblestone and Stone
val stones = CMatterImpl.of(Material.COBBLESTONE, Material.STONE)
```

### predicates
`predicates` can hold inspection logic for conditions that `candidate` and `amount` cannot cover.
It is a set of `CMatterPredicate` functional interface instances, each of which receives a `CMatterPredicate.Context` and must return a `Boolean` from its `test` function.

Since arbitrary logic can be written here, it is possible to call an external database or API and use the result for matching decisions.

## type
`type` indicates the recipe type.
CustomCrafterAPI supports two types: **shaped** (`CRecipe.Type.SHAPED`), where the item arrangement must exactly match the recipe, and **shapeless** (`CRecipe.Type.SHAPELESS`), where the arrangement does not matter — the recipe passes as long as all specified items are present.

### Shaped recipe example

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val result = ResultSupplier.timesSingle(ItemStack.of(Material.CHISELED_STONE_BRICKS))

// Specifying coordinates individually
val shaped1 = CRecipeImpl(
    name = "stone-bricks",
    items = mapOf(
        CoordinateComponent(0, 0) to stone,
        CoordinateComponent(1, 0) to stone,
        CoordinateComponent(0, 1) to stone,
        CoordinateComponent(1, 1) to stone
    ),
    results = listOf(result),
    type = CRecipe.Type.SHAPED
)

// Using squareFill (same arrangement as above)
val shaped2 = CRecipeImpl(
    name = "stone-bricks-2",
    items = CoordinateComponent.squareFill(2).associateWith { stone },
    results = listOf(result),
    type = CRecipe.Type.SHAPED
)

// Generating from a string list
val shaped3 = CRecipeImpl(
    name = "stone-bricks-3",
    items = CoordinateComponent.recipeMapFromStringList(
        listOf("s,s", "s,s"),
        mapOf("s" to stone)
    ),
    results = listOf(result),
    type = CRecipe.Type.SHAPED
)
```

### Shapeless recipe example

When creating a shapeless recipe, using `CRecipeImpl.shapeless` lets you define the recipe with fewer parameters.

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val result = ResultSupplier.timesSingle(ItemStack.of(Material.GRAVEL))

// Using CRecipeImpl.shapeless (coordinates are assigned automatically)
val shapeless1 = CRecipeImpl.shapeless(
    name = "stone-to-gravel",
    items = List(4) { stone },  // requires 4 stones
    results = listOf(result)
)

// Using CRecipeImpl directly (type = SHAPELESS stated explicitly)
val shapeless2 = CRecipeImpl(
    name = "stone-to-gravel-2",
    items = CoordinateComponent.getN(4).associateWith { stone },
    results = listOf(result),
    type = CRecipe.Type.SHAPELESS
)
```

### Recipe validation

You can call `CRecipe.isValidRecipe()` to check in advance whether the recipe is in a registrable state.
`CustomCrafterAPI.registerRecipe()` calls this method internally, and throws an `IllegalStateException` if it fails.

```kotlin
val recipe = CRecipeImpl(/* ... */)
recipe.isValidRecipe().exceptionOrNull()?.let { e ->
    println("Recipe is invalid: ${e.message}")
    return
}
CustomCrafterAPI.registerRecipe(recipe)
```

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
- For `CRecipePredicate` (held by `predicates`), see the [CRecipePredicate page](/ja/recipe/predicate/).
- For `ResultSupplier` (held by `results`), see the [Result items page](/ja/recipe/result/).
:::
