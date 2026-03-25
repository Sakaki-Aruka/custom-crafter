---
title: About CMatter
---

## What is CMatter?

CMatter is the central element of recipes in CustomCrafterAPI.
CMatter is an interface; when using it, you will work with the standard implementation `CMatterImpl` or a custom class that implements CMatter.
It is invoked during recipe inspection and can perform checks using a variety of conditions.

## Role of each field

This section covers the `candidate`, `mass`, and `predicates` fields of CMatter.

### candidate

`candidate` literally means "candidates" and represents "what types of items are acceptable in this slot."
If an item of a type not listed in `candidate` is placed, the inspection is considered failed.
Just as sticks can be crafted from any type of wood, you can define similar flexible constraints for your own recipes.

```kotlin
val stoneOrCobblestone = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
// Passes if the placed item is Stone or Cobblestone

val onlyStone = CMatterImpl.of(Material.STONE)
// The placed item must be Stone
```

With a few exceptions, specifying zero elements or a `Material` where `Material.isAir == true` will be rejected during the recipe registration check.

### mass

`mass` is a somewhat special value. When set to `false` it has no effect on recipe crafting, but setting it to `true` changes several behaviours.
When `true`, the CMatter's quantity specification (`amount`) is ignored, and it also affects the quantity calculation at crafting time.
The reason quantity is ignored is that this flag means "at least one item of this type needs to be present."
"At least one" means that the count of items which pass all checks other than the quantity check for this CMatter must be 1 or more.
Therefore, when calculating how many times the result-generating function should be called, items corresponding to a CMatter with `mass = true` are excluded from the calculation; instead, the smallest value obtained by dividing the count of the remaining items by their respective `amount` is used.

This property makes it easier to handle both stackable items and normally non-stackable items such as water buckets or potions together in the same recipe.

```kotlin
val wetSponge = CMatterImpl.of(Material.WET_SPONGE) // mass = false
val lavaBucket = CMatterImpl(
    name = "lava bucket",
    candidate = setOf(Material.LAVA_BUCKET),
    mass = true
)

val spongeDry = CRecipeImpl(
    name = "dry sponge",
    items = mapOf(
        CoordinateComponent(0, 0) to wetSponge,
        CoordinateComponent(1, 0) to lavaBucket
    ),
    type = CRecipe.Type.SHAPED,
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.SPONGE)))
)

// This recipe requires a Wet Sponge and a Lava Bucket,
// and returns the same number of dry Sponges as the number of Wet Sponges provided.
```

### predicates

`predicates` can contain inspection logic for areas that `candidate` alone cannot cover.
It is a set of `CMatterPredicate` — a functional interface — each of which must accept a `CMatterPredicate.Context` (the context) and return a `Boolean`.
In addition to inspecting the item itself, you can also load information from a database or file to make a judgement.
Because execution may occur on an asynchronous thread, it is recommended to call `isAsync` on the context to check whether the current thread is asynchronous.

<details><summary>Default implementation of CPotionMatter's inspection logic</summary>

```kotlin
// Default implementation of CPotionMatter's inspection logic
val DEFAULT_POTION_CHECKER = CMatterPredicate { ctx ->
  if (ctx.input.type.isAir) {
    return@CMatterPredicate true
  }
  val potionMatter = ctx.matter as? CPotionMatter
    ?: return@CMatterPredicate true

  if (potionMatter.potionComponents.isEmpty()) {
    return@CMatterPredicate true
  } else if (ctx.input.itemMeta !is PotionMeta) {
    // CPotionComponent required, but the input does not have potion effects
    return@CMatterPredicate false
  }

  // Key: Type of potion, Value: Effect level
  val sources: MutableMap<PotionEffectType, Int> = mutableMapOf()
  val potionMeta: PotionMeta = ctx.input.itemMeta as PotionMeta
  potionMeta.basePotionType?.let { p ->
    p.potionEffects.forEach { effect -> sources[effect.type] = effect.amplifier }
  }
  potionMeta.customEffects.takeIf { it.isNotEmpty() }?.let { list ->
    list.forEach { effect -> sources[effect.type] = effect.amplifier }
  }

  return@CMatterPredicate potionMatter.potionComponents.all { component ->
    when (component.strict) {
      CPotionComponent.Strict.ONLY_EFFECT -> sources.containsKey(component.effect.type)
      CPotionComponent.Strict.STRICT -> {
        sources.containsKey(component.effect.type)
                && sources.getValue(component.effect.type) == component.effect.amplifier
      }
    }
  }
}
```
</details>

:::note
Asynchronous execution uses virtual threads available in Java 21 and later.

Java's asynchronous processing works cooperatively, so it is not possible to forcibly interrupt an in-progress task.
Therefore, periodically check the async context's (`asyncContext`) `isInterrupted()` to see whether an interrupt request (a request to stop processing) has been issued.
:::

## Other

CMatter instances used in recipes registered with CustomCrafterAPI must satisfy certain criteria. Unless `CMatter#isValidMatter` is overridden, the default check mechanism is invoked every time a recipe is registered to verify integrity.
Recipes containing CMatter that does not pass the `isValidMatter` check cannot be registered with CustomCrafterAPI.

## Subtypes

### CEnchantMatter(Impl)

A CMatter implementation extended for items that have enchantments applied directly to them.
It holds a set of `CEnchantComponent` instances (`enchantComponents`) that define enchantment restrictions.
The inspection passes only when all `CEnchantComponent` instances in the set match.

`CEnchantComponent` has the following fields:

| Field | Type | Description |
|-------|------|-------------|
| `level` | `Int` | Required enchantment level |
| `enchantment` | `Enchantment` | Required enchantment type |
| `strict` | `CEnchantComponent.Strict` | Strictness of the restriction |

The possible values of `CEnchantComponent.Strict` are:

| Value | Description |
|-------|-------------|
| `ONLY_ENCHANT` | The item only needs to contain the specified enchantment type (any level is acceptable) |
| `STRICT` | The item must contain exactly the specified enchantment type at exactly the specified level |

```kotlin
// Require a Diamond Pickaxe with at least Efficiency I
val efficientPickaxe = CEnchantMatterImpl(
    name = "efficient-pickaxe",
    candidate = setOf(Material.DIAMOND_PICKAXE),
    amount = 1,
    mass = false,
    predicates = null,
    enchantComponents = setOf(
        CEnchantComponent(
            level = 1,
            enchantment = Enchantment.EFFICIENCY,
            strict = CEnchantComponent.Strict.ONLY_ENCHANT // any level of 1 or above is fine
        )
    )
)

// Require a Diamond Pickaxe with exactly Unbreaking III
val durablePickaxe = CEnchantMatterImpl(
    name = "durable-pickaxe",
    candidate = setOf(Material.DIAMOND_PICKAXE),
    amount = 1,
    mass = false,
    predicates = null,
    enchantComponents = setOf(
        CEnchantComponent(
            level = 3,
            enchantment = Enchantment.UNBREAKING,
            strict = CEnchantComponent.Strict.STRICT // must be exactly III
        )
    )
)
```

This `CEnchantComponent` can also be used for restrictions on enchanted books.

### CEnchantmentStoreMatter(Impl)

A CMatter implementation extended for items that store enchantments (primarily enchanted books).
Like `CEnchantMatter`, it holds `CEnchantComponent` instances, but the field name is `storedEnchantComponents`.
It inspects stored enchantments (Stored Enchantments) rather than enchantments applied directly to the item.

```kotlin
// Require an Enchanted Book storing Efficiency III
val efficiencyBook = CEnchantmentStoreMatterImpl(
    name = "efficiency-book",
    candidate = setOf(Material.ENCHANTED_BOOK),
    amount = 1,
    mass = true,
    predicates = null,
    storedEnchantComponents = setOf(
        CEnchantComponent(
            level = 3,
            enchantment = Enchantment.EFFICIENCY,
            strict = CEnchantComponent.Strict.STRICT
        )
    )
)
```

### CEnchantBothMatterImpl

A CMatter implementation that implements both `CEnchantMatter` and `CEnchantmentStoreMatter`.
Use this when you want to inspect both directly applied enchantments and stored enchantments within a single CMatter.

```kotlin
// Inspect both directly applied and stored enchantments
val bothEnchant = CEnchantBothMatterImpl(
    name = "both-enchant",
    candidate = setOf(Material.DIAMOND_SWORD, Material.ENCHANTED_BOOK),
    amount = 1,
    mass = false,
    predicates = null,
    enchantComponents = setOf(
        CEnchantComponent(1, Enchantment.SHARPNESS, CEnchantComponent.Strict.ONLY_ENCHANT)
    ),
    storedEnchantComponents = setOf(
        CEnchantComponent(1, Enchantment.SHARPNESS, CEnchantComponent.Strict.ONLY_ENCHANT)
    )
)
```

### CPotionMatter(Impl)

A CMatter implementation extended for potions.
It holds a set of `CPotionComponent` instances (`potionComponents`) that define potion restrictions.
The inspection passes only when all `CPotionComponent` instances in the set match.

`CPotionComponent` has the following fields:

| Field | Type | Description |
|-------|------|-------------|
| `effect` | `PotionEffect` | Required potion effect (includes type, duration, and level) |
| `strict` | `CPotionComponent.Strict` | Strictness of the restriction |

The possible values of `CPotionComponent.Strict` are:

| Value | Description |
|-------|-------------|
| `ONLY_EFFECT` | The item only needs to contain the specified potion effect type (any level is acceptable) |
| `STRICT` | The item must contain the specified potion effect type at exactly the specified level |

```kotlin
// Require a potion with the Glowing effect (any level)
val glowingPotion = CPotionMatterImpl(
    name = "glowing-potion",
    candidate = setOf(Material.POTION, Material.SPLASH_POTION),
    amount = 1,
    mass = true,
    predicates = null,
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.GLOWING, 200, 0),
            strict = CPotionComponent.Strict.ONLY_EFFECT // any level is acceptable
        )
    )
)

// Require a Fire Resistance potion strictly (level 0, duration 600 ticks)
val fireResistPotion = CPotionMatterImpl(
    name = "fire-resist-potion",
    candidate = setOf(Material.POTION),
    amount = 1,
    mass = true,
    predicates = null,
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0),
            strict = CPotionComponent.Strict.STRICT // type and level must match exactly
        )
    )
)
```
