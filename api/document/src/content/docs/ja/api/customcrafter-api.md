---
title: CustomCrafterAPI オブジェクト
---

## CustomCrafterAPI とは

`CustomCrafterAPI` は CustomCrafterAPI の設定値の取得・変更・リセットと、レシピの登録・解除を一元管理する Kotlin の `object` です。
プラグインの `onEnable` での初期設定や、レシピを動的に追加・削除する際に利用します。

---

## 定数

| 定数 | 型 | 概要 |
|------|----|------|
| `API_VERSION` | `String` | API のバージョン文字列 (パッチバージョンを含まない) |
| `IS_STABLE` | `Boolean` | 安定版かどうか |
| `IS_BETA` | `Boolean` | ベータ版かどうか |
| `AUTHORS` | `Set<String>` | CustomCrafterAPI の作者名一覧 |

### バージョン互換性チェック

`hasFullCompatibility(version: String)` を使うことで、プラグインが依存しているバージョンと実行時にロードされている CustomCrafterAPI に完全な互換性があるかを確認できます。
互換性がない場合はプラグインを無効化するなど、適切な対処を行うことが推奨されます。

```kotlin
override fun onEnable() {
    if (!CustomCrafterAPI.hasFullCompatibility("5.0.20")) {
        logger.warning("このプラグインは現在ロードされている CustomCrafterAPI と互換性がありません。")
        logger.warning("ロード済みバージョン: ${CustomCrafterAPI.API_VERSION}")
        server.pluginManager.disablePlugin(this)
        return
    }
}
```

---

## レシピ管理

### レシピの取得

`getRecipes()` は登録済みのすべてのレシピをイミュータブルなリストとして返します。
返されたリストを変更してもレシピの登録・解除には影響しません。

```kotlin
val recipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
recipes.forEach { recipe -> println(recipe.name) }
```

### レシピの登録

`registerRecipe(vararg recipes: CRecipe)` でレシピを登録します。
登録時に `CRecipe.isValidRecipe()` によるバリデーションが実行されます。バリデーションに失敗した場合は `IllegalStateException` がスローされます。
また、登録は `RegisterCustomRecipeEvent` を発火させ、そのイベントがキャンセルされた場合は登録に失敗します。

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
    // RegisterCustomRecipeEvent がキャンセルされた場合
    println("レシピの登録がキャンセルされました。")
}
```

### レシピの解除

`unregisterRecipe(vararg recipes: CRecipe)` でレシピを解除します。
`unregisterAllRecipes()` ですべてのレシピをまとめて解除することもできます。
どちらも `UnregisterCustomRecipeEvent` を発火させ、キャンセルされた場合は解除に失敗します。

```kotlin
// 特定のレシピを解除
CustomCrafterAPI.unregisterRecipe(recipe)

// すべてのレシピを解除
val success: Boolean = CustomCrafterAPI.unregisterAllRecipes()
```

---

## 設定プロパティ

各設定プロパティは `get` / `set` / `setDefault` の 3 種類のメソッドで管理されています。
設定変更時には `CustomCrafterAPIPropertiesChangeEvent` が発火します。
`setXxx(value, calledAsync = false)` の第 2 引数は非同期スレッドから呼び出す際に `true` を指定します。

### BaseBlock

作業台の真下に置かれることで専用 UI を開くトリガーとなるブロックの種類です。
デフォルトは `Material.GOLD_BLOCK` です。
ブロック以外の `Material` を指定した場合は `IllegalArgumentException` がスローされます。

```kotlin
// 現在の設定を取得
val current: Material = CustomCrafterAPI.getBaseBlock()

// ダイヤモンドブロックに変更
CustomCrafterAPI.setBaseBlock(Material.DIAMOND_BLOCK)

// デフォルト (金ブロック) に戻す
CustomCrafterAPI.setBaseBlockDefault()
```

### BaseBlockSide

作業台の真下に必要なブロックの一辺のサイズです。
デフォルトは `3` (3×3)。3 以上の奇数のみ有効で、それ以外を指定すると `false` が返されます。

```kotlin
// 5×5 のブロックが必要になるように変更
val success: Boolean = CustomCrafterAPI.setBaseBlockSideSize(5)

// デフォルト (3) に戻す
CustomCrafterAPI.setBaseBlockSideSizeDefault()
```

### ResultGiveCancel

`true` に設定すると、CustomCrafterAPI の UI でアイテムが作成された際に、アイテムをプレイヤーへ渡す処理を CustomCrafterAPI が行わず、呼び出し元のプラグインに委ねます。
独自の成果物付与ロジックを実装する場合に使用します。デフォルトは `false` です。

```kotlin
// 成果物付与を自前で処理したい場合
CustomCrafterAPI.setResultGiveCancel(true)

// デフォルト (false) に戻す
CustomCrafterAPI.setResultGiveCancelDefault()
```

### UseCustomCraftUI

`true` (デフォルト) の場合、プレイヤーが作業台をクリックすると CustomCrafterAPI の専用 UI が開きます。
`false` にすると、作業台のクリックで専用 UI が開かなくなります。

```kotlin
CustomCrafterAPI.setUseCustomCraftUI(false) // 専用 UI を無効化
CustomCrafterAPI.setUseCustomCraftUIDefault() // デフォルト (true) に戻す
```

### UseMultipleResultCandidateFeature

入力されたアイテムに複数のレシピが合致した場合に、すべての候補をプレイヤーへ表示するモードを有効化します。
`false` (デフォルト) の場合は最初に見つかったレシピのみが採用されます。

```kotlin
CustomCrafterAPI.setUseMultipleResultCandidateFeature(true)
CustomCrafterAPI.setUseMultipleResultCandidateFeatureDefault()
```

### CraftUIDesigner

クラフト UI の外観をカスタマイズするデザイナーを設定します。
詳細は [CraftUIDesigner のページ](/ja/extra/craftui-designer/) を参照してください。

```kotlin
val myDesigner = object : CraftUIDesigner {
    override fun title(context: CraftUIDesigner.Context) = Component.text("My Crafter")
    // ... その他のメソッドを実装
}
CustomCrafterAPI.setCraftUIDesigner(myDesigner)
CustomCrafterAPI.setCraftUIDesignerDefault() // デフォルトに戻す
```

### AllCandidateNotDisplayableItem

一括候補表示モードで、候補として表示できないレシピのスロットに代わりに表示されるアイテムを設定します。
`loreSupplier` にはレシピ名を受け取ってロアを返すラムダを渡します。

```kotlin
val icon = ItemStack.of(Material.BARRIER)
CustomCrafterAPI.setAllCandidateNotDisplayableItem(icon) { recipeName ->
    listOf(Component.text("表示不可: $recipeName"))
}
```
