---
title: CRecipePredicate について
---

## CRecipePredicate とは

`CRecipePredicate` は `CRecipe` に対してアイテム配置単位ではなくレシピ全体を対象にした検査を挿入するための関数型インターフェースです。
`CMatterPredicate` が個々のスロットのアイテムに対する検査であるのに対し、`CRecipePredicate` は入力されたアイテムの配置全体・プレイヤー情報・レシピ情報などを一括して参照できます。

```kotlin
fun interface CRecipePredicate {
    fun test(ctx: Context): Boolean
}
```

`CRecipe.predicates` フィールドにリストとして保持され、リスト内のすべての predicate が `true` を返した場合にのみレシピが合致と判定されます。

---

## Context

`test` 関数に渡されるコンテキストは以下のフィールドを持ちます。

| フィールド | 型 | 概要 |
|------------|-----|------|
| `input` | `CraftView` | クラフト UI の入力状態 (アイテム配置・成果物スロット) |
| `crafterID` | `UUID` | クラフトを実行したプレイヤーの UUID |
| `recipe` | `CRecipe` | 検査対象のレシピ |
| `relation` | `MappedRelation` | レシピ座標と入力スロット座標の対応関係 (CMatter の検査通過後に生成される) |
| `asyncContext` | `AsyncContext?` | 非同期実行時のコンテキスト。同期実行時は `null` (5.0.20 以降) |

非同期スレッド上で実行されることがあるため、`asyncContext?.isInterrupted()` による割り込みチェックを推奨します。
また BukkitAPI のワールドやエンティティへのアクセスは非同期スレッドでは許可されていないため、`ctx.isAsync()` で実行コンテキストを確認することを推奨します。

---

## 実装例

### 特定プレイヤーのみ作成を許可する

```kotlin
val onlyAdminPredicate = CRecipePredicate { ctx ->
    val player = Bukkit.getPlayer(ctx.crafterID) ?: return@CRecipePredicate false
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
    val hasPermission: Boolean = MyDatabase.hasPermission(ctx.crafterID, "special-recipe")
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
