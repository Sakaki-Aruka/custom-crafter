---
title: Events
---

## CustomCrafterAPI Events

CustomCrafterAPI fires several custom events that implement Bukkit's `Event`.
These can be listened to and handled in the same way as ordinary Bukkit events.

---

## RegisterCustomRecipeEvent

Fired when a recipe is about to be registered via `CustomCrafterAPI.registerRecipe()`.
Implements `Cancellable`; cancelling the event causes the recipe registration to fail.

| Property | Type | Description |
|------------|-----|------|
| `recipes` | `List<CRecipe>` | The list of recipes that are about to be registered |

```kotlin
class RecipeRegisterListener : Listener {
    @EventHandler
    fun onRegister(event: RegisterCustomRecipeEvent) {
        event.recipes.forEach { recipe ->
            println("Recipe registered: ${recipe.name}")
        }

        // Example: cancel registration of recipes whose name starts with "forbidden-"
        if (event.recipes.any { it.name.startsWith("forbidden-") }) {
            event.isCancelled = true
            println("Registration cancelled.")
        }
    }
}
```

---

## UnregisterCustomRecipeEvent

Fired when a recipe is about to be unregistered via `CustomCrafterAPI.unregisterRecipe()` or `CustomCrafterAPI.unregisterAllRecipes()`.
Implements `Cancellable`; cancelling the event causes the recipe unregistration to fail.

| Property | Type | Description |
|------------|-----|------|
| `recipes` | `List<CRecipe>` | The list of recipes that are about to be unregistered |

```kotlin
class RecipeUnregisterListener : Listener {
    @EventHandler
    fun onUnregister(event: UnregisterCustomRecipeEvent) {
        event.recipes.forEach { recipe ->
            println("Recipe unregistered: ${recipe.name}")
        }

        // Example: prevent a specific recipe from being unregistered
        if (event.recipes.any { it.name == "protected-recipe" }) {
            event.isCancelled = true
        }
    }
}
```

---

## CreateCustomItemEvent

Fired when an item is crafted (i.e., a craft operation is executed).
This event is not cancellable.

| Property | Type | Description |
|------------|-----|------|
| `player` | `Player` | The player who performed the craft |
| `view` | `CraftView` | The UI input state at the time of crafting |
| `result` | `Search.SearchResult?` | The search result. `null` indicates the craft did not succeed |
| `shiftUsed` | `Boolean` | Whether the player bulk-crafted by holding Shift (since 5.0.17) |
| `isAsync` | `Boolean` | Whether the call originated from an asynchronous thread (since 5.0.17) |

```kotlin
class CreateItemListener : Listener {
    @EventHandler
    fun onCreateItem(event: CreateCustomItemEvent) {
        val result = event.result ?: return

        // Log the name of the custom recipe that was crafted
        result.customs().forEach { (recipe, _) ->
            println("${event.player.name} crafted ${recipe.name}.")
        }

        // Example: perform additional processing when bulk-crafting
        if (event.shiftUsed) {
            println("Bulk crafting was used.")
        }
    }
}
```

---

## CustomCrafterAPIPropertiesChangeEvent

Fired when a mutable property (configuration value) of `CustomCrafterAPI` is changed.
Both the old and new values are included.

This event is generic and carries the type `T` of the changed property.

| Property | Type | Description |
|------------|-----|------|
| `propertyName` | `String` | The name of the changed property |
| `oldValue` | `Property<T>` | The value before the change |
| `newValue` | `Property<T>` | The value after the change |
| `isAsync` | `Boolean` | Whether the change originated from an asynchronous thread |

To extract a value from `Property<T>` in a type-safe manner, use `PropertyKey<T>`.

```kotlin
val key = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK
val value: Material? = property.getOrNull(key)
```

The predefined `PropertyKey` values are as follows:

| Key | Corresponding Type | Corresponding Property |
|------|-----------|----------------|
| `RESULT_GIVE_CANCEL` | `Boolean` | ResultGiveCancel |
| `BASE_BLOCK` | `Material` | BaseBlock |
| `USE_MULTIPLE_RESULT_CANDIDATE_FEATURE` | `Boolean` | UseMultipleResultCandidateFeature |
| `USE_CUSTOM_CRAFT_UI` | `Boolean` | UseCustomCraftUI |
| `BASE_BLOCK_SIDE` | `Int` | BaseBlockSide |
| `CRAFT_UI_DESIGNER` | `CraftUIDesigner` | CraftUIDesigner |

### Example: Detecting a Base Block Change

```kotlin
class PropertiesChangeListener : Listener {
    @EventHandler
    fun <T> onPropertiesChange(event: CustomCrafterAPIPropertiesChangeEvent<T>) {
        val key = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK

        // Filter to only the relevant event by property name
        if (event.propertyName != key.propertyName) return

        val oldBlock: Material = event.oldValue.getOrNull(key) ?: return
        val newBlock: Material = event.newValue.getOrNull(key) ?: return

        println("Base block changed from ${oldBlock.name} to ${newBlock.name}.")
    }
}
```

---

## CraftInputInterruptEvent

Fired when a player interacts with the crafting input slots or closes the CraftUI while a craft process (recipe search or result generation) is already in progress, causing the ongoing process to be interrupted.
This event is not cancellable.

| Property | Type | Description |
|------------|-----|------|
| `interrupter` | `Player` | The player who caused the interruption |
| `isAsync` | `Boolean` | Whether the event was fired from an asynchronous thread |

```kotlin
class CraftInputInterruptListener : Listener {
    @EventHandler
    fun onInterrupt(event: CraftInputInterruptEvent) {
        println("${event.interrupter.name} interrupted an ongoing craft.")
    }
}
```

---

## PreventDoubleCraftEvent

Fired when a player attempts to start a new craft while a craft process (recipe search or result generation) is already running, causing the new attempt to be blocked.
This event is not cancellable.

| Property | Type | Description |
|------------|-----|------|
| `player` | `Player` | The player who attempted the double craft |
| `isAsync` | `Boolean` | Whether the event was fired from an asynchronous thread |

```kotlin
class PreventDoubleCraftListener : Listener {
    @EventHandler
    fun onPrevent(event: PreventDoubleCraftEvent) {
        event.player.sendMessage("Please wait for the current craft to finish.")
    }
}
```

---

## ResultItemGiveFailEvent

Fired when CustomCrafterAPI fails to deliver one or more result items to the player (for example, when the player's inventory is full).
This event is not cancellable.

Use `getResultsIfNotObtained()` to claim the remaining items and handle delivery yourself.
Once claimed, subsequent calls to `getResultsIfNotObtained()` return `null`.

| Property / Method | Type | Description |
|------------|-----|------|
| `usedSupplierContext` | `ResultSupplier.Context?` | The context used by the `ResultSupplier` that produced the items; `null` if unavailable |
| `getResultsIfNotObtained()` | `List<ItemStack>?` | Returns the undistributed items and marks them as obtained. Returns `null` if already claimed |
| `isResultObtained()` | `Boolean` | Returns whether the items have already been claimed |

```kotlin
class ResultGiveFailListener : Listener {
    @EventHandler
    fun onResultGiveFail(event: ResultItemGiveFailEvent) {
        val items: List<ItemStack> = event.getResultsIfNotObtained() ?: return

        // Fall back: drop items at the player's location
        val player = event.usedSupplierContext?.crafterID
            ?.let { Bukkit.getPlayer(it) } ?: return
        items.forEach { player.world.dropItemNaturally(player.location, it) }
    }
}
```

---

### Example: Monitoring Multiple Properties Together

```kotlin
class AllPropertiesChangeListener : Listener {
    @EventHandler
    fun <T> onPropertiesChange(event: CustomCrafterAPIPropertiesChangeEvent<T>) {
        when (event.propertyName) {
            CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK.propertyName -> {
                val newValue = event.newValue.getOrNull(
                    CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK
                )
                println("Base block changed: $newValue")
            }
            CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI.propertyName -> {
                val newValue = event.newValue.getOrNull(
                    CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI
                )
                println("Custom UI enabled: $newValue")
            }
        }
    }
}
```
