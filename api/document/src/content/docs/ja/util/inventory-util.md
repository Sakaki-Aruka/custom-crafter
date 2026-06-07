---
title: InventoryUtil
---

## InventoryUtil とは

`InventoryUtil` はプレイヤーのインベントリ操作を補助するユーティリティオブジェクトです。

---

## Player.giveItems()

`Player` に対してアイテムを付与する拡張関数です。
インベントリに空きがない場合は足元にアイテムをドロップします。

```kotlin
fun Player.giveItems(
    saveLimit: Boolean = true,
    vararg items: ItemStack
)
```

| パラメータ | デフォルト | 概要 |
|------------|-----------|------|
| `saveLimit` | `true` | `Material.maxStackSize` を考慮してスタックを分割するかどうか |
| `items` | — | 付与するアイテム |

`saveLimit = true` の場合、アイテムの個数がそのマテリアルの最大スタック数を超えていると自動的に分割してインベントリに追加します。

### 使用例

```kotlin
import io.github.sakaki_aruka.customcrafter.util.InventoryUtil.giveItems

// ダイヤモンド 64 個をプレイヤーに付与する
player.giveItems(ItemStack.of(Material.DIAMOND, 64))

// スタック分割なしで付与する
player.giveItems(saveLimit = false, ItemStack.of(Material.DIAMOND, 128))
```

`ResultSupplier` 内でプレイヤーに直接アイテムを渡したい場合に利用できます。
ただし非同期実行時 (`asyncContext != null`) にはプレイヤーへのアクセスが安全でないため、同期実行時のみ使用してください。

```kotlin
val supplier = ResultSupplier { ctx ->
    if (ctx.asyncContext != null) {
        // 非同期時はアイテムを返すのみ
        return@ResultSupplier listOf(ItemStack.of(Material.DIAMOND))
    }
    // 同期時はインベントリへ直接付与することも可能
    val player = Bukkit.getPlayer(ctx.crafterID)
    player?.giveItems(ItemStack.of(Material.EXPERIENCE_BOTTLE, 16))
    emptyList()
}
```
