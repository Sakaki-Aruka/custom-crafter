---
title: Enchant CMatter Implementations
---

## Overview

The following three classes are provided as CMatter implementations for inspecting enchantments.

| Class | Target |
|-------|--------|
| `CEnchantMatterImpl` | Enchantments directly applied to an item |
| `CEnchantmentStoreMatterImpl` | Enchantments stored in an item (primarily enchanted books) |
| `CEnchantBothMatterImpl` | Both directly applied and stored enchantments |

They are defined as `open class`, so they can be extended through inheritance.

---

## CEnchantMatterImpl

### Constructor

```kotlin
open class CEnchantMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val anyAmount: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CEnchantMatter
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `name` | — | Identifier name for the CMatter |
| `candidate` | — | The set of item types that are accepted |
| `enchantComponents` | — | The set of enchantment conditions required |
| `amount` | `1` | Minimum number of items required |
| `anyAmount` | `false` | When `true`, excluded from quantity calculations |
| `predicates` | `defaultMatterPredicates()` | Additional validation logic set |

### DEFAULT_ENCHANT_CHECKER

`CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER` is the default `CMatterPredicate` that inspects enchantments directly applied to an item.
Because it is included in `CMatterImpl.defaultMatterPredicates()`, it becomes active automatically when `predicates` is omitted.

Processing flow:
1. If the input item is Air, return `true`
2. If `CMatter` cannot be cast to `CEnchantMatter`, return `true` (coexistence with other CMatter types)
3. If `enchantComponents` is empty, return `true`
4. If the item has no enchantments at all, return `false`
5. Verify that all `CEnchantComponent` entries in `enchantComponents` pass `enchantBaseCheck()`

### enchantBaseCheck()

`enchantBaseCheck(enchants, required)` is an internal utility function that is also reused by `CEnchantmentStoreMatterImpl`.

```kotlin
internal fun enchantBaseCheck(
    enchants: Map<Enchantment, Int>,
    required: CEnchantComponent
): Boolean {
    return when (required.strict) {
        CEnchantComponent.Strict.ONLY_ENCHANT -> enchants.containsKey(required.enchantment)
        CEnchantComponent.Strict.STRICT -> {
            enchants.getOrDefault(required.enchantment, -1) == required.level
        }
    }
}
```

With `ONLY_ENCHANT`, only the type of enchantment is checked and the level does not matter.
With `STRICT`, both the enchantment type and level must match exactly.

### Usage Examples

```kotlin
// Diamond pickaxe with Efficiency (any level)
val efficientPickaxe = CEnchantMatterImpl(
    name = "efficient-pickaxe",
    candidate = setOf(Material.DIAMOND_PICKAXE),
    enchantComponents = setOf(
        CEnchantComponent(
            level = 1,
            enchantment = Enchantment.EFFICIENCY,
            strict = CEnchantComponent.Strict.ONLY_ENCHANT
        )
    )
)

// Diamond pickaxe with Unbreaking III (exactly III)
val durablePickaxe = CEnchantMatterImpl(
    name = "durable-pickaxe",
    candidate = setOf(Material.DIAMOND_PICKAXE),
    enchantComponents = setOf(
        CEnchantComponent(
            level = 3,
            enchantment = Enchantment.UNBREAKING,
            strict = CEnchantComponent.Strict.STRICT
        )
    )
)

// Multiple enchantment conditions (all conditions must be satisfied)
val powerfulSword = CEnchantMatterImpl(
    name = "powerful-sword",
    candidate = setOf(Material.DIAMOND_SWORD),
    enchantComponents = setOf(
        CEnchantComponent(1, Enchantment.SHARPNESS, CEnchantComponent.Strict.ONLY_ENCHANT),
        CEnchantComponent(3, Enchantment.UNBREAKING, CEnchantComponent.Strict.STRICT)
    )
)
```

---

## CEnchantmentStoreMatterImpl

### Constructor

```kotlin
open class CEnchantmentStoreMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val storedEnchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val anyAmount: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CEnchantmentStoreMatter
```

This is structurally symmetric to `CEnchantMatterImpl`, but differs in that the field name is `storedEnchantComponents` and it inspects stored enchantments held by `EnchantmentStorageMeta` rather than enchantments directly applied to the item.

### DEFAULT_ENCHANT_STORE_CHECKER

`CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER` is the default `CMatterPredicate` that inspects stored enchantments.
It is included in `CMatterImpl.defaultMatterPredicates()`.

Processing flow:
1. If the input item is Air, return `true`
2. If `CMatter` cannot be cast to `CEnchantmentStoreMatter`, return `true`
3. If `storedEnchantComponents` is empty, return `true`
4. If the item's meta is not `EnchantmentStorageMeta`, return `false`
5. Retrieve `storedEnchants` and verify that all `storedEnchantComponents` pass `enchantBaseCheck()`

### Usage Examples

```kotlin
// Enchanted book storing Efficiency III (exactly III)
val efficiencyBook = CEnchantmentStoreMatterImpl(
    name = "efficiency-book",
    candidate = setOf(Material.ENCHANTED_BOOK),
    storedEnchantComponents = setOf(
        CEnchantComponent(
            level = 3,
            enchantment = Enchantment.EFFICIENCY,
            strict = CEnchantComponent.Strict.STRICT
        )
    ),
    anyAmount = true  // Non-stackable like buckets, so anyAmount = true is appropriate
)

// Enchanted book storing Sharpness (any level)
val sharpnessBook = CEnchantmentStoreMatterImpl(
    name = "sharpness-book",
    candidate = setOf(Material.ENCHANTED_BOOK),
    storedEnchantComponents = setOf(
        CEnchantComponent(
            level = 1,
            enchantment = Enchantment.SHARPNESS,
            strict = CEnchantComponent.Strict.ONLY_ENCHANT
        )
    )
)
```

---

## CEnchantBothMatterImpl

### Constructor

```kotlin
open class CEnchantBothMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val storedEnchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val anyAmount: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CEnchantMatter, CEnchantmentStoreMatter
```

This implements both `CEnchantMatter` and `CEnchantmentStoreMatter`.
Because both `DEFAULT_ENCHANT_CHECKER` and `DEFAULT_ENCHANT_STORE_CHECKER` are included in `defaultMatterPredicates()`,
both directly applied and stored enchantments are checked together.

### Usage Example

```kotlin
// Diamond sword or enchanted book:
// if a sword, checks for Sharpness applied directly; if an enchanted book, checks for stored Sharpness
val sharpnessMatter = CEnchantBothMatterImpl(
    name = "sharpness-matter",
    candidate = setOf(Material.DIAMOND_SWORD, Material.ENCHANTED_BOOK),
    enchantComponents = setOf(
        CEnchantComponent(1, Enchantment.SHARPNESS, CEnchantComponent.Strict.ONLY_ENCHANT)
    ),
    storedEnchantComponents = setOf(
        CEnchantComponent(1, Enchantment.SHARPNESS, CEnchantComponent.Strict.ONLY_ENCHANT)
    )
)
```

:::note
With `CEnchantBothMatterImpl`, both the `enchantComponents` and `storedEnchantComponents` conditions must be satisfied.
If you need an OR condition where either one is sufficient, define separate CMatter instances and absorb them via `candidate`, or implement a custom `predicates`.
:::

---

## CEnchantComponent Quick Reference

| Field | Type | Description |
|-------|------|-------------|
| `level` | `Int` | The required enchantment level |
| `enchantment` | `Enchantment` | The required enchantment type |
| `strict` | `CEnchantComponent.Strict` | The strictness of the check |

| `Strict` value | Description |
|----------------|-------------|
| `ONLY_ENCHANT` | The type only needs to match (level is ignored) |
| `STRICT` | Both the type and level must match exactly |
