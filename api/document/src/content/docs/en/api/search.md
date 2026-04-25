---
title: Search API
---

## What is Search

`Search` is the entry-point object for performing recipe searches in CustomCrafterAPI.
It matches the input item arrangement against registered recipes and returns the matching recipes along with their coordinate-mapping information.

---

## CraftView

`CraftView` is the class that serves as the argument for recipe searches, representing the item arrangement in the crafting UI.

```kotlin
data class CraftView(
    val materials: Map<CoordinateComponent, ItemStack>, // arrangement of items placed by the player
    val result: ItemStack                               // item in the result slot
)
```

The size of `materials` must be between 1 and 36 inclusive. Otherwise, `Search.search` / `Search.asyncSearch` will throw an `IllegalArgumentException`.

```kotlin
// Example: create a CraftView with stone placed at (0,0)
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)
```

---

## Search.search (synchronous search)

```kotlin
fun search(
    crafterID: UUID,
    view: CraftView,
    forceSearchVanillaRecipe: Boolean = true,
    onlyFirst: Boolean = false,
    sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
): SearchResult
```

A synchronous search method that runs on the main thread.
If there are many registered recipes or if predicates contain heavy processing, this may affect server TPS.
In such cases, using `asyncSearch` (described below) is recommended.

| Argument | Description |
|----------|-------------|
| `crafterID` | The UUID of the player performing the craft |
| `view` | The arrangement of input items |
| `forceSearchVanillaRecipe` | When `true`, always searches vanilla recipes. When `false` and a custom recipe is found, vanilla is not searched |
| `onlyFirst` | When `true`, returns only the first matching custom recipe |
| `sourceRecipes` | The list of recipes to search (defaults to all registered recipes) |

```kotlin
val player: Player = /* ... */
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)

val result: Search.SearchResult = Search.search(player.uniqueId, view)
```

---

## Search.asyncSearch (asynchronous search)

```kotlin
fun asyncSearch(
    crafterID: UUID,
    view: CraftView,
    query: SearchQuery = SearchQuery.ASYNC_DEFAULT,
    sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
): CompletableFuture<SearchResult>
```

An asynchronous search method using virtual threads (available since 5.0.17).
Each recipe search is executed in parallel on individual threads, making it especially effective when there are many recipes with heavy predicates that call databases or external APIs.
This method is used internally by CustomCrafterAPI's standard crafting screen and the all-candidates display feature.

:::caution
Asynchronous threads cannot access BukkitAPI worlds or entities.
When accessing player information inside `ResultSupplier` or `CMatterPredicate`, use `asyncContext.isAsync()` to check whether the context is asynchronous.
:::

```kotlin
val player: Player = /* ... */
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)

val future: CompletableFuture<Search.SearchResult> = Search.asyncSearch(player.uniqueId, view)
future.thenAccept { result ->
    // Process results asynchronously
    val customs = result.customs()
    println("Matching custom recipe count: ${customs.size}")
}
```

---

## SearchQuery

`SearchQuery` is the class that controls the search behavior of `asyncSearch`.

```kotlin
class SearchQuery(
    val searchMode: SearchMode,
    val vanillaSearchMode: VanillaSearchMode,
    val asyncContext: AsyncContext? = null
)
```

### SearchMode

| Value | Description |
|-------|-------------|
| `ALL` (default) | Returns all matching custom recipes |
| `ONLY_FIRST` | Returns only the first matching custom recipe. Cancels other search tasks as soon as one is found |

### VanillaSearchMode

| Value | Description |
|-------|-------------|
| `IF_CUSTOMS_NOT_FOUND` (default) | Searches vanilla only if no custom recipe is found |
| `FORCE` | Always searches vanilla regardless of whether custom recipes were found |

```kotlin
// Search in ONLY_FIRST mode
val query = Search.SearchQuery(
    searchMode = Search.SearchQuery.SearchMode.ONLY_FIRST,
    vanillaSearchMode = Search.SearchQuery.VanillaSearchMode.IF_CUSTOMS_NOT_FOUND,
    asyncContext = AsyncContext.ofTurnOff()
)
val future = Search.asyncSearch(player.uniqueId, view, query)
```

---

## SearchResult

`SearchResult` is the class that holds the search results.

| Method | Return type | Description |
|--------|-------------|-------------|
| `vanilla()` | `Recipe?` | The vanilla recipe. `null` if not found or not searched |
| `customs()` | `List<Pair<CRecipe, MappedRelation>>` | The list of matching custom recipes and their coordinate mappings |
| `size()` | `Int` | The total number of matches across vanilla and custom recipes |
| `getMergedResults()` | `List<Pair<CRecipe, MappedRelation?>>` | A combined list of vanilla and custom results. The vanilla entry has `null` for `MappedRelation` |

```kotlin
val result: Search.SearchResult = Search.search(player.uniqueId, view)

// Get vanilla recipe
val vanilla: Recipe? = result.vanilla()
vanilla?.let { println("Vanilla recipe: ${it.result.type}") }

// Get custom recipes
val customs: List<Pair<CRecipe, MappedRelation>> = result.customs()
customs.forEach { (recipe, relation) ->
    println("Custom recipe: ${recipe.name}")
}

// Get all results combined
result.getMergedResults().forEach { (recipe, relation) ->
    println("Recipe: ${recipe.name}, has coordinate mapping: ${relation != null}")
}
```

---

## VanillaSearch

`VanillaSearch` is an object for searching only vanilla recipes without going through the CustomCrafterAPI search flow.

```kotlin
// Example: search for a vanilla recipe to craft a furnace from cobblestone
val view = CraftView(
    materials = CoordinateComponent.squareFill(3)
        .filter { it.x < 3 && it.y < 3 }
        .associate { it to ItemStack.of(Material.COBBLESTONE) },
    result = ItemStack.empty()
)
val vanillaRecipe: Recipe? = VanillaSearch.search(Bukkit.getWorlds().first(), view)
vanillaRecipe?.let { println("Result item: ${it.result.type}") }
```

---

## MappedRelation and MappedRelationComponent

`MappedRelation` is a class that holds the correspondence between coordinates in the recipe and the actual input slot coordinates.
`MappedRelationComponent` represents a single correspondence pair (recipe coordinate → input coordinate).

```kotlin
data class MappedRelation(
    val components: Set<MappedRelationComponent>
)

data class MappedRelationComponent(
    val recipe: CoordinateComponent, // coordinate in the recipe
    val input: CoordinateComponent   // coordinate of the actual input slot
)
```

For example, if a shaped recipe requires stone at `(0,0)`, there are cases where the recipe still matches even if the player places the item at `(2,2)`.
In that case the `MappedRelationComponent` would be `recipe = (0,0), input = (2,2)`.
This information is passed as `ResultSupplier.Context.relation` and `CRecipePredicate.Context.relation`, and is used to track which item was placed in which slot.

---

## PartialSearch

`PartialSearch` provides asynchronous partial recipe match searches.
A partial match occurs when the player's current crafting grid satisfies some — but not necessarily all — of a recipe's required slots.
This is useful for crafting hints, recipe guides, and autocomplete suggestions.

Recipes implementing `UnPartialSearchableRecipe` are excluded from all partial searches.

```kotlin
fun asyncPartialSearch(
    crafterId: UUID,
    view: CraftView,
    searchQuery: SearchQuery = SearchQuery.ASYNC_DEFAULT,
    sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
): CompletableFuture<List<PartialSearchResult>>
```

### PartialSearchResult

Each entry in the returned list implements `PartialSearchResult`:

| Method | Return type | Description |
|--------|-------------|-------------|
| `recipe` | `CRecipe` | The candidate recipe that was evaluated |
| `matched()` | `Set<CoordinateComponent>` | Recipe slot coordinates covered by at least one input item |
| `notEnough()` | `Set<CoordinateComponent>` | Recipe slot coordinates that have no matching input item |
| `state()` | `MatchState` | `ALL` if all slots are satisfied; `PARTIAL_NOT_ENOUGH` otherwise |

`PartialShapedResult` is returned for shaped recipes and includes a `relation: MappedRelation`.
`PartialShapelessResult` is returned for shapeless recipes and exposes `weakRelations()` — a matter-keyed map of compatible input slots.

### Example

```kotlin
val player: Player = /* ... */
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)

PartialSearch.asyncPartialSearch(player.uniqueId, view).thenAccept { results ->
    results.forEach { result ->
        println("Recipe: ${result.recipe.name}, state: ${result.state()}")
        if (result.notEnough().isNotEmpty()) {
            println("  Missing slots: ${result.notEnough()}")
        }
    }
}
```