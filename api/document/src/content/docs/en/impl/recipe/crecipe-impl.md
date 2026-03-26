---
title: CRecipeImpl
---

## What is CRecipeImpl

`CRecipeImpl` is the standard implementation class for the `CRecipe` interface.
It can be used to create both shaped and shapeless recipes.
Since it is defined as an `open class`, you can also extend it to create recipes with custom fields.

---

## Constructor

```kotlin
open class CRecipeImpl @JvmOverloads constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipe.Type,
    override val predicates: List<CRecipePredicate>? = null,
    override val results: List<ResultSupplier>? = null,
) : CRecipe
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `name` | — | Identifier name for the recipe |
| `items` | — | Mapping of coordinates to item conditions |
| `type` | — | `CRecipe.Type.SHAPED` or `CRecipe.Type.SHAPELESS` |
| `predicates` | `null` | Additional validation logic applied to the whole recipe |
| `results` | `null` | List of `ResultSupplier` instances that produce the output |

---

## Factory Method: `shapeless()`

A factory method for concisely creating shapeless recipes.
Pass a list of `CMatter` instances to `items` and coordinates will be assigned automatically.
Internally calls `isValidRecipe()` for validation and throws an exception if it fails.

```kotlin
@JvmStatic
fun shapeless(
    name: String,
    items: List<CMatter>,
    predicates: List<CRecipePredicate>? = null,
    results: List<ResultSupplier>? = null,
): CRecipeImpl
```

The size of `items` must be between 1 and 36 (inclusive).

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val result = ResultSupplier.timesSingle(ItemStack.of(Material.GRAVEL))

// A shapeless recipe requiring 4 stone
val recipe = CRecipeImpl.shapeless(
    name = "stone-to-gravel",
    items = List(4) { stone },
    results = listOf(result)
)
```

---

## Usage Examples

### Shaped Recipe

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val result = ResultSupplier.timesSingle(ItemStack.of(Material.CHISELED_STONE_BRICKS))

// A recipe that arranges stone in a 2x2 square
val shaped = CRecipeImpl(
    name = "chiseled-stone-bricks",
    items = CoordinateComponent.squareFill(2).associateWith { stone },
    results = listOf(result),
    type = CRecipe.Type.SHAPED
)
```

### Shapeless Recipe

```kotlin
val apple = CMatterImpl.of(Material.APPLE)
val result = ResultSupplier.single(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE))

// A recipe requiring 8 apples in any arrangement
val shapeless = CRecipeImpl.shapeless(
    name = "apple-to-notch-apple",
    items = List(8) { apple },
    results = listOf(result)
)
```

### Using predicates for Restrictions

```kotlin
val diamond = CMatterImpl.of(Material.DIAMOND)
val result = ResultSupplier.single(ItemStack.of(Material.NETHER_STAR))

// A recipe that can only be crafted by OP players
val opRecipe = CRecipeImpl(
    name = "op-only-star",
    items = CoordinateComponent.squareFill(3).associateWith { diamond },
    type = CRecipe.Type.SHAPED,
    results = listOf(result),
    predicates = listOf(CRecipePredicate { ctx ->
        ctx.player?.isOp ?: false
    })
)
```

### Extending with Custom Fields

```kotlin
// An example of adding category information to a recipe
open class CategorizedRecipe(
    name: String,
    items: Map<CoordinateComponent, CMatter>,
    type: CRecipe.Type,
    val category: String,
    predicates: List<CRecipePredicate>? = null,
    results: List<ResultSupplier>? = null,
) : CRecipeImpl(name, items, type, predicates, results)
```

---

## Validation

You can check whether a recipe is in a registrable state using `CRecipeImpl.isValidRecipe()`.
`CustomCrafterAPI.registerRecipe()` calls this method internally, and throws an `IllegalStateException` if validation fails.

```kotlin
val recipe = CRecipeImpl(/* ... */)
recipe.isValidRecipe().exceptionOrNull()?.let { e ->
    println("Recipe is invalid: ${e.message}")
    return
}
CustomCrafterAPI.registerRecipe(recipe)
```
