---
title: Converter
---

## Converter とは

`Converter` は `CoordinateComponent` の座標集合を視覚的な文字列に変換するユーティリティオブジェクトです。
デバッグやログ出力時にレシピの形状を確認するために利用できます。

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

| パラメータ | デフォルト | 概要 |
|------------|-----------|------|
| `list` | — | 座標のコレクション |
| `existsSlotChar` | `'#'` | 座標が存在するスロットに使用する文字 |
| `notExistsSlotChar` | `'_'` | 座標が存在しないスロットに使用する文字 |

:::note
このメソッドはコスト高な処理です (KDoc に `高コスト関数` と記載されています)。
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

---

## String.toComponent()

`String` を `net.kyori.adventure.text.Component` に変換する拡張関数です。
MiniMessage 形式のテキストをサポートします。

```kotlin
fun String.toComponent(): Component
```

```kotlin
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent

val component = "<aqua>カスタムクラフター</aqua>".toComponent()
player.sendMessage(component)
```
