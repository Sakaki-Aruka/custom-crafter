---
title: AsyncContext
---

## AsyncContext とは

`AsyncContext` は非同期実行時の状態を管理するクラスです。
`ResultSupplier`、`CMatterPredicate`、`CRecipePredicate` などの処理が仮想スレッド上で実行される場合に、コンテキスト経由で渡されます。

CustomCrafterAPI は Java 21 の仮想スレッドを利用して非同期処理を行います。
仮想スレッドは協調的に動作するため外部から強制終了できません。
そのため処理の途中で定期的に `isInterrupted()` を確認し、割り込み要請があれば自発的に処理を中断する必要があります。

---

## クラス定義

```kotlin
class AsyncContext(interrupted: Boolean) {
    companion object {
        @JvmStatic
        fun ofTurnOff(): AsyncContext
    }

    fun isInterrupted(): Boolean
}
```

| メソッド | 概要 |
|----------|------|
| `ofTurnOff()` | 割り込みなし状態の `AsyncContext` を生成する (ファクトリメソッド) |
| `isInterrupted()` | 割り込み要請があるかどうかを返す |

`interrupt()` は `internal` であり、外部から直接呼び出すことはできません。
割り込みは CustomCrafterAPI の内部処理によって行われます。

---

## 非同期実行の仕組み

`ResultSupplier` や各種 `Predicate` の処理は、アイテムを作成する部分をプラグイン側でハンドリングしない限り仮想スレッド上で実行されます。
非同期実行時はコンテキストの `asyncContext` フィールドに `AsyncContext` インスタンスが渡されます。同期実行時は `null` です。

| 状況 | `asyncContext` |
|------|---------------|
| 同期実行 | `null` |
| 非同期実行 | `AsyncContext` インスタンス |

---

## 使用例

### ResultSupplier での割り込みチェック

```kotlin
val supplier = ResultSupplier { ctx ->
    val asyncCtx = ctx.asyncContext
    if (asyncCtx != null) {
        // 重い処理の前に割り込みチェック
        if (asyncCtx.isInterrupted()) {
            return@ResultSupplier emptyList()
        }

        val data = MyDatabase.fetchData(ctx.crafterID)

        // 複数ステップある場合は処理の間にも確認する
        if (asyncCtx.isInterrupted()) {
            return@ResultSupplier emptyList()
        }

        return@ResultSupplier listOf(ItemStack.of(data.material))
    }
    // 同期実行時の処理
    listOf(ItemStack.of(Material.DIAMOND))
}
```

### CMatterPredicate での割り込みチェック

```kotlin
val predicate = CMatterPredicate { ctx ->
    // 非同期実行中かどうかを確認
    if (ctx.isAsync) {
        val asyncCtx = ctx.asyncContext ?: return@CMatterPredicate false
        if (asyncCtx.isInterrupted()) {
            return@CMatterPredicate false
        }
    }
    // アイテムの検査処理
    ctx.input.itemMeta?.displayName() != null
}
```

:::caution
非同期スレッド上では BukkitAPI のワールド・エンティティへのアクセスが安全ではありません。
プレイヤーへのアイテム付与や座標取得などは同期スレッドで行うか、`ctx.crafterID` などの UUID を経由して後処理してください。
:::
