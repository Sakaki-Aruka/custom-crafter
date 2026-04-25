---
title: Search API
---

## Search とは

`Search` は CustomCrafterAPI でレシピ検索を行うためのエントリポイントとなるオブジェクトです。
入力されたアイテムの配置と登録済みレシピを照合し、合致するレシピとその座標対応情報を返します。

---

## CraftView

`CraftView` はレシピ検索の引数となるクラスで、クラフト UI 上のアイテム配置を表します。

```kotlin
data class CraftView(
    val materials: Map<CoordinateComponent, ItemStack>, // 入力されたアイテムの配置
    val result: ItemStack                               // 成果物スロットのアイテム
)
```

`materials` のサイズは 1 以上 36 以下である必要があります。それ以外の場合は `Search.search` / `Search.asyncSearch` で `IllegalArgumentException` がスローされます。

```kotlin
// 石を (0,0) に配置した CraftView を作成する例
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)
```

---

## Search.search (同期検索)

```kotlin
fun search(
    crafterID: UUID,
    view: CraftView,
    forceSearchVanillaRecipe: Boolean = true,
    onlyFirst: Boolean = false,
    sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
): SearchResult
```

メインスレッドで動作する同期的な検索メソッドです。
登録済みレシピが多い場合や、predicates が重い処理を含む場合はサーバーの TPS に影響する可能性があります。
そのような場合は後述の `asyncSearch` の使用を推奨します。

| 引数 | 概要 |
|------|------|
| `crafterID` | クラフトを実行するプレイヤーの UUID |
| `view` | 入力されたアイテムの配置 |
| `forceSearchVanillaRecipe` | `true` の場合は常にバニラレシピを検索する。`false` かつカスタムレシピが見つかった場合はバニラを検索しない |
| `onlyFirst` | `true` にすると最初に合致したカスタムレシピのみ返す |
| `sourceRecipes` | 検索対象のレシピリスト (デフォルトは登録済み全レシピ) |

```kotlin
val player: Player = /* ... */
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)

val result: Search.SearchResult = Search.search(player.uniqueId, view)
```

---

## Search.asyncSearch (非同期検索)

```kotlin
fun asyncSearch(
    crafterID: UUID,
    view: CraftView,
    query: SearchQuery = SearchQuery.ASYNC_DEFAULT,
    sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
): CompletableFuture<SearchResult>
```

仮想スレッドを用いた非同期検索メソッドです (5.0.17 以降)。
各レシピの検索は個別のスレッドで並列実行されるため、データベースや外部 API を呼び出すような重い predicates を持つレシピが多数存在する場合に特に効果的です。
CustomCrafterAPI の標準クラフト画面および全候補表示機能では、このメソッドが内部的に使用されています。

:::caution
非同期スレッドでは BukkitAPI のワールドやエンティティへのアクセスができません。
`ResultSupplier` や `CMatterPredicate` の中でプレイヤー情報などにアクセスする場合は、`asyncContext.isAsync()` で非同期かどうかを確認してください。
:::

```kotlin
val player: Player = /* ... */
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)

val future: CompletableFuture<Search.SearchResult> = Search.asyncSearch(player.uniqueId, view)
future.thenAccept { result ->
    // 非同期で結果を処理
    val customs = result.customs()
    println("合致したカスタムレシピ数: ${customs.size}")
}
```

---

## SearchQuery

`SearchQuery` は `asyncSearch` の検索動作を制御するクラスです。

```kotlin
class SearchQuery(
    val searchMode: SearchMode,
    val vanillaSearchMode: VanillaSearchMode,
    val asyncContext: AsyncContext? = null
)
```

### SearchMode

| 値 | 概要 |
|----|------|
| `ALL` (デフォルト) | 合致するすべてのカスタムレシピを返す |
| `ONLY_FIRST` | 最初に合致したカスタムレシピのみ返す。見つかり次第他の検索タスクをキャンセルする |

### VanillaSearchMode

| 値 | 概要 |
|----|------|
| `IF_CUSTOMS_NOT_FOUND` (デフォルト) | カスタムレシピが見つからなかった場合のみバニラを検索する |
| `FORCE` | カスタムレシピの有無にかかわらず常にバニラを検索する |

```kotlin
// ONLY_FIRST モードで検索する
val query = Search.SearchQuery(
    searchMode = Search.SearchQuery.SearchMode.ONLY_FIRST,
    vanillaSearchMode = Search.SearchQuery.VanillaSearchMode.IF_CUSTOMS_NOT_FOUND,
    asyncContext = AsyncContext.ofTurnOff()
)
val future = Search.asyncSearch(player.uniqueId, view, query)
```

---

## SearchResult

`SearchResult` は検索結果を保持するクラスです。

| メソッド | 返り値 | 概要 |
|----------|--------|------|
| `vanilla()` | `Recipe?` | バニラレシピ。見つからなかった場合または検索しなかった場合は `null` |
| `customs()` | `List<Pair<CRecipe, MappedRelation>>` | 合致したカスタムレシピとその座標対応のリスト |
| `size()` | `Int` | バニラとカスタムの合計合致数 |
| `getMergedResults()` | `List<Pair<CRecipe, MappedRelation?>>` | バニラ・カスタムをまとめたリスト。バニラは `MappedRelation` が `null` になる |

```kotlin
val result: Search.SearchResult = Search.search(player.uniqueId, view)

// バニラレシピの取得
val vanilla: Recipe? = result.vanilla()
vanilla?.let { println("バニラレシピ: ${it.result.type}") }

// カスタムレシピの取得
val customs: List<Pair<CRecipe, MappedRelation>> = result.customs()
customs.forEach { (recipe, relation) ->
    println("カスタムレシピ: ${recipe.name}")
}

// 全候補をまとめて取得
result.getMergedResults().forEach { (recipe, relation) ->
    println("レシピ: ${recipe.name}, 座標対応あり: ${relation != null}")
}
```

---

## VanillaSearch

`VanillaSearch` は CustomCrafterAPI の検索フローを介さず、バニラのレシピのみを検索するためのオブジェクトです。

```kotlin
// 丸石から石炉を作るバニラレシピを検索する例
val view = CraftView(
    materials = CoordinateComponent.squareFill(3)
        .filter { it.x < 3 && it.y < 3 }
        .associate { it to ItemStack.of(Material.COBBLESTONE) },
    result = ItemStack.empty()
)
val vanillaRecipe: Recipe? = VanillaSearch.search(Bukkit.getWorlds().first(), view)
vanillaRecipe?.let { println("結果アイテム: ${it.result.type}") }
```

---

## MappedRelation と MappedRelationComponent

`MappedRelation` はレシピ上の座標と実際の入力スロット座標の対応関係を保持するクラスです。
`MappedRelationComponent` は 1 つの対応ペア (レシピ座標 → 入力座標) を表します。

```kotlin
data class MappedRelation(
    val components: Set<MappedRelationComponent>
)

data class MappedRelationComponent(
    val recipe: CoordinateComponent, // レシピ上の座標
    val input: CoordinateComponent   // 実際の入力スロットの座標
)
```

たとえば定形レシピで `(0,0)` に石を要求しているとき、プレイヤーが `(2,2)` にアイテムを配置してもレシピに合致する場合があります。
その際の `MappedRelationComponent` は `recipe = (0,0), input = (2,2)` となります。

---

## PartialSearch

`PartialSearch` は非同期の部分一致レシピ検索を提供します。
部分一致とは、プレイヤーの現在のクラフトグリッドがレシピの要求スロットの一部（必ずしも全部ではない）を満たしている状態を指します。
クラフトヒントやレシピガイド、オートコンプリート提案などに活用できます。

`UnPartialSearchableRecipe` を実装しているレシピは部分一致検索から除外されます。

```kotlin
fun asyncPartialSearch(
    crafterId: UUID,
    view: CraftView,
    searchQuery: SearchQuery = SearchQuery.ASYNC_DEFAULT,
    sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
): CompletableFuture<List<PartialSearchResult>>
```

### PartialSearchResult

返されるリストの各要素は `PartialSearchResult` を実装します。

| メソッド | 戻り値型 | 概要 |
|--------|---------|------|
| `recipe` | `CRecipe` | 評価された候補レシピ |
| `matched()` | `Set<CoordinateComponent>` | 少なくとも 1 つの入力アイテムに対応したレシピスロットの座標 |
| `notEnough()` | `Set<CoordinateComponent>` | 対応する入力アイテムがないレシピスロットの座標 |
| `state()` | `MatchState` | 全スロットが満たされていれば `ALL`、不足があれば `PARTIAL_NOT_ENOUGH` |

定形レシピの場合は `PartialShapedResult` が返され、`relation: MappedRelation` を持ちます。
不定形レシピの場合は `PartialShapelessResult` が返され、Matter をキーとした候補入力スロットのマッピング `weakRelations()` を持ちます。

### 実装例

```kotlin
val player: Player = /* ... */
val view = CraftView(
    materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
    result = ItemStack.empty()
)

PartialSearch.asyncPartialSearch(player.uniqueId, view).thenAccept { results ->
    results.forEach { result ->
        println("レシピ: ${result.recipe.name}, 状態: ${result.state()}")
        if (result.notEnough().isNotEmpty()) {
            println("  不足スロット: ${result.notEnough()}")
        }
    }
}
```
この情報は `ResultSupplier.Context.relation` や `CRecipePredicate.Context.relation` として渡され、どのスロットに何が配置されていたかを追跡するために使用します。
