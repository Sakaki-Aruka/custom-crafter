---
title: AllCandidateUIDesigner について
---

## AllCandidateUIDesigner とは

`AllCandidateUIDesigner` は、`UseMultipleResultCandidateFeature` が有効なときに合致するレシピの候補を一覧表示する AllCandidateUI の外観をカスタマイズするためのインターフェースです。
実装したデザイナーを `CustomCrafterAPI.setAllCandidateUIDesigner(designer)` で設定することで、タイトル・ボタン位置・レシピスロットレイアウト・プレースホルダーアイテムを変更できます。

カスタマイズできる要素は以下の 7 つです：

| メソッド | 概要 |
|----------|------|
| `title(context)` | AllCandidateUI のインベントリタイトル |
| `previousPageButton(context)` | 前のページに戻るボタンのスロット座標とアイコンアイテム |
| `nextPageButton(context)` | 次のページへ進むボタンのスロット座標とアイコンアイテム |
| `backToCraftUIButton(context)` | クラフト画面へ戻るボタンのスロット座標とアイコンアイテム |
| `recipeSlots(context)` | レシピアイコンを配置できるスロット座標の集合 |
| `noDisplayableItem(context)` | 表示可能なアイテムを提供できないレシピのスロットに表示されるアイコン |
| `ungeneratedIconPlaceholderItem(context)` | レシピアイコン生成中に使用するプレースホルダーアイコンを作成するラムダ |

すべてのメソッドにデフォルト実装が存在するため、カスタマイズが必要な部分だけをオーバーライドすれば足ります。
すべてのメソッドは `AllCandidateUIDesigner.Context` を受け取り、`context.searchResult` と `context.crafterId` を参照できます。

:::note
CustomCrafterAPI が管理するインベントリ (54 スロット) のスロット番号は `CoordinateComponent.toIndex()` と `CoordinateComponent.fromIndex(index)` で相互変換できます。
`recipeSlots` の集合は 1 以上 51 以下の要素数を持ち、ボタン座標と重複してはいけません。
たとえばレシピが 45 件あり `recipeSlots` が 30 スロットを返す場合、AllCandidateUI は 2 ページにわたって表示されます。
:::

---

## デフォルト実装

デフォルト設定のレイアウトは以下のとおりです：
- 前のページボタン: インデックス `45`
- 次のページボタン: インデックス `53`
- クラフト画面へ戻るボタン: インデックス `49` (作業台アイコン)
- レシピスロット: インデックス `0〜44` (5 行 × 9 列)

設定をリセットしたい場合は以下を呼び出します：

```kotlin
CustomCrafterAPI.setAllCandidateUIDesigner(CustomCrafterAPI.DEFAULT_ALL_CANDIDATE_UI_DESIGNER)
```

---

## カスタムデザイナーの実装例

### シンプルなカスタマイズ

```kotlin
val myDesigner = object : AllCandidateUIDesigner {
    override fun title(context: AllCandidateUIDesigner.Context): Component {
        return Component.text("全候補")
    }

    override fun recipeSlots(context: AllCandidateUIDesigner.Context): Set<CoordinateComponent> {
        // 上 3 行 (インデックス 0〜26) のみをレシピスロットとして使用する
        return (0..<27).map { CoordinateComponent.fromIndex(it) }.toSet()
    }

    override fun noDisplayableItem(context: AllCandidateUIDesigner.Context): ItemStack {
        return ItemStack.of(Material.BARRIER).apply {
            editMeta { meta -> meta.displayName(Component.text("表示不可")) }
        }
    }
}

CustomCrafterAPI.setAllCandidateUIDesigner(myDesigner)
```

### プレイヤーごとのカスタマイズ

`context.crafterId` と `context.searchResult` を使うことでプレイヤーや検索結果に応じたカスタマイズが可能です：

```kotlin
val myDesigner = object : AllCandidateUIDesigner {
    override fun title(context: AllCandidateUIDesigner.Context): Component {
        val playerName = Bukkit.getPlayer(context.crafterId)?.name ?: "Unknown"
        return Component.text("${playerName} の候補一覧 (${context.searchResult.size()} 件)")
    }
}

CustomCrafterAPI.setAllCandidateUIDesigner(myDesigner)
```

### プラグイン起動時に登録する

```kotlin
class MyPlugin : JavaPlugin() {
    override fun onEnable() {
        val designer = MyAllCandidateDesigner()
        CustomCrafterAPI.setAllCandidateUIDesigner(designer)
    }

    override fun onDisable() {
        // 他のプラグインへの影響を避けるためデフォルトに戻す
        CustomCrafterAPI.setAllCandidateUIDesigner(CustomCrafterAPI.DEFAULT_ALL_CANDIDATE_UI_DESIGNER)
    }
}
```

---

## Baked クラス

`AllCandidateUIDesigner.Baked` は `AllCandidateUIDesigner` の各メソッドを特定のコンテキストで評価した結果をイミュータブルに保持するクラスです。
`designer.bake(context)` または `designer.bakeWithEmptyContext()` を呼び出すことで取得できます。

```kotlin
val context = AllCandidateUIDesigner.Context.emptyContext()
val baked: AllCandidateUIDesigner.Baked = myDesigner.bake(context)
```

| メソッド / プロパティ | 型 | 概要 |
|----------------------|----|------|
| `isValid()` | `Result<Unit>` | bake された値を検証する。成功なら `Result.success`、失敗なら説明付きの `Result.failure` を返す |
| `ungeneratedIcon(recipe)` | `ItemStack` | `recipe` のプレースホルダーアイコンを返す。生成されたアイテムが表示不可の場合はデフォルトのプレースホルダーへフォールバックする |
| `recipeSlotsIndex` | `Set<Int>` | `recipeSlots` と同じスロット座標をインデックス (`Set<Int>`) で表したもの |

:::note
`setAllCandidateUIDesigner` を呼び出すと、内部的に `designer.bakeWithEmptyContext()` が実行され `isValid()` のチェックが行われます。
`recipeSlots` が空、51 件超、またはボタン座標と重複するなど、bake された値が不正な場合は例外がスローされデザイナーの設定は失敗します。
:::
