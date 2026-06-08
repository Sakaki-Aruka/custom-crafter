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
| `MAJOR_VERSION` | `Int` | メジャーバージョン番号。互換性のない API 変更で増加する |
| `MINOR_VERSION` | `Int` | マイナーバージョン番号。後方互換のある機能追加で増加する |
| `PATCH_VERSION` | `Int` | パッチバージョン番号。後方互換のあるバグ修正で増加する |
| `API_VERSION` | `String` | `"MAJOR.MINOR.PATCH"` 形式のバージョン文字列 |
| `VERSION_TYPE` | `VersionType` | リリース段階: `ALPHA`・`BETA`・`RC`・`STABLE` のいずれか |
| `AUTHORS` | `Set<String>` | CustomCrafterAPI の作者名一覧 |

`VersionType` は `ALPHA`・`BETA`・`RC`・`STABLE` の 4 つの値を持つ enum です。現在のリリース段階は `VERSION_TYPE` で確認できます。

```kotlin
if (CustomCrafterAPI.VERSION_TYPE != CustomCrafterAPI.VersionType.STABLE) {
    logger.warning("CustomCrafter API は安定版ではありません: ${CustomCrafterAPI.VERSION_TYPE}")
}
```

### バージョン互換性チェック

`hasFullCompatibility(version: String)` は 5.2.0 で非推奨となり、6.0.0 で削除される予定です。
代わりに `MAJOR_VERSION`・`MINOR_VERSION`・`PATCH_VERSION`・`VERSION_TYPE` を使用してバージョンを確認してください。

```kotlin
override fun onEnable() {
    if (CustomCrafterAPI.MAJOR_VERSION < 5 || CustomCrafterAPI.MINOR_VERSION < 2) {
        logger.warning("このプラグインは CustomCrafterAPI 5.2.0 以降を必要とします。")
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

`getRegisteredRecipeNames()` は登録済みのすべてのレシピ名を `Set<String>` として返します。

```kotlin
val names: Set<String> = CustomCrafterAPI.getRegisteredRecipeNames()
```

`getRegisteredRecipeFromName(filter)` は名前が条件を満たすレシピを返します。
`getRegisteredRecipeFromName(recipeName)` は名前が完全一致するレシピを返します。

```kotlin
// 名前が "custom-" で始まるレシピ
val filtered: List<CRecipe> = CustomCrafterAPI.getRegisteredRecipeFromName { it.startsWith("custom-") }

// 名前が "stone-to-coal" と完全一致するレシピ
val exact: List<CRecipe> = CustomCrafterAPI.getRegisteredRecipeFromName("stone-to-coal")
```

`getRegisteredRecipeFromPlugin(plugin)` は指定したプラグインインスタンスが登録したすべてのレシピを返します。

```kotlin
val mine: List<CRecipe> = CustomCrafterAPI.getRegisteredRecipeFromPlugin(this)
```

### レシピの登録

`registerRecipe(recipes: List<CRecipe>, plugin: JavaPlugin)` でレシピを登録します。

- 登録時に `CRecipe.isValidRecipe()` によるバリデーションが実行されます。失敗した場合は `IllegalStateException` がスローされます。
- 現在の `RecipeNameStrictLevel` が重複した名前を検出した場合は `IllegalArgumentException` がスローされます。
- 登録に成功すると `RegisterCustomRecipeEvent` が発火します。

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

### レシピの解除

`unregisterRecipe(name: String?, plugin: JavaPlugin?)` で条件に一致するレシピを解除します。
`unregisterAllRecipes()` ですべてのレシピをまとめて解除することもできます。
どちらも `UnregisterCustomRecipeEvent` を発火します。

| 引数       | デフォルト                         | `null` 時の動作                                    |
|----------|-------------------------------|------------------------------------------------|
| `name`   | —                             | 名前を絞り込み条件に使用しない                                |
| `plugin` | `CustomCrafter.getInstance()` | プラグインを絞り込み条件に使用しない。両方が `null` の場合はすべてのレシピを解除する |

```kotlin
// 名前で解除 (デフォルトプラグインで登録されたものが対象)
CustomCrafterAPI.unregisterRecipe(name = "stone-to-coal")

// 特定プラグインが登録したすべてのレシピを解除
CustomCrafterAPI.unregisterRecipe(plugin = myPlugin)

// プラグインを問わずすべてのレシピを解除
CustomCrafterAPI.unregisterRecipe(name = null, plugin = null)

// すべてのレシピを解除
CustomCrafterAPI.unregisterAllRecipes()
```

---

## 設定プロパティ

各設定プロパティには対応する `DEFAULT_XXX` 定数が存在します。
デフォルト値に戻すには、定数を `set` メソッドに渡してください。

```kotlin
CustomCrafterAPI.setBaseBlock(CustomCrafterAPI.DEFAULT_BASE_BLOCK)
```

設定変更時には `CustomCrafterAPIPropertiesChangeEvent` が発火します。
`setXxx(value, calledAsync = false)` の第 2 引数は非同期スレッドから呼び出す際に `true` を指定します。

### BaseBlock

作業台の真下に置かれることで専用 UI を開くトリガーとなるブロックの種類です。
デフォルト: `Material.GOLD_BLOCK` (`DEFAULT_BASE_BLOCK`)。
ブロック以外の `Material` を指定した場合は `IllegalArgumentException` がスローされます。

```kotlin
val current: Material = CustomCrafterAPI.getBaseBlock()

CustomCrafterAPI.setBaseBlock(Material.DIAMOND_BLOCK)

// デフォルトに戻す
CustomCrafterAPI.setBaseBlock(CustomCrafterAPI.DEFAULT_BASE_BLOCK)
```

### BaseBlockSide

作業台の真下に必要なブロックの一辺のサイズです。
デフォルト: `3` (3×3) (`DEFAULT_BASE_BLOCK_SIDE`)。3 以上の奇数のみ有効で、それ以外を指定すると `false` が返されます。

```kotlin
val success: Boolean = CustomCrafterAPI.setBaseBlockSideSize(5)

// デフォルト (3) に戻す
CustomCrafterAPI.setBaseBlockSideSize(CustomCrafterAPI.DEFAULT_BASE_BLOCK_SIDE)
```

### ResultGiveCancel

`true` に設定すると、CustomCrafterAPI の UI でアイテムが作成された際に、アイテムをプレイヤーへ渡す処理を CustomCrafterAPI が行わず、呼び出し元のプラグインに委ねます。
独自の成果物付与ロジックを実装する場合に使用します。
デフォルト: `false` (`DEFAULT_RESULT_GIVE_CANCEL`)。

```kotlin
CustomCrafterAPI.setResultGiveCancel(true)

// デフォルトに戻す
CustomCrafterAPI.setResultGiveCancel(CustomCrafterAPI.DEFAULT_RESULT_GIVE_CANCEL)
```

### UseCustomCraftUI

`true` (デフォルト) の場合、プレイヤーが作業台をクリックすると CustomCrafterAPI の専用 UI が開きます。
`false` にすると、作業台のクリックで専用 UI が開かなくなります。
デフォルト: `true` (`DEFAULT_USE_CUSTOM_CRAFT_UI`)。

```kotlin
CustomCrafterAPI.setUseCustomCraftUI(false)

// デフォルトに戻す
CustomCrafterAPI.setUseCustomCraftUI(CustomCrafterAPI.DEFAULT_USE_CUSTOM_CRAFT_UI)
```

### UseMultipleResultCandidateFeature

入力されたアイテムに複数のレシピが合致した場合に、すべての候補をプレイヤーへ表示するモードを有効化します。
`false` (デフォルト) の場合は最初に見つかったレシピのみが採用されます。
デフォルト: `false` (`DEFAULT_USE_MULTIPLE_RESULT_CANDIDATE_FEATURE`)。

```kotlin
CustomCrafterAPI.setUseMultipleResultCandidateFeature(true)

// デフォルトに戻す
CustomCrafterAPI.setUseMultipleResultCandidateFeature(CustomCrafterAPI.DEFAULT_USE_MULTIPLE_RESULT_CANDIDATE_FEATURE)
```

### RecipeNameStrictLevel

レシピ登録時の名前重複チェックの厳格さを制御します。
レベルは厳しい方向へのみ変更できます。緩和は不可です。
デフォルト: `NameStrictLevel.NOTHING` (`DEFAULT_RECIPE_NAME_STRICT_LEVEL`)。

| レベル | 動作 |
|--------|------|
| `NOTHING` | 重複チェックなし |
| `WEAK` | 名前が既存のレシピ名と完全一致する場合に拒否する |
| `STRICT` | 名前からスペースを除去した結果が既存のレシピ名と一致する場合に拒否する |

```kotlin
CustomCrafterAPI.setRecipeNameStrictLevel(CustomCrafterAPI.NameStrictLevel.WEAK)

val current: CustomCrafterAPI.NameStrictLevel = CustomCrafterAPI.getRecipeNameStrictLevel()
```

### CraftUIDesigner

クラフト UI の外観をカスタマイズするデザイナーを設定します。
詳細は [CraftUIDesigner のページ](/ja/extra/craftui-designer/) を参照してください。
デフォルト: `CraftUIDesigner.DEFAULT` (`DEFAULT_CRAFT_UI_DESIGNER`)。

```kotlin
val myDesigner = object : CraftUIDesigner {
    override fun title(context: CraftUIDesigner.Context) = Component.text("My Crafter")
    // ... その他のメソッドを実装
}
CustomCrafterAPI.setCraftUIDesigner(myDesigner)

// デフォルトに戻す
CustomCrafterAPI.setCraftUIDesigner(CustomCrafterAPI.DEFAULT_CRAFT_UI_DESIGNER)
```

### AllCandidateUIDesigner

AllCandidateUI の外観をカスタマイズするデザイナーを設定します。
詳細は [AllCandidateUIDesigner のページ](/ja/extra/allcandidateui-designer/) を参照してください。
デフォルト: `AllCandidateUIDesigner.DEFAULT` (`DEFAULT_ALL_CANDIDATE_UI_DESIGNER`)。

```kotlin
val myDesigner = object : AllCandidateUIDesigner {
    override fun title(context: AllCandidateUIDesigner.Context) = Component.text("全候補")
    // ... 必要なメソッドをオーバーライド
}
CustomCrafterAPI.setAllCandidateUIDesigner(myDesigner)

// デフォルトに戻す
CustomCrafterAPI.setAllCandidateUIDesigner(CustomCrafterAPI.DEFAULT_ALL_CANDIDATE_UI_DESIGNER)
```
