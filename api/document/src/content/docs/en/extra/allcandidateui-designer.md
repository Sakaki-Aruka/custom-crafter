---
title: About AllCandidateUIDesigner
---

## What is AllCandidateUIDesigner?

`AllCandidateUIDesigner` is an interface for customizing the AllCandidateUI — the screen that displays all matching recipe candidates when `UseMultipleResultCandidateFeature` is enabled.
By registering your implemented designer via `CustomCrafterAPI.setAllCandidateUIDesigner(designer)`, you can change the title, button positions, recipe slot layout, and placeholder items.

There are seven customizable elements:

| Method | Description |
|--------|-------------|
| `title(context)` | Title of the AllCandidateUI inventory |
| `previousPageButton(context)` | Slot coordinate and icon item for the previous-page button |
| `nextPageButton(context)` | Slot coordinate and icon item for the next-page button |
| `backToCraftUIButton(context)` | Slot coordinate and icon item for the back-to-CraftUI button |
| `recipeSlots(context)` | Set of slot coordinates available for recipe icons |
| `noDisplayableItem(context)` | Icon shown when a recipe cannot produce a displayable item |
| `ungeneratedIconPlaceholderItem(context)` | Factory lambda producing a placeholder icon while a recipe icon is being generated |

All methods have default implementations, so only the parts that need customization must be overridden.
All methods receive an `AllCandidateUIDesigner.Context` that exposes `context.searchResult` and `context.crafterId`.

:::note
Slot numbers in the inventory managed by CustomCrafterAPI (54 slots) can be converted to and from indices using `CoordinateComponent.toIndex()` and `CoordinateComponent.fromIndex(index)`.
The `recipeSlots` set must contain between 1 and 51 elements and must not overlap with any button coordinate.
If 45 recipes are available but `recipeSlots` returns 30 slots, the AllCandidateUI will paginate across 2 pages.
:::

---

## Default Implementation

The default configuration uses the following layout:
- Previous-page button: index `45`
- Next-page button: index `53`
- Back-to-CraftUI button: index `49` (crafting table icon)
- Recipe slots: indices `0–44` (5 rows × 9 columns)

To reset the configuration, call:

```kotlin
CustomCrafterAPI.setAllCandidateUIDesigner(CustomCrafterAPI.DEFAULT_ALL_CANDIDATE_UI_DESIGNER)
```

---

## Custom Designer Implementation Examples

### Simple Customization

```kotlin
val myDesigner = object : AllCandidateUIDesigner {
    override fun title(context: AllCandidateUIDesigner.Context): Component {
        return Component.text("All Candidates")
    }

    override fun recipeSlots(context: AllCandidateUIDesigner.Context): Set<CoordinateComponent> {
        // Use only the top 3 rows (indices 0–26) for recipes
        return (0..<27).map { CoordinateComponent.fromIndex(it) }.toSet()
    }

    override fun noDisplayableItem(context: AllCandidateUIDesigner.Context): ItemStack {
        return ItemStack.of(Material.BARRIER).apply {
            editMeta { meta -> meta.displayName(Component.text("Not displayable")) }
        }
    }
}

CustomCrafterAPI.setAllCandidateUIDesigner(myDesigner)
```

### Player-Specific Customization

`context.crafterId` and `context.searchResult` allow per-player or per-result customization:

```kotlin
val myDesigner = object : AllCandidateUIDesigner {
    override fun title(context: AllCandidateUIDesigner.Context): Component {
        val playerName = Bukkit.getPlayer(context.crafterId)?.name ?: "Unknown"
        return Component.text("$playerName's Candidates (${context.searchResult.size()} results)")
    }
}

CustomCrafterAPI.setAllCandidateUIDesigner(myDesigner)
```

### Registering on Plugin Startup

```kotlin
class MyPlugin : JavaPlugin() {
    override fun onEnable() {
        val designer = MyAllCandidateDesigner()
        CustomCrafterAPI.setAllCandidateUIDesigner(designer)
    }

    override fun onDisable() {
        // Reset to default to avoid affecting other plugins
        CustomCrafterAPI.setAllCandidateUIDesigner(CustomCrafterAPI.DEFAULT_ALL_CANDIDATE_UI_DESIGNER)
    }
}
```

---

## Baked Class

`AllCandidateUIDesigner.Baked` is an immutable snapshot of a resolved `AllCandidateUIDesigner` with all values fixed at bake time.
Obtained by calling `designer.bake(context)` or `designer.bakeWithEmptyContext()`.

```kotlin
val context = AllCandidateUIDesigner.Context.emptyContext()
val baked: AllCandidateUIDesigner.Baked = myDesigner.bake(context)
```

| Method / Property | Type | Description |
|-------------------|------|-------------|
| `isValid()` | `Result<Unit>` | Validates the baked values; returns `Result.success` or `Result.failure` with a descriptive exception |
| `ungeneratedIcon(recipe)` | `ItemStack` | Returns the placeholder icon for `recipe`; falls back to the default placeholder if the produced item is not displayable |
| `recipeSlotsIndex` | `Set<Int>` | The same slot coordinates as `recipeSlots`, expressed as slot indices |

:::note
When `setAllCandidateUIDesigner` is called, `designer.bakeWithEmptyContext()` is executed internally and an `isValid()` check is performed.
If the baked values are invalid — for example, `recipeSlots` is empty, has more than 51 elements, or overlaps with a button coordinate — an exception is thrown and the designer registration fails.
:::
