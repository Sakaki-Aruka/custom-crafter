---
title: About CraftUIDesigner
---

## What is CraftUIDesigner?

`CraftUIDesigner` is an interface for customizing the appearance of the crafting UI in CustomCrafterAPI.
By registering your implemented designer via `CustomCrafterAPI.setCraftUIDesigner(designer)`, you can change the title and slot layout of the crafting UI.

There are three customizable elements:

| Method | Description |
|----------|------|
| `title(context)` | Title of the crafting UI |
| `makeButton(context)` | Slot coordinates and icon item for the craft button |
| `blankSlots(context)` | Mapping of non-clickable decorative slot coordinates to their icon items |

All methods have default implementations, so you only need to override the ones you want to customize.

All methods receive a `CraftUIDesigner.Context`, so you can reference `context.player` to return a different design for each player.

:::note
Slot numbers in the inventory managed by CustomCrafterAPI (54 slots) can be converted to and from indices using `CoordinateComponent.toIndex()` and `CoordinateComponent.fromIndex(index)`.
The craft slots (6×6) must occupy the 36 cells that are not the craft button or blank slots.
:::

---

## Default Implementation

To reset the configuration, call `CustomCrafterAPI.setCraftUIDesigner(CustomCrafterAPI.DEFAULT_CRAFT_UI_DESIGNER)`.

The default layout is as follows:
- Craft button: index `35` (anvil icon)
- Blank slots: the slots in the right 3 columns (column indices 6–8)

```kotlin
// Outline of the default implementation
const val MAKE_BUTTON = 35

override fun title(context: CraftUIDesigner.Context): Component {
    return Component.text("Custom Crafter")
}

override fun makeButton(context: CraftUIDesigner.Context): Pair<CoordinateComponent, ItemStack> {
    return CoordinateComponent.fromIndex(MAKE_BUTTON) to ItemStack(Material.ANVIL).apply {
        itemMeta = itemMeta.apply {
            customName(Component.text("Making items"))
        }
    }
}

override fun blankSlots(context: CraftUIDesigner.Context): Map<CoordinateComponent, ItemStack> {
    val blank = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply { displayName(Component.empty()) }
    }
    return (0..<54)
        .filter { it % 9 >= 6 }
        .minus(MAKE_BUTTON)
        .associate { CoordinateComponent.fromIndex(it) to blank }
}
```

---

## Custom Designer Implementation Examples

### Simple Customization

```kotlin
val myDesigner = object : CraftUIDesigner {
    override fun title(context: CraftUIDesigner.Context): Component {
        // Display the player's name in the UI title
        val name = context.player?.name ?: "Unknown"
        return Component.text("$name's Crafter")
    }

    override fun makeButton(context: CraftUIDesigner.Context): Pair<CoordinateComponent, ItemStack> {
        val button = ItemStack.of(Material.EMERALD).apply {
            editMeta { meta -> meta.displayName(Component.text("Craft!")) }
        }
        return CoordinateComponent.fromIndex(35) to button
    }

    override fun blankSlots(context: CraftUIDesigner.Context): Map<CoordinateComponent, ItemStack> {
        val glass = ItemStack.of(Material.CYAN_STAINED_GLASS_PANE).apply {
            editMeta { meta -> meta.displayName(Component.empty()) }
        }
        return (0..<54)
            .filter { it % 9 >= 6 }
            .minus(35) // makeButton
            .associate { CoordinateComponent.fromIndex(it) to glass }
    }
}

CustomCrafterAPI.setCraftUIDesigner(myDesigner)
```

### Registering on Plugin Startup

```kotlin
class MyPlugin : JavaPlugin() {
    override fun onEnable() {
        if (!CustomCrafterAPI.hasFullCompatibility("5.0.20")) {
            logger.warning("CustomCrafterAPI version is not compatible.")
            server.pluginManager.disablePlugin(this)
            return
        }

        val designer = MyDesigner()
        CustomCrafterAPI.setCraftUIDesigner(designer)
    }

    override fun onDisable() {
        // Reset to default to avoid affecting other plugins
        CustomCrafterAPI.setCraftUIDesigner(CustomCrafterAPI.DEFAULT_CRAFT_UI_DESIGNER)
    }
}
```

---

## Baked Class

`CraftUIDesigner.Baked` is an immutable class that holds the result of evaluating each method of `CraftUIDesigner` for a specific context.

```kotlin
val context = CraftUIDesigner.Context(player = null)
val baked: CraftUIDesigner.Baked = myDesigner.bake(context)
```

The main methods provided by `Baked` are as follows:

| Method | Description |
|----------|------|
| `apply(ui: Inventory)` | Places the button and blank slot items into the inventory |
| `craftSlots()` | Returns a list of coordinates for the 36 craftable slots |
| `isValid()` | Validates whether the craft slots form a 6×6 square |

:::note
When `setCraftUIDesigner` is called, `designer.bake(Context(player = null))` is executed internally and an `isValid()` check is performed.
If the craft slots do not form a 6×6 square of 36 slots, an exception is thrown and the designer registration fails.
:::
