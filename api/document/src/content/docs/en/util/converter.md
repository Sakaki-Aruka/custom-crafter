---
title: Converter
---

## What is Converter

`Converter` is a utility object that converts a set of `CoordinateComponent` coordinates into a visual string representation.
It can be used during debugging or log output to inspect the shape of a recipe.

---

## getComponentsShapeString()

Converts a collection of coordinates into an ASCII-art style string.

```kotlin
@JvmStatic
fun getComponentsShapeString(
    list: Collection<CoordinateComponent>,
    existsSlotChar: Char = '#',
    notExistsSlotChar: Char = '_'
): String
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `list` | — | Collection of coordinates |
| `existsSlotChar` | `'#'` | Character used for slots where a coordinate exists |
| `notExistsSlotChar` | `'_'` | Character used for slots where no coordinate exists |

:::note
This method is an expensive operation.
Rather than calling it frequently, limit its use to situations where it is truly needed, such as debug output or startup logging.
:::

### Usage Example

```kotlin
import io.github.sakaki_aruka.customcrafter.impl.util.Converter

// 3x3 filled
val filled = CoordinateComponent.squareFill(3)
println(Converter.getComponentsShapeString(filled))
// ###
// ###
// ###

// 3x3 frame (border only)
val frame = CoordinateComponent.square(3)
println(Converter.getComponentsShapeString(frame))
// ###
// #_#
// ###

// Custom characters
println(Converter.getComponentsShapeString(filled, existsSlotChar = 'O', notExistsSlotChar = '.'))
// OOO
// OOO
// OOO
```

---

## String.toComponent()

An extension function that converts a `String` to a `net.kyori.adventure.text.Component`.
Supports MiniMessage-formatted text.

```kotlin
fun String.toComponent(): Component
```

```kotlin
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent

val component = "<aqua>CustomCrafter</aqua>".toComponent()
player.sendMessage(component)
```
