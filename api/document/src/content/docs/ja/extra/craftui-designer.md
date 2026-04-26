---
title: CraftUIDesigner について
---

## CraftUIDesigner とは

`CraftUIDesigner` は CustomCrafterAPI のクラフト UI の外観をカスタマイズするためのインターフェースです。
実装したデザイナーを `CustomCrafterAPI.setCraftUIDesigner(designer)` で設定することで、クラフト UI のタイトルやスロットレイアウトを変更できます。

カスタマイズできる要素は以下の 3 つです：

| メソッド | 概要 |
|----------|------|
| `title(context)` | クラフト UI のタイトル |
| `makeButton(context)` | 作成ボタンのスロット座標とアイコンアイテム |
| `blankSlots(context)` | クリック不可の装飾スロットの座標とアイコンアイテムの対応 |

すべてのメソッドは `CraftUIDesigner.Context` を受け取るため、`context.player` を参照することでプレイヤーごとに異なるデザインを返すことができます。

:::note
CustomCrafterAPI が管理するインベントリ (54 スロット) のスロット番号は `CoordinateComponent.toIndex()` と `CoordinateComponent.fromIndex(index)` で相互変換できます。
クラフトスロット (6×6) は作成ボタン・空白スロット以外の 36 マスである必要があります。
:::

---

## デフォルト実装

設定をリセットしたい場合は `CustomCrafterAPI.setCraftUIDesignerDefault()` を呼び出します。

デフォルトのレイアウトは以下のとおりです：
- 作成ボタン: インデックス `35` (アンビルアイコン)
- 空白スロット: 右側 3 列 (列インデックス 6〜8) のスロット

```kotlin
// デフォルト実装の概略
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

## カスタムデザイナーの実装例

### シンプルなカスタマイズ

```kotlin
val myDesigner = object : CraftUIDesigner {
    override fun title(context: CraftUIDesigner.Context): Component {
        // プレイヤー名を UI タイトルに表示する
        val name = context.player?.name ?: "Unknown"
        return Component.text("$name のクラフター")
    }

    override fun makeButton(context: CraftUIDesigner.Context): Pair<CoordinateComponent, ItemStack> {
        val button = ItemStack.of(Material.EMERALD).apply {
            editMeta { meta -> meta.displayName(Component.text("作成！")) }
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

### プラグイン起動時に登録する

```kotlin
class MyPlugin : JavaPlugin() {
    override fun onEnable() {
        if (!CustomCrafterAPI.hasFullCompatibility("5.0.20")) {
            logger.warning("CustomCrafterAPI のバージョンが互換性を持ちません。")
            server.pluginManager.disablePlugin(this)
            return
        }

        val designer = MyDesigner()
        CustomCrafterAPI.setCraftUIDesigner(designer)
    }

    override fun onDisable() {
        // 他のプラグインへの影響を避けるためデフォルトに戻す
        CustomCrafterAPI.setCraftUIDesignerDefault()
    }
}
```

---

## Baked クラス

`CraftUIDesigner.Baked` は `CraftUIDesigner` の各メソッドを特定のコンテキストで評価した結果をイミュータブルに保持するクラスです。

```kotlin
val context = CraftUIDesigner.Context(player = null)
val baked: CraftUIDesigner.Baked = CraftUIDesigner.bake(myDesigner, context)
```

`Baked` が持つ主なメソッドは以下のとおりです：

| メソッド | 概要 |
|----------|------|
| `apply(ui: Inventory)` | インベントリにボタン・空白スロットのアイテムを配置する |
| `craftSlots()` | クラフト可能な 36 スロットの座標リストを返す |
| `isValid()` | クラフトスロットが 6×6 の正方形かどうか検証する |

:::note
`setCraftUIDesigner` を呼び出すと、内部的に `CraftUIDesigner.bake(designer, Context(null))` が実行され `isValid()` のチェックが行われます。
クラフトスロットが 36 個の 6×6 正方形を成さない場合は例外がスローされ、デザイナーの設定は失敗します。
:::
