---
title: About Coordinates
---

# What is CoordinateComponent?

`CoordinateComponent` is a class for representing positions in an inventory, used in recipes, UI, and other areas.
It is used throughout CustomCrafterAPI because it allows positions to be specified more intuitively than raw slot indices.

---

## Coordinate System

`CoordinateComponent` represents a position on the 6×6 grid using two integers, `X` and `Y`:
```
// Coordinates expressed as (X, Y)

(0,0) (1,0) (2,0) (3,0) (4,0) (5,0)
(0,1) (1,1) (2,1) (3,1) (4,1) (5,1)
(0,2) (1,2) (2,2) (3,2) (4,2) (5,2)
(0,3) (1,3) (2,3) (3,3) (4,3) (5,3)
(0,4) (1,4) (2,4) (3,4) (4,4) (5,4)
(0,5) (1,5) (2,5) (3,5) (4,5) (5,5)
```

For shaped recipes (`CRecipe.Type.SHAPED`), the coordinates define the *shape* of the arrangement. To express a 2×2 square, specifying:
```
(0,0), (1,0)
(0,1), (1,1)
```
is sufficient. With this specification, placing items at:
```
(4,4), (5,4)
(4,5), (5,5)
```
will also pass the check, since it is the same shape.

---

## Factory Methods

`CoordinateComponent` provides factory methods to efficiently generate sets of coordinates.

### `squareFill(size, dx, dy)` — fill a square

Returns the full set of coordinates that fills a square of the specified size.

```kotlin
// Generate fill coordinates for a 3×3 square
CoordinateComponent.squareFill(3)
// → (0,0) (1,0) (2,0)
//   (0,1) (1,1) (2,1)
//   (0,2) (1,2) (2,2)

// Specify an offset to start at (1,1)
CoordinateComponent.squareFill(3, dx = 1, dy = 1)
```

### `square(size, dx, dy)` — square outline

Returns only the perimeter coordinates of a square of the specified size (interior not included).

```kotlin
// Generate outline coordinates for a 3×3 square
CoordinateComponent.square(3)
// → (0,0) (1,0) (2,0)
//   (0,1)       (2,1)
//   (0,2) (1,2) (2,2)
```

### `getN(n)` — first N coordinates

Returns a list of N coordinates starting from the top-left. Primarily used when building `items` for shapeless recipes.

```kotlin
// Get coordinates for 3 slots
CoordinateComponent.getN(3)
// → [(0,0), (1,0), (2,0)]
```

### `recipeMapFromStringList(lines, map)` — convert from a list of strings

Generates an `items` map in bulk from a comma-separated list of strings and a character-to-CMatter mapping.
Empty elements in a slot are skipped. Both row count and column count can be up to 6.

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
// The 9 slots from (0,0) to (2,2) become stone, with (1,1) being apple
```

To include empty slots, leave the entry between commas blank, or use a character not registered in the mapping.

```kotlin
val lines = listOf(
    "s,s",
    ",s"   // (0,1) is skipped
)

val lines2 = listOf(
    "s,s",
    "_,s" // (0,1) is skipped (only when "_" is not registered in the map)
)
```

### `mapToRecipeMap(source)` — convert from a CMatter-keyed mapping

Generates `items` from a reverse mapping of `CMatter → Set<CoordinateComponent>`.
This reduces the amount of code needed when assigning the same CMatter to multiple slots.

```kotlin
val gold = CMatterImpl.of(Material.GOLD_BLOCK)
val apple = CMatterImpl.of(Material.APPLE)

val source = mapOf(
    gold to CoordinateComponent.squareFill(3).minus(CoordinateComponent(1, 1)),
    apple to setOf(CoordinateComponent(1, 1))
)

val items = CoordinateComponent.mapToRecipeMap(source)
// The border of the 3×3 square is gold; the centre (1,1) is apple
```

---

## getComponentsShapeString()

Converts a collection of coordinates into a string in ASCII art format.

```kotlin
@JvmStatic
fun getComponentsShapeString(
    list: Collection<CoordinateComponent>,
    existsSlotChar: Char = '#',
    notExistsSlotChar: Char = '_'
): String
```

| Parameter             | Default | Description                                        |
|-----------------------|---------|----------------------------------------------------|
| `list`                | —       | Collection of coordinates                          |
| `existsSlotChar`      | `'#'`   | Character used for slots where a coordinate exists |
| `notExistsSlotChar`   | `'_'`   | Character used for slots where no coordinate exists|

:::note
This method is slightly costly (documented as a `high-cost function` in KDoc).
Use it only when necessary — for example, during debug logging or at startup — rather than calling it frequently.
:::

### Usage Example

```kotlin
import io.github.sakaki_aruka.customcrafter.impl.util.Converter

// 3×3 fill
val filled = CoordinateComponent.squareFill(3)
println(Converter.getComponentsShapeString(filled))
// ###
// ###
// ###

// 3×3 outline
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
