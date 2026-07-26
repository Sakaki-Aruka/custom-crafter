---
title: CRecipePredicate について
---

## CRecipePredicate とは

`CRecipePredicate` は `CRecipe` に対してアイテム配置単位ではなくレシピ全体を対象にした検査を挿入するための関数型インターフェースです。
`CMatterPredicate` が個々のスロットのアイテムに対する検査であるのに対し、`CRecipePredicate` は入力されたアイテムの配置全体・プレイヤー情報・レシピ情報などを一括して参照できます。

```kotlin
fun interface CRecipePredicate {
    fun test(ctx: Context): Boolean
    fun name(): String = ANONYMOUS // デフォルト実装 (5.3.0 以降)
}
```

`CRecipe.predicates` フィールドにリストとして保持され、リスト内のすべての predicate が `true` を返した場合にのみレシピが合致と判定されます。

---

## Context

`test` 関数に渡されるコンテキストは以下のフィールドを持ちます。

| フィールド | 型 | 概要 |
|------------|-----|------|
| `input` | `CraftView` | クラフト UI の入力状態 (アイテム配置・成果物スロット) |
| `crafterId` | `UUID` | クラフトを実行したプレイヤーの UUID |
| `recipe` | `CRecipe` | 検査対象のレシピ |
| `relation` | `MappedRelation` | レシピ座標と入力スロット座標の対応関係 (CMatter の検査通過後に生成される) |
| `asyncContext` | `AsyncContext?` | 非同期実行時のコンテキスト。同期実行時は `null` (5.0.20 以降) |
| `explainer` | `Explainer?` | 検索の診断レコーダ。検索時に渡された場合のみ非 `null` (5.3.0 以降) |

非同期スレッド上で実行されることがあるため、`asyncContext?.isInterrupted()` による割り込みチェックを推奨します。
また BukkitAPI のワールドやエンティティへのアクセスは非同期スレッドでは許可されていないため、`ctx.isAsync()` で実行コンテキストを確認することを推奨します。

---

## 実装例

### 特定プレイヤーのみ作成を許可する

```kotlin
val onlyAdminPredicate = CRecipePredicate { ctx ->
    val player = Bukkit.getPlayer(ctx.crafterId) ?: return@CRecipePredicate false
    player.isOp
}

val recipe = CRecipeImpl(
    name = "admin-only-recipe",
    items = mapOf(CoordinateComponent(0, 0) to CMatterImpl.of(Material.DIAMOND)),
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.NETHERITE_INGOT))),
    type = CRecipe.Type.SHAPED,
    predicates = listOf(onlyAdminPredicate)
)
```

### 外部ファイルやデータベースを参照する (非同期考慮)

```kotlin
val externalCheckPredicate = CRecipePredicate { ctx ->
    // 非同期スレッドからの呼び出し時は割り込みを確認する
    if (ctx.asyncContext?.isInterrupted() == true) {
        return@CRecipePredicate false
    }

    // 非同期スレッドでの BukkitAPI アクセスは不可
    // ここでは UUID を使ってデータベースを参照する例
    val hasPermission: Boolean = MyDatabase.hasPermission(ctx.crafterId, "special-recipe")
    hasPermission
}
```

### 合致したアイテムの総数を条件にする

`relation` を使ってレシピ座標と入力座標の対応を参照できます。

```kotlin
val multipleItemsPredicate = CRecipePredicate { ctx ->
    // 入力されたアイテムがすべて同一スタックサイズかどうかを確認する
    val inputItems = ctx.input.materials.values
    val firstAmount = inputItems.firstOrNull()?.amount ?: return@CRecipePredicate true
    inputItems.all { it.amount == firstAmount }
}
```

---

## Predicates ユーティリティ

`CRecipePredicates` および `CMatterPredicates` は、predicate を合成・命名するための拡張関数を提供するユーティリティオブジェクトです (5.3.0 以降)。

### コンビネータ

複数の predicate を論理演算で1つにまとめられます。

| 関数 | 概要 |
|------|------|
| `and(other)` | 両方が `true` の場合に `true` |
| `or(other)` | いずれかが `true` の場合に `true` |
| `allOf(vararg predicates)` | 自身を含むすべてが `true` の場合に `true` |
| `nOf(n, vararg predicates)` | 自身を含む中で `true` を返したものが `n` 個以上の場合に `true` |

```kotlin
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicates.and

val isOp = CRecipePredicate { ctx -> Bukkit.getPlayer(ctx.crafterId)?.isOp == true }
val isDaytime = CRecipePredicate { ctx -> /* ... */ true }

val combined = isOp.and(isDaytime)
```

### named — predicate に名前を付ける

`named` は predicate に表示用の名前を与えます。

```kotlin
val onlyAdmin = CRecipePredicates.named("onlyAdmin") { ctx ->
    Bukkit.getPlayer(ctx.crafterId)?.isOp == true
}
```

Kotlin のラムダ式は識別子を持たないため、デバッグ時にどの predicate が検査を弾いたかを名前で特定できません。
`named` で包んでおくと、`Explainer` によるデバッグ時に添字ではなく名前で表示されます。

コンビネータで合成した場合、名前も `(onlyAdmin and isDaytime)` のように自動で合成されます。

```kotlin
val combined = CRecipePredicates.named("onlyAdmin") { /* ... */ }
    .and(CRecipePredicates.named("isDaytime") { /* ... */ })
```

`name()` は `CRecipePredicate` / `CMatterPredicate` のデフォルトメソッドとして定義されているため、直接実装することもできます。

```kotlin
val predicate = object : CRecipePredicate {
    override fun test(ctx: CRecipePredicate.Context): Boolean = /* ... */ true
    override fun name(): String = "onlyAdmin"
}
```

:::tip
名前を付けた predicate の実際の表示例や、検索が失敗した原因の追跡方法については [レシピ検索の高度なデバッグ](/ja/extra/explainer/) を参照してください。
:::

---

## CRecipePredicate と CMatterPredicate の使い分け

| | CMatterPredicate | CRecipePredicate |
|--|-----------------|-----------------|
| 検査単位 | 個別スロットのアイテム 1 つ | レシピ全体・全入力アイテム |
| 受け取れる情報 | 配置されたアイテム・座標・レシピ | CraftView 全体・プレイヤー UUID・レシピ・MappedRelation |
| 用途 | アイテムの種類・エンチャント・ポーションなどの個別検査 | プレイヤー権限・入力配置全体を横断するロジック・外部データ参照 |

:::note
`CRecipePredicate` は `CMatterPredicate` の全検査が通過した後に実行されます。
そのため `CRecipePredicate.Context.relation` は、CMatter の検査を通過したアイテム配置に基づいて生成された確定済みの座標対応を持っています。
:::
