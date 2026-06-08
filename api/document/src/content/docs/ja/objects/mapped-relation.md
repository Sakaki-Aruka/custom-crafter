---
title: MappedRelation / MappedRelationComponent
---

## 概要

`MappedRelation` と `MappedRelationComponent` は、レシピの座標と実際の入力スロットの座標の対応関係を保持するデータクラスです。

定形レシピ (`SHAPED`) ではプレイヤーが盤面のどこにアイテムを配置しても形が一致すれば通過しますが、
この「レシピ上の座標」と「実際に配置された座標」の対応を記録するのが `MappedRelation` です。

---

## クラス定義

```kotlin
data class MappedRelation(
    val components: Set<MappedRelationComponent>
)

data class MappedRelationComponent(
    val recipe: CoordinateComponent,
    val input: CoordinateComponent
)
```

| フィールド | 型 | 概要 |
|------------|-----|------|
| `MappedRelation.components` | `Set<MappedRelationComponent>` | 対応関係のコンポーネント集合 |
| `MappedRelationComponent.recipe` | `CoordinateComponent` | レシピ定義上の座標 |
| `MappedRelationComponent.input` | `CoordinateComponent` | 実際に配置されたスロットの座標 |

---

## 利用される場面

`MappedRelation` は以下のコンテキストで渡されます:

| コンテキスト | フィールド名 |
|-------------|-------------|
| `ResultSupplier.Context` | `relation` |
| `CRecipePredicate.Context` | `relation` |
| `SearchResult` | `relation` (取得メソッド経由) |

---

## 使用例

`ResultSupplier` 内でどのスロットに何が配置されているかを確認する例:

```kotlin
val supplier = ResultSupplier { ctx ->
    // レシピ座標 (0,0) に対応する入力座標を取得する
    val recipeCoord = CoordinateComponent(0, 0)
    val inputCoord: CoordinateComponent? = ctx.relation.components
        .firstOrNull { it.recipe == recipeCoord }
        ?.input

    val inputItem: ItemStack = inputCoord?.let { ctx.mapped[it] } ?: ItemStack.empty()

    listOf(ItemStack.of(Material.DIAMOND, inputItem.amount))
}
```
