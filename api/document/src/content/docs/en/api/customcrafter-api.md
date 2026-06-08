---
title: CustomCrafterAPI Object
---

## What is CustomCrafterAPI

`CustomCrafterAPI` is a Kotlin `object` that centrally manages retrieval, modification, and reset of CustomCrafterAPI configuration values, as well as recipe registration and deregistration.
It is used for initial setup in a plugin's `onEnable`, and for dynamically adding or removing recipes at runtime.

---

## Constants

| Constant | Type | Description |
|----------|------|-------------|
| `MAJOR_VERSION` | `Int` | Major version number; incremented on incompatible API changes |
| `MINOR_VERSION` | `Int` | Minor version number; incremented on backward-compatible additions |
| `PATCH_VERSION` | `Int` | Patch version number; incremented on backward-compatible bug fixes |
| `API_VERSION` | `String` | Version string in `"MAJOR.MINOR.PATCH"` format |
| `VERSION_TYPE` | `VersionType` | Release stage: `ALPHA`, `BETA`, `RC`, or `STABLE` |
| `AUTHORS` | `Set<String>` | The set of CustomCrafterAPI author names |

`VersionType` is an enum with values `ALPHA`, `BETA`, `RC`, and `STABLE`. Use `VERSION_TYPE` to check the current release stage:

```kotlin
if (CustomCrafterAPI.VERSION_TYPE != CustomCrafterAPI.VersionType.STABLE) {
    logger.warning("CustomCrafter API is not a stable release: ${CustomCrafterAPI.VERSION_TYPE}")
}
```

### Version compatibility check

`hasFullCompatibility(version: String)` is deprecated as of 5.2.0 and will be removed in 6.0.0.
Use `MAJOR_VERSION`, `MINOR_VERSION`, `PATCH_VERSION`, and `VERSION_TYPE` for version checks instead.

```kotlin
override fun onEnable() {
    if (CustomCrafterAPI.MAJOR_VERSION < 5 || CustomCrafterAPI.MINOR_VERSION < 2) {
        logger.warning("This plugin requires CustomCrafterAPI 5.2.0 or later.")
        logger.warning("Loaded version: ${CustomCrafterAPI.API_VERSION}")
        server.pluginManager.disablePlugin(this)
        return
    }
}
```

---

## Recipe Management

### Retrieving recipes

`getRecipes()` returns all registered recipes as an immutable list.
Modifying the returned list does not affect recipe registration or deregistration.

```kotlin
val recipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
recipes.forEach { recipe -> println(recipe.name) }
```

`getRegisteredRecipeNames()` returns a set of all registered recipe names.

```kotlin
val names: Set<String> = CustomCrafterAPI.getRegisteredRecipeNames()
```

`getRegisteredRecipeFromName(filter)` returns recipes whose names satisfy the given predicate.
`getRegisteredRecipeFromName(recipeName)` returns recipes with a name exactly matching `recipeName`.

```kotlin
// Recipes whose name starts with "custom-"
val filtered: List<CRecipe> = CustomCrafterAPI.getRegisteredRecipeFromName { it.startsWith("custom-") }

// Recipes with the exact name "stone-to-coal"
val exact: List<CRecipe> = CustomCrafterAPI.getRegisteredRecipeFromName("stone-to-coal")
```

`getRegisteredRecipeFromPlugin(plugin)` returns all recipes registered by the specified plugin instance.

```kotlin
val mine: List<CRecipe> = CustomCrafterAPI.getRegisteredRecipeFromPlugin(this)
```

### Registering recipes

Use `registerRecipe(recipes: List<CRecipe>, plugin: JavaPlugin)` to register recipes.

- Validation via `CRecipe.isValidRecipe()` is performed at registration time; an `IllegalStateException` is thrown if validation fails.
- If the current `RecipeNameStrictLevel` detects a duplicate name, an `IllegalArgumentException` is thrown.
- After a successful registration, `RegisterCustomRecipeEvent` is fired.

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val recipe = CRecipeImpl(
    name = "stone-to-coal",
    items = mapOf(CoordinateComponent(0, 0) to stone),
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.COAL))),
    type = CRecipe.Type.SHAPED
)

CustomCrafterAPI.registerRecipe(listOf(recipe))
```

### Deregistering recipes

Use `unregisterRecipe(name: String?, plugin: JavaPlugin?)` to deregister recipes matching the given conditions.
`unregisterAllRecipes()` deregisters all recipes at once.
Both fire an `UnregisterCustomRecipeEvent`.

| Argument | Default | Behavior when `null` |
|----------|---------|----------------------|
| `name` | — | Name is not used as a filter |
| `plugin` | `CustomCrafter.getInstance()` | Plugin is not used as a filter. When both are `null`, all recipes are deregistered |

```kotlin
// Deregister a recipe by name (registered by the default plugin)
CustomCrafterAPI.unregisterRecipe(name = "stone-to-coal")

// Deregister all recipes registered by a specific plugin
CustomCrafterAPI.unregisterRecipe(plugin = myPlugin)

// Deregister all recipes regardless of plugin
CustomCrafterAPI.unregisterRecipe(name = null, plugin = null)

// Deregister all recipes
CustomCrafterAPI.unregisterAllRecipes()
```

---

## Configuration Properties

Each configuration property has a corresponding `DEFAULT_XXX` constant.
To reset a property to its default value, pass the constant to the `set` method:

```kotlin
CustomCrafterAPI.setBaseBlock(CustomCrafterAPI.DEFAULT_BASE_BLOCK)
```

When a configuration value is changed, `CustomCrafterAPIPropertiesChangeEvent` is fired.
The second argument of `setXxx(value, calledAsync = false)` should be set to `true` when calling from an asynchronous thread.

### BaseBlock

The type of block that, when placed directly beneath a crafting table, triggers the dedicated UI to open.
Default: `Material.GOLD_BLOCK` (`DEFAULT_BASE_BLOCK`).
If a non-block `Material` is specified, an `IllegalArgumentException` is thrown.

```kotlin
val current: Material = CustomCrafterAPI.getBaseBlock()

CustomCrafterAPI.setBaseBlock(Material.DIAMOND_BLOCK)

// Reset to default
CustomCrafterAPI.setBaseBlock(CustomCrafterAPI.DEFAULT_BASE_BLOCK)
```

### BaseBlockSide

The side length of the block arrangement required directly beneath the crafting table.
Default: `3` (3×3) (`DEFAULT_BASE_BLOCK_SIDE`). Only odd numbers of 3 or greater are valid; any other value causes the method to return `false`.

```kotlin
val success: Boolean = CustomCrafterAPI.setBaseBlockSideSize(5)

// Reset to default (3)
CustomCrafterAPI.setBaseBlockSideSize(CustomCrafterAPI.DEFAULT_BASE_BLOCK_SIDE)
```

### ResultGiveCancel

When set to `true`, CustomCrafterAPI will not deliver the crafted item to the player — instead, that responsibility is delegated to the calling plugin.
Use this when you want to implement your own result-delivery logic.
Default: `false` (`DEFAULT_RESULT_GIVE_CANCEL`).

```kotlin
CustomCrafterAPI.setResultGiveCancel(true)

// Reset to default
CustomCrafterAPI.setResultGiveCancel(CustomCrafterAPI.DEFAULT_RESULT_GIVE_CANCEL)
```

### UseCustomCraftUI

When `true` (the default), clicking a crafting table opens the CustomCrafterAPI dedicated UI.
When set to `false`, clicking a crafting table will no longer open the dedicated UI.
Default: `true` (`DEFAULT_USE_CUSTOM_CRAFT_UI`).

```kotlin
CustomCrafterAPI.setUseCustomCraftUI(false)

// Reset to default
CustomCrafterAPI.setUseCustomCraftUI(CustomCrafterAPI.DEFAULT_USE_CUSTOM_CRAFT_UI)
```

### UseMultipleResultCandidateFeature

Enables a mode that displays all matching recipe candidates to the player when multiple recipes match the input items.
When `false` (the default), only the first recipe found is used.
Default: `false` (`DEFAULT_USE_MULTIPLE_RESULT_CANDIDATE_FEATURE`).

```kotlin
CustomCrafterAPI.setUseMultipleResultCandidateFeature(true)

// Reset to default
CustomCrafterAPI.setUseMultipleResultCandidateFeature(CustomCrafterAPI.DEFAULT_USE_MULTIPLE_RESULT_CANDIDATE_FEATURE)
```

### RecipeNameStrictLevel

Controls how strictly duplicate recipe names are checked during registration.
The level can only be changed in the stricter direction; loosening is not permitted.
Default: `NameStrictLevel.NOTHING` (`DEFAULT_RECIPE_NAME_STRICT_LEVEL`).

| Level | Behavior |
|-------|----------|
| `NOTHING` | No duplicate check |
| `WEAK` | Rejects a recipe if its name exactly matches an already-registered name |
| `STRICT` | Rejects a recipe if its name, with whitespace removed, matches an already-registered name |

```kotlin
CustomCrafterAPI.setRecipeNameStrictLevel(CustomCrafterAPI.NameStrictLevel.WEAK)

val current: CustomCrafterAPI.NameStrictLevel = CustomCrafterAPI.getRecipeNameStrictLevel()
```

### CraftUIDesigner

Sets the designer that customizes the appearance of the crafting UI.
See the [CraftUIDesigner page](/en/extra/craftui-designer/) for details.
Default: `CraftUIDesigner.DEFAULT` (`DEFAULT_CRAFT_UI_DESIGNER`).

```kotlin
val myDesigner = object : CraftUIDesigner {
    override fun title(context: CraftUIDesigner.Context) = Component.text("My Crafter")
    // ... implement other methods
}
CustomCrafterAPI.setCraftUIDesigner(myDesigner)

// Reset to default
CustomCrafterAPI.setCraftUIDesigner(CustomCrafterAPI.DEFAULT_CRAFT_UI_DESIGNER)
```

### AllCandidateUIDesigner

Sets the designer that customizes the appearance of the AllCandidateUI.
See the [AllCandidateUIDesigner page](/en/extra/allcandidateui-designer/) for details.
Default: `AllCandidateUIDesigner.DEFAULT` (`DEFAULT_ALL_CANDIDATE_UI_DESIGNER`).

```kotlin
val myDesigner = object : AllCandidateUIDesigner {
    override fun title(context: AllCandidateUIDesigner.Context) = Component.text("All Recipes")
    // ... override other methods as needed
}
CustomCrafterAPI.setAllCandidateUIDesigner(myDesigner)

// Reset to default
CustomCrafterAPI.setAllCandidateUIDesigner(CustomCrafterAPI.DEFAULT_ALL_CANDIDATE_UI_DESIGNER)
```
