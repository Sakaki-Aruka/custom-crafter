---
title: CVanillaRecipe
---

## CVanillaRecipe とは

`CVanillaRecipe` はバニラ Bukkit の `Recipe` インスタンスを `CRecipe` としてラップするクラスです。
バニラのクラフトレシピを `CRecipe` として扱えるようにすることで、`PartialSearch` などの CustomCrafter 機能と組み合わせられるようになります。

コンストラクタは `internal` のため直接呼び出すことはできません。以下のファクトリメソッドを使用してください。

---

## ファクトリメソッド

### fromVanilla()

バニラの `CraftingRecipe` を `CVanillaRecipe` に変換します。
実行時の型に応じて `fromShaped()`、`fromShapeless()`、`fromTransmute()` のいずれかに処理を委譲します。
対応していないレシピ型の場合は `null` を返します。

```kotlin
@JvmStatic
fun fromVanilla(recipe: CraftingRecipe): CVanillaRecipe?
```

---

### fromShaped()

バニラの `ShapedRecipe` を変換します。

```kotlin
@JvmStatic
fun fromShaped(recipe: ShapedRecipe): CVanillaRecipe
```

レシピのシェイプと素材マップを `Map<CoordinateComponent, CMatter>` へ変換します。
各 `CMatter` は `RecipeChoice.test()` を使用した `CMatterPredicate` を持つため、NBT や完全一致の素材条件も正しく評価されます。

---

### fromShapeless()

バニラの `ShapelessRecipe` を変換します。

```kotlin
@JvmStatic
fun fromShapeless(recipe: ShapelessRecipe): CVanillaRecipe
```

素材は `CoordinateComponent.fromIndex()` で順番に座標へ割り当てられます。
各 `CMatter` は `RecipeChoice.test()` を使用した `CMatterPredicate` を持ちます。

---

### fromTransmute()

バニラの `TransmuteRecipe` を変換します。**5.0.21** で追加されました。

```kotlin
@JvmStatic
fun fromTransmute(recipe: TransmuteRecipe): CVanillaRecipe
```

`TransmuteRecipe` は、ソースアイテム (`input`) とカタリスト (`material`) を組み合わせることでソースのアイテムタイプが `result` に変化するクラフトレシピです。
変換後の `CVanillaRecipe` は `SHAPELESS` 型で、ソース用とカタリスト用の 2 スロットを持ちます。

`ResultSupplier` はクラフトグリッド内から `recipe.input` にマッチするアイテムを探し、そのタイプを `recipe.result.type` に置き換えたコピーを返します。

```kotlin
// 例: バニラの TransmuteRecipe をすべてラップして登録する
Bukkit.recipeIterator().asSequence()
    .filterIsInstance<TransmuteRecipe>()
    .mapNotNull { CVanillaRecipe.fromTransmute(it) }
    .forEach { CustomCrafterAPI.registerVanillaRecipe(it) }
```

---

## relateWith()

このレシピのスロットを指定した `CraftView` とインデックス順に対応付けた `MappedRelation` を構築します。

```kotlin
fun relateWith(view: CraftView): MappedRelation
```

| 条件 | 挙動 |
|------|------|
| `view` の非 Air アイテム数がレシピのスロット数と一致しない | `IllegalArgumentException` をスロー |
| ビューが 4 列または 4 行を超える | `IllegalArgumentException` をスロー |

---

## original

元の Bukkit `Recipe` インスタンスは `original` プロパティからアクセスできます。

```kotlin
val original: Recipe
```

---

## CVanillaRecipe + PartialSearch

`CVanillaRecipe` は `CRecipe` を実装しているため、`PartialSearch` と組み合わせることができます。
これにより、**Bukkit API では提供されていないバニラレシピの部分一致検索**が可能になります。

```kotlin
val vanillaRecipes: List<CVanillaRecipe> = Bukkit.recipeIterator().asSequence()
    .filterIsInstance<CraftingRecipe>()
    .mapNotNull { CVanillaRecipe.fromVanilla(it) }
    .toList()

// バニラレシピに対して非同期で部分一致検索
// asyncPartialSearch は CompletableFuture を返すため、thenAccept でコールバックを登録する
// (非同期スレッド上で .get() を使って値を直接取得することもできる)
PartialSearch.asyncPartialSearch(
    crafterId = player.uniqueId,
    view = currentView,
    sourceRecipes = vanillaRecipes
).thenAccept { results: List<PartialSearch.PartialSearchResult> ->
    results.forEach { result ->
        println("部分一致: ${result.recipe.name}, 状態: ${result.state()}")
    }
}
```

:::note
起動時に読み込まれたバニラレシピは `Bukkit.recipeIterator()` で列挙できます。
`CVanillaRecipe` への変換はいちど行ってリストをキャッシュすることを推奨します。
:::