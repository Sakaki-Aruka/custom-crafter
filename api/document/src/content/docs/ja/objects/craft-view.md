---
title: CraftView
---

## CraftView とは

`CraftView` はクラフト UI の現在の状態を表すデータクラスです。
プレイヤーがスロットに配置したアイテムのマッピングを保持します。

`Search.search()` の戻り値の一部として渡されるほか、`CRecipePredicate.Context` の `input` フィールドとしても利用されます。

---

## クラス定義

```kotlin
data class CraftView(
    val materials: Map<CoordinateComponent, ItemStack>
)
```

| フィールド | 型 | 概要 |
|------------|-----|------|
| `materials` | `Map<CoordinateComponent, ItemStack>` | クラフトスロットに配置されたアイテムのマッピング |

---

## メソッド

### `getDecremented(shiftUsed, recipe, relations)`

クラフト実行後のスロット状態 (消費後の `CraftView`) を返します。
`anyAmount = false` の CMatter に対応するアイテムは `amount` 分消費され、`anyAmount = true` は 1 個消費されます。
一括作成時 (`shiftUsed = true`) は消費量がクラフト回数倍になります。

```kotlin
val decremented: CraftView = craftView.getDecremented(
    shiftUsed = true,
    recipe = recipe,
    relations = mappedRelation
)
```

### `clone()`

`CraftView` の深いコピーを返します。
`materials` の各 `ItemStack` も `clone()` されます。

### `drop(world, location)`

`materials` に含まれる空でないアイテムをすべて指定したワールドの座標にドロップします。

```kotlin
craftView.drop(player.world, player.location)
```

### `excludeAir()`

Air (空スロット) を除外した `CraftView` を返します。

```kotlin
val filtered: CraftView = craftView.excludeAir()
```

---

## 使用例

### Search.search() と組み合わせる

```kotlin
val craftView = CraftView(
    materials = mapOf(
        CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)
    )
)

val results: List<SearchResult> = Search.search(craftView, player.uniqueId)
```

### CRecipePredicate での利用

```kotlin
val predicate = CRecipePredicate { ctx ->
    // ctx.input は現在の CraftView
    val placedCount = ctx.input.materials.values.count { !it.type.isAir }
    placedCount >= 3
}
```
