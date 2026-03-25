---
title: CMatterImpl
---

## What is CMatterImpl

`CMatterImpl` is the standard implementation class for the `CMatter` interface.
Because it is defined as an `open class`, you can also create extended subclasses that add custom fields by inheriting from it.

---

## Constructor

```kotlin
open class CMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = defaultMatterPredicates()
) : CMatter
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `name` | — | Identifier name for the CMatter. Duplicates are allowed but unique names are recommended |
| `candidate` | — | The set of item types that are accepted |
| `amount` | `1` | Minimum number of items required |
| `mass` | `false` | When `true`, passes with 1 or more items regardless of count, and is excluded from bulk-crafting quantity calculations |
| `predicates` | `defaultMatterPredicates()` | Additional validation logic set. When omitted, the default checkers for enchantments, stored enchantments, and potions are applied |

:::caution
The default value when `predicates` is omitted includes three checkers: `CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER`, `CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER`, and `CPotionMatterImpl.DEFAULT_POTION_CHECKER`.
Each of these immediately returns `true` if it cannot cast to its respective derived type (`CEnchantMatter`, `CEnchantmentStoreMatter`, `CPotionMatter`), so when using `CMatterImpl` as-is, they effectively check nothing.
If `null` is passed, no predicates are executed at all.
:::

---

## Factory Methods

Instead of calling the constructor directly, you can use the following factory methods for more concise code.
All of them call `isValidMatter()` internally to perform validation.

### `of(vararg materials)`

Accepts multiple `Material` values and returns a `CMatterImpl` with them as the `candidate`.
The `name` is a string formed by joining each Material name with `-`.
Created with `amount = 1`, `mass = false`, and `predicates = null`.

```kotlin
// A CMatter that accepts stone or cobblestone
val stones = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)

// Use a Tag to make all log variants candidates
val log = CMatterImpl.of(*Tag.LOGS.getValues().toTypedArray())
```

### `single(material)` / `multi(vararg materials)`

Both are aliases for `of()`. They are intended to be used interchangeably for readability.

```kotlin
val stone = CMatterImpl.single(Material.STONE)        // 1 type
val stones = CMatterImpl.multi(Material.STONE, Material.COBBLESTONE)  // multiple types
```

---

## defaultMatterPredicates()

`CMatterImpl.defaultMatterPredicates()` returns a `Set<CMatterPredicate>` containing the three default checkers for enchantments, stored enchantments, and potions.

```kotlin
val predicates: Set<CMatterPredicate> = CMatterImpl.defaultMatterPredicates()
// = setOf(
//     CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER,
//     CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER,
//     CPotionMatterImpl.DEFAULT_POTION_CHECKER
// )
```

By passing this to the constructor argument of a subclass, you can manually build the same predicates set as the derived classes.

---

## Usage Examples

### Minimal configuration

```kotlin
// Accept only stone (predicates = null)
val stone = CMatterImpl.of(Material.STONE)
```

### With quantity requirement

```kotlin
// Require 8 or more stone
val stoneStack = CMatterImpl(
    name = "stone-stack",
    candidate = setOf(Material.STONE),
    amount = 8
)
```

### mass = true (for non-stackable items)

```kotlin
// Exclude lava bucket from quantity calculations
val lavaBucket = CMatterImpl(
    name = "lava-bucket",
    candidate = setOf(Material.LAVA_BUCKET),
    mass = true
)
```

### Adding extra predicates

When you want to add custom validation on top of the default predicates, merge them with the result of `defaultMatterPredicates()`.

```kotlin
val myChecker = CMatterPredicate { ctx ->
    // Custom condition: the item's custom name must be "Special Stone"
    ctx.input.itemMeta?.displayName()?.let { name ->
        name == Component.text("Special Stone")
    } ?: false
}

val specialStone = CMatterImpl(
    name = "special-stone",
    candidate = setOf(Material.STONE),
    predicates = CMatterImpl.defaultMatterPredicates() + myChecker
)
```

### Inheriting to add custom fields

```kotlin
// Example: inspect the custom model data of an item
class CustomModelMatter(
    name: String,
    candidate: Set<Material>,
    val customModelData: Int
) : CMatterImpl(
    name = name,
    candidate = candidate,
    predicates = CMatterImpl.defaultMatterPredicates() + CMatterPredicate { ctx ->
        ctx.input.itemMeta?.hasCustomModelData() == true
            && ctx.input.itemMeta.customModelData == customModelData
    }
)
```
