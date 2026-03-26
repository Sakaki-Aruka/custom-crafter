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
| `API_VERSION` | `String` | The API version string (does not include the patch version) |
| `IS_STABLE` | `Boolean` | Whether this is a stable release |
| `IS_BETA` | `Boolean` | Whether this is a beta release |
| `AUTHORS` | `Set<String>` | The set of CustomCrafterAPI author names |

### Version compatibility check

You can use `hasFullCompatibility(version: String)` to verify that the version your plugin depends on is fully compatible with the CustomCrafterAPI loaded at runtime.
If they are not compatible, it is recommended to take appropriate action such as disabling the plugin.

```kotlin
override fun onEnable() {
    if (!CustomCrafterAPI.hasFullCompatibility("5.0.20")) {
        logger.warning("This plugin is not compatible with the currently loaded CustomCrafterAPI.")
        logger.warning("Loaded version: ${CustomCrafterAPI.API_VERSION}")
        server.pluginManager.disablePlugin(this)
        return
    }
}
```

---

## Recipe management

### Retrieving recipes

`getRecipes()` returns all registered recipes as an immutable list.
Modifying the returned list does not affect recipe registration or deregistration.

```kotlin
val recipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
recipes.forEach { recipe -> println(recipe.name) }
```

### Registering recipes

Use `registerRecipe(vararg recipes: CRecipe)` to register recipes.
Validation via `CRecipe.isValidRecipe()` is performed at registration time. If validation fails, an `IllegalStateException` is thrown.
Registration also fires a `RegisterCustomRecipeEvent`, and if that event is cancelled, registration will fail.

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val recipe = CRecipeImpl(
    name = "stone-to-coal",
    items = mapOf(CoordinateComponent(0, 0) to stone),
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.COAL))),
    type = CRecipe.Type.SHAPED
)

val success: Boolean = CustomCrafterAPI.registerRecipe(recipe)
if (!success) {
    // If RegisterCustomRecipeEvent was cancelled
    println("Recipe registration was cancelled.")
}
```

### Deregistering recipes

Use `unregisterRecipe(vararg recipes: CRecipe)` to deregister recipes.
You can also use `unregisterAllRecipes()` to deregister all recipes at once.
Both fire an `UnregisterCustomRecipeEvent`, and if it is cancelled, deregistration will fail.

```kotlin
// Unregister a specific recipe
CustomCrafterAPI.unregisterRecipe(recipe)

// Unregister all recipes
val success: Boolean = CustomCrafterAPI.unregisterAllRecipes()
```

---

## Configuration properties

Each configuration property is managed via three kinds of methods: `get` / `set` / `setDefault`.
When a configuration value is changed, `CustomCrafterAPIPropertiesChangeEvent` is fired.
The second argument of `setXxx(value, calledAsync = false)` should be set to `true` when calling from an asynchronous thread.

### BaseBlock

The type of block that, when placed directly beneath a crafting table, triggers the dedicated UI to open.
The default is `Material.GOLD_BLOCK`.
If a non-block `Material` is specified, an `IllegalArgumentException` is thrown.

```kotlin
// Get current setting
val current: Material = CustomCrafterAPI.getBaseBlock()

// Change to diamond block
CustomCrafterAPI.setBaseBlock(Material.DIAMOND_BLOCK)

// Reset to default (gold block)
CustomCrafterAPI.setBaseBlockDefault()
```

### BaseBlockSide

The side length of the block arrangement required directly beneath the crafting table.
Default is `3` (3×3). Only odd numbers of 3 or greater are valid; any other value causes the method to return `false`.

```kotlin
// Change to require a 5×5 arrangement
val success: Boolean = CustomCrafterAPI.setBaseBlockSideSize(5)

// Reset to default (3)
CustomCrafterAPI.setBaseBlockSideSizeDefault()
```

### ResultGiveCancel

When set to `true`, CustomCrafterAPI will not deliver the crafted item to the player when an item is created in the UI — instead, that responsibility is delegated to the calling plugin.
Use this when you want to implement your own result-delivery logic. The default is `false`.

```kotlin
// When you want to handle item delivery yourself
CustomCrafterAPI.setResultGiveCancel(true)

// Reset to default (false)
CustomCrafterAPI.setResultGiveCancelDefault()
```

### UseCustomCraftUI

When `true` (the default), clicking a crafting table opens the CustomCrafterAPI dedicated UI.
When set to `false`, clicking a crafting table will no longer open the dedicated UI.

```kotlin
CustomCrafterAPI.setUseCustomCraftUI(false) // Disable the dedicated UI
CustomCrafterAPI.setUseCustomCraftUIDefault() // Reset to default (true)
```

### UseMultipleResultCandidateFeature

Enables a mode that displays all matching recipe candidates to the player when multiple recipes match the input items.
When `false` (the default), only the first recipe found is used.

```kotlin
CustomCrafterAPI.setUseMultipleResultCandidateFeature(true)
CustomCrafterAPI.setUseMultipleResultCandidateFeatureDefault()
```

### CraftUIDesigner

Sets the designer that customizes the appearance of the crafting UI.
See the [CraftUIDesigner page](/ja/extra/craftui-designer/) for details.

```kotlin
val myDesigner = object : CraftUIDesigner {
    override fun title(context: CraftUIDesigner.Context) = Component.text("My Crafter")
    // ... implement other methods
}
CustomCrafterAPI.setCraftUIDesigner(myDesigner)
CustomCrafterAPI.setCraftUIDesignerDefault() // Reset to default
```

### AllCandidateNotDisplayableItem

Sets the item displayed in place of a recipe slot that cannot be shown in the all-candidates display mode.
Pass a lambda to `loreSupplier` that receives a recipe name and returns a lore list.

```kotlin
val icon = ItemStack.of(Material.BARRIER)
CustomCrafterAPI.setAllCandidateNotDisplayableItem(icon) { recipeName ->
    listOf(Component.text("Not displayable: $recipeName"))
}
```
