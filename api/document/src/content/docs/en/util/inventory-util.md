---
title: InventoryUtil
---

## What is InventoryUtil

`InventoryUtil` is a utility object that assists with player inventory operations.

---

## Player.giveItems()

An extension function that gives items to a `Player`.
If there is no space in the inventory, the items are dropped at the player's feet.

```kotlin
fun Player.giveItems(
    saveLimit: Boolean = true,
    vararg items: ItemStack
)
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `saveLimit` | `true` | Whether to split stacks taking `Material.maxStackSize` into account |
| `items` | — | Items to give |

When `saveLimit = true`, if the item count exceeds the maximum stack size for that material, the stack is automatically split and added to the inventory.

### Usage Example

```kotlin
import io.github.sakaki_aruka.customcrafter.util.InventoryUtil.giveItems

// Give the player 64 diamonds
player.giveItems(ItemStack.of(Material.DIAMOND, 64))

// Give items without stack splitting
player.giveItems(saveLimit = false, ItemStack.of(Material.DIAMOND, 128))
```

This can be used when you want to give items directly to a player inside a `ResultSupplier`.
However, because accessing the player is not safe during asynchronous execution (`asyncContext != null`), use this only during synchronous execution.

```kotlin
val supplier = ResultSupplier { ctx ->
    if (ctx.asyncContext != null) {
        // During async execution, just return the items
        return@ResultSupplier listOf(ItemStack.of(Material.DIAMOND))
    }
    // During sync execution, items can also be given directly to the inventory
    val player = Bukkit.getPlayer(ctx.crafterId)
    player?.giveItems(ItemStack.of(Material.EXPERIENCE_BOTTLE, 16))
    emptyList()
}
```
