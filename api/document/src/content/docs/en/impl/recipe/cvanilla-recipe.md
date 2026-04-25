---
title: CVanillaRecipe
---

## What is CVanillaRecipe

`CVanillaRecipe` is a `CRecipe` wrapper for vanilla Bukkit `Recipe` instances.
It allows vanilla crafting recipes to be used as `CRecipe`, making them compatible with CustomCrafter features such as `PartialSearch`.

The constructor is `internal` and cannot be called directly. Use the provided factory methods to create instances.

---

## Factory Methods

### fromVanilla()

Converts a vanilla `CraftingRecipe` to a `CVanillaRecipe`.
Dispatches to `fromShaped()`, `fromShapeless()`, or `fromTransmute()` based on the runtime type.
Returns `null` if the recipe type is not supported.

```kotlin
@JvmStatic
fun fromVanilla(recipe: CraftingRecipe): CVanillaRecipe?
```

---

### fromShaped()

Converts a vanilla `ShapedRecipe`.

```kotlin
@JvmStatic
fun fromShaped(recipe: ShapedRecipe): CVanillaRecipe
```

The recipe shape and ingredient map are converted into a `Map<CoordinateComponent, CMatter>`.
Each `CMatter` uses a `CMatterPredicate` backed by `RecipeChoice.test()`, so NBT and exact-item choices are respected.

---

### fromShapeless()

Converts a vanilla `ShapelessRecipe`.

```kotlin
@JvmStatic
fun fromShapeless(recipe: ShapelessRecipe): CVanillaRecipe
```

Ingredients are mapped to sequential coordinates via `CoordinateComponent.fromIndex()`.
Each `CMatter` uses a `CMatterPredicate` backed by `RecipeChoice.test()`.

---

### fromTransmute()

Converts a vanilla `TransmuteRecipe`. Added in **5.0.21**.

```kotlin
@JvmStatic
fun fromTransmute(recipe: TransmuteRecipe): CVanillaRecipe
```

A `TransmuteRecipe` is a crafting recipe where a source item (`input`) has its type changed to `result` when combined with a catalyst item (`material`).
The converted `CVanillaRecipe` is `SHAPELESS` with two slots — one for the source and one for the catalyst.

The `ResultSupplier` finds the item matching `recipe.input` in the crafting grid and returns a copy with the type replaced by `recipe.result.type`.

```kotlin
// Example: wrap all vanilla transmute recipes and register them
Bukkit.recipeIterator().asSequence()
    .filterIsInstance<TransmuteRecipe>()
    .mapNotNull { CVanillaRecipe.fromTransmute(it) }
    .forEach { CustomCrafterAPI.registerVanillaRecipe(it) }
```

---

## relateWith()

Builds a `MappedRelation` by pairing this recipe's slots against the given `CraftView` in index order.

```kotlin
fun relateWith(view: CraftView): MappedRelation
```

| Condition | Behavior |
|-----------|----------|
| Non-air item count in `view` ≠ recipe slot count | Throws `IllegalArgumentException` |
| View spans more than 4 columns or 4 rows | Throws `IllegalArgumentException` |

---

## original

The original Bukkit `Recipe` instance is accessible via the `original` property.

```kotlin
val original: Recipe
```

---

## CVanillaRecipe + PartialSearch

Because `CVanillaRecipe` implements `CRecipe`, it is compatible with `PartialSearch`.
This enables **partial matching for vanilla recipes**, which is not available in the Bukkit API.

```kotlin
val vanillaRecipes: List<CVanillaRecipe> = Bukkit.recipeIterator().asSequence()
    .filterIsInstance<CraftingRecipe>()
    .mapNotNull { CVanillaRecipe.fromVanilla(it) }
    .toList()

// Asynchronous partial search against vanilla recipes
val result: PartialSearch.PartialSearchResult = PartialSearch.asyncPartialSearch(
    recipes = vanillaRecipes,
    view = currentView,
    query = SearchQuery(asyncContext = ctx)
).await()
```

:::note
Vanilla recipes loaded at startup can be iterated with `Bukkit.recipeIterator()`.
Wrapping them with `CVanillaRecipe` once and caching the list is recommended for performance.
:::