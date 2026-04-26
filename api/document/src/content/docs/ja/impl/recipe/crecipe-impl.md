---
title: CRecipeImpl
---

## CRecipeImpl とは

`CRecipeImpl` は `CRecipe` インターフェースの標準実装クラスです。
定形・不定形のどちらのレシピも作成できます。
`open class` として定義されているため、継承して独自フィールドを持ったレシピを作ることもできます。

---

## コンストラクタ

```kotlin
open class CRecipeImpl @JvmOverloads constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipe.Type,
    override val predicates: List<CRecipePredicate>? = null,
    override val results: List<ResultSupplier>? = null,
) : CRecipe
```

| パラメータ | デフォルト値 | 概要 |
|------------|-------------|------|
| `name` | — | レシピの識別名 |
| `items` | — | 座標とアイテム条件のマッピング |
| `type` | — | `CRecipe.Type.SHAPED` または `CRecipe.Type.SHAPELESS` |
| `predicates` | `null` | レシピ全体の追加検査ロジック |
| `results` | `null` | 成果物を生成する `ResultSupplier` のリスト |

---

## ファクトリメソッド: `shapeless()`

不定形レシピを簡潔に作成するためのファクトリメソッドです。
`items` に `CMatter` のリストを渡すと、座標の割り当てを自動で行います。
内部で `isValidRecipe()` を呼び出してバリデーションを行い、失敗した場合は例外をスローします。

```kotlin
@JvmStatic
fun shapeless(
    name: String,
    items: List<CMatter>,
    predicates: List<CRecipePredicate>? = null,
    results: List<ResultSupplier>? = null,
): CRecipeImpl
```

`items` のサイズは 1 以上 36 以下である必要があります。

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val result = ResultSupplier.timesSingle(ItemStack.of(Material.GRAVEL))

// 石 4 個を不定形で要求するレシピ
val recipe = CRecipeImpl.shapeless(
    name = "stone-to-gravel",
    items = List(4) { stone },
    results = listOf(result)
)
```

---

## 使用例

### 定形レシピ

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val result = ResultSupplier.timesSingle(ItemStack.of(Material.CHISELED_STONE_BRICKS))

// 2x2 の正方形に石を配置するレシピ
val shaped = CRecipeImpl(
    name = "chiseled-stone-bricks",
    items = CoordinateComponent.squareFill(2).associateWith { stone },
    results = listOf(result),
    type = CRecipe.Type.SHAPED
)
```

### 不定形レシピ

```kotlin
val apple = CMatterImpl.of(Material.APPLE)
val result = ResultSupplier.single(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE))

// リンゴ 8 個を任意の配置で要求するレシピ
val shapeless = CRecipeImpl.shapeless(
    name = "apple-to-notch-apple",
    items = List(8) { apple },
    results = listOf(result)
)
```

### 文字列レイアウトを使った定形レシピ

`CoordinateComponent.recipeMapFromStringList()` を使うと、クラフトグリッドをカンマ区切りの文字列リストで宣言できます。座標を一つずつ指定するより可読性が高くなります。

```kotlin
val gold = CMatterImpl.of(Material.GOLD_BLOCK)
val apple = CMatterImpl.of(Material.APPLE)
val result = ResultSupplier.timesSingle(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE))

// g → 金ブロック, a → リンゴ, _ → 空スロット (スキップ)
val items = CoordinateComponent.recipeMapFromStringList(
    lines = listOf(
        "g,g,g",
        "g,a,g",
        "g,g,g"
    ),
    map = mapOf("g" to gold, "a" to apple)
)

val recipe = CRecipeImpl(
    name = "golden-apple",
    items = items,
    type = CRecipe.Type.SHAPED,
    results = listOf(result)
)
```

逆に、各 `CMatter` が占める座標のセットを渡す `CoordinateComponent.mapToRecipeMap()` も利用できます。

```kotlin
val items = CoordinateComponent.mapToRecipeMap(
    mapOf(
        gold to setOf(
            CoordinateComponent(0, 0), CoordinateComponent(1, 0), CoordinateComponent(2, 0),
            CoordinateComponent(0, 1),                            CoordinateComponent(2, 1),
            CoordinateComponent(0, 2), CoordinateComponent(1, 2), CoordinateComponent(2, 2)
        ),
        apple to setOf(CoordinateComponent(1, 1))
    )
)
```

---

### predicates を使った制限

```kotlin
val diamond = CMatterImpl.of(Material.DIAMOND)
val result = ResultSupplier.single(ItemStack.of(Material.NETHER_STAR))

// OP プレイヤーのみ作成できるレシピ
val opRecipe = CRecipeImpl(
    name = "op-only-star",
    items = CoordinateComponent.squareFill(3).associateWith { diamond },
    type = CRecipe.Type.SHAPED,
    results = listOf(result),
    predicates = listOf(CRecipePredicate { ctx ->
        ctx.player?.isOp ?: false
    })
)
```

### 継承して独自フィールドを追加する

```kotlin
// レシピにカテゴリ情報を持たせる例
open class CategorizedRecipe(
    name: String,
    items: Map<CoordinateComponent, CMatter>,
    type: CRecipe.Type,
    val category: String,
    predicates: List<CRecipePredicate>? = null,
    results: List<ResultSupplier>? = null,
) : CRecipeImpl(name, items, type, predicates, results)
```

---

## バリデーション

`CRecipeImpl.isValidRecipe()` でレシピが登録可能な状態かを確認できます。
`CustomCrafterAPI.registerRecipe()` は内部でこのメソッドを呼び出しており、失敗した場合は `IllegalStateException` がスローされます。

```kotlin
val recipe = CRecipeImpl(/* ... */)
recipe.isValidRecipe().exceptionOrNull()?.let { e ->
    println("レシピが無効です: ${e.message}")
    return
}
CustomCrafterAPI.registerRecipe(recipe)
```
