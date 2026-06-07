---
title: 座標に関して
---

# CoordinateComponent とは

CoordinateComponent はレシピや UI などでインベントリ上の座標を示すためのクラスです。  
整数で示されたスロット番号よりもより直感的に指定できることから CustomCrafterAPI では各所で利用されています。  


---

## 座標系

`CoordinateComponent` は 6 x 6 の盤面上での座標を `X`, `Y` の 2 つの整数値で表します。
座標は以下のとおりです。
```
// (X, Y) で座標を示すと

(0,0) (1,0) (2,0) (3,0) (4,0) (5,0)
(0,1) (1,1) (2,1) (3,1) (4,1) (5,1)
(0,2) (1,2) (2,2) (3,2) (4,2) (5,2)
(0,3) (1,3) (2,3) (3,3) (4,3) (5,3)
(0,4) (1,4) (2,4) (3,4) (4,4) (5,4)
(0,5) (1,5) (2,5) (3,5) (4,5) (5,5)
```

定形レシピ (`CRecipe.Type.SHAPED`) ではアイテム配置の「形」を示すため、 2 マス四方の正方形の配置を示すためには
```
(0,0), (1,0)
(0,1), (1,1)
```
を指定すればよく、上記の指定では
```
(4,4), (5,4)
(4,5), (5,5)
```
にアイテムが配置されても同一の形であるとして検査を通過します。

---

## ファクトリメソッド

`CoordinateComponent` には座標の集合を効率よく生成するためのファクトリメソッドが用意されています。

### `squareFill(size, dx, dy)` — 正方形の塗りつぶし

指定サイズの正方形をすべて埋めた座標のセットを返します。

```kotlin
// 3×3 の塗りつぶし座標を生成する
CoordinateComponent.squareFill(3)
// → (0,0) (1,0) (2,0)
//   (0,1) (1,1) (2,1)
//   (0,2) (1,2) (2,2)

// オフセットを指定して (1,1) を起点にする
CoordinateComponent.squareFill(3, dx = 1, dy = 1)
```

### `square(size, dx, dy)` — 正方形の枠線

指定サイズの正方形の外周のみの座標セットを返します（内部は含まない）。

```kotlin
// 3×3 の枠線座標を生成する
CoordinateComponent.square(3)
// → (0,0) (1,0) (2,0)
//   (0,1)       (2,1)
//   (0,2) (1,2) (2,2)
```

### `getN(n)` — 先頭 N 個の座標

左上から順に N 個の座標リストを返します。主に不定形レシピの `items` を組み立てる際に使用します。

```kotlin
// 3 スロット分の座標を取得する
CoordinateComponent.getN(3)
// → [(0,0), (1,0), (2,0)]
```

### `recipeMapFromStringList(lines, map)` — 文字列リストから変換

カンマ区切りの文字列リストと文字→CMatter のマッピングから `items` を一括生成します。
空要素のスロットはスキップされます。行数・列数は最大 6 まで指定できます。

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val apple = CMatterImpl.of(Material.APPLE)

val lines = listOf(
    "s,s,s",
    "s,a,s",
    "s,s,s"
)
val map = mapOf("s" to stone, "a" to apple)

val items = CoordinateComponent.recipeMapFromStringList(lines, map)
// (0,0)〜(2,2) の 9 スロットが stone、(1,1) だけ apple になる
```

カンマの間が空文字、もしくはマッピングに登録されていない文字の場合は空として扱います。

```kotlin
val lines = listOf(
    "s,s",
    ",s"   // (0,1) はスキップ
)

val lines2 = listOf(
    "s,s",
    "_,s" // (0, 1) はスキップ ("_" が登録されていない場合のみ)
)
```

### `mapToRecipeMap(source)` — CMatter 主体のマッピングから変換

`CMatter → Set<CoordinateComponent>` という逆向きのマッピングから `items` を生成します。
同じ CMatter を複数スロットに割り当てる場合に記述量を減らせます。

```kotlin
val gold = CMatterImpl.of(Material.GOLD_BLOCK)
val apple = CMatterImpl.of(Material.APPLE)

val source = mapOf(
    gold to CoordinateComponent.squareFill(3).minus(CoordinateComponent(1, 1)),
    apple to setOf(CoordinateComponent(1, 1))
)

val items = CoordinateComponent.mapToRecipeMap(source)
// 3×3 の枠は gold、中央 (1,1) は apple
```

---

## getComponentsShapeString()

座標の集合を ASCII アートの形式で文字列に変換します。

```kotlin
@JvmStatic
fun getComponentsShapeString(
    list: Collection<CoordinateComponent>,
    existsSlotChar: Char = '#',
    notExistsSlotChar: Char = '_'
): String
```

| パラメータ               | デフォルト | 概要                  |
|---------------------|-------|---------------------|
| `list`              | —     | 座標のコレクション           |
| `existsSlotChar`    | `'#'` | 座標が存在するスロットに使用する文字  |
| `notExistsSlotChar` | `'_'` | 座標が存在しないスロットに使用する文字 |

:::note
このメソッドはちょっとだけコストがかかる処理です (KDoc に `高コスト関数` と記載されています)。
頻繁に呼び出すのではなく、デバッグ時や起動時のログ出力など必要最小限の場面で使用してください。
:::

### 使用例

```kotlin
import io.github.sakaki_aruka.customcrafter.impl.util.Converter

// 3x3 の塗りつぶし
val filled = CoordinateComponent.squareFill(3)
println(Converter.getComponentsShapeString(filled))
// ###
// ###
// ###

// 3x3 の枠線
val frame = CoordinateComponent.square(3)
println(Converter.getComponentsShapeString(frame))
// ###
// #_#
// ###

// カスタム文字
println(Converter.getComponentsShapeString(filled, existsSlotChar = 'O', notExistsSlotChar = '.'))
// OOO
// OOO
// OOO
```
