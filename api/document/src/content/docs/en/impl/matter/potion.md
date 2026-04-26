---
title: CPotionMatterImpl
---

## What is CPotionMatterImpl

`CPotionMatterImpl` is the standard implementation class for the `CPotionMatter` interface, which inspects items that have potion effects.
Because it is defined as an `open class`, you can also extend it through inheritance.

---

## Constructor

```kotlin
open class CPotionMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val potionComponents: Set<CPotionComponent>,
    override val amount: Int = 1,
    override val anyAmount: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CPotionMatter
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `name` | — | Identifier name for the CMatter |
| `candidate` | — | The set of item types that are accepted |
| `potionComponents` | — | The set of potion effect conditions required |
| `amount` | `1` | Minimum number of items required |
| `anyAmount` | `false` | When `true`, excluded from quantity calculations |
| `predicates` | `defaultMatterPredicates()` | Additional validation logic set |

:::note
Potion-type items (`POTION`, `SPLASH_POTION`, `LINGERING_POTION`) cannot be stacked, so it is recommended to set `anyAmount = true` to exclude them from quantity calculations.
:::

---

## DEFAULT_POTION_CHECKER

`CPotionMatterImpl.DEFAULT_POTION_CHECKER` is the default `CMatterPredicate` that inspects potion effects.
Because it is included in `CMatterImpl.defaultMatterPredicates()`, it becomes active automatically when `predicates` is omitted.

Processing flow:
1. If the input item is Air, return `true`
2. If `CMatter` cannot be cast to `CPotionMatter`, return `true`
3. If `potionComponents` is empty, return `true`
4. If the item's meta is not `PotionMeta`, return `false` (potion effect conditions exist but the item is not a potion)
5. Collect effects from `basePotionType` and `customEffects`
6. Verify that all `potionComponents` match at least one of the collected effects

```kotlin
val DEFAULT_POTION_CHECKER = CMatterPredicate { ctx ->
    if (ctx.input.type.isAir) return@CMatterPredicate true
    val potionMatter = ctx.matter as? CPotionMatter
        ?: return@CMatterPredicate true

    if (potionMatter.potionComponents.isEmpty()) return@CMatterPredicate true
    else if (ctx.input.itemMeta !is PotionMeta) return@CMatterPredicate false

    // Key: potion effect type, Value: effect level (amplifier)
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

---

## CPotionComponent

`CPotionComponent` is a data class that represents a potion effect condition.

| Field | Type | Description |
|-------|------|-------------|
| `effect` | `PotionEffect` | The required potion effect (includes type, duration, and level) |
| `strict` | `CPotionComponent.Strict` | The strictness of the check |

| `Strict` value | Description |
|----------------|-------------|
| `ONLY_EFFECT` | The effect type only needs to be present (level and duration are ignored) |
| `STRICT` | Both the effect type and level (`amplifier`) must match |

:::note
With `STRICT`, the `amplifier` of `PotionEffect` (the value of level - 1) is compared.
For example, Speed II has `amplifier = 1`.
The effect duration (`duration`) is not compared even under `STRICT`.
:::

---

## Usage Examples

### Potion with Glowing effect (any level)

```kotlin
val glowingPotion = CPotionMatterImpl(
    name = "glowing-potion",
    candidate = setOf(Material.POTION, Material.SPLASH_POTION),
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.GLOWING, 200, 0),
            strict = CPotionComponent.Strict.ONLY_EFFECT
        )
    ),
    anyAmount = true
)
```

### Potion with Speed II (strict level)

```kotlin
// Speed II has amplifier = 1
val swiftPotion = CPotionMatterImpl(
    name = "swift-potion",
    candidate = setOf(Material.POTION),
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.SPEED, 200, 1),
            strict = CPotionComponent.Strict.STRICT
        )
    ),
    anyAmount = true
)
```

### Multiple effect conditions

```kotlin
// Splash potion with both Fire Resistance and Regeneration
val comboPotion = CPotionMatterImpl(
    name = "combo-potion",
    candidate = setOf(Material.SPLASH_POTION),
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0),
            strict = CPotionComponent.Strict.ONLY_EFFECT
        ),
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.REGENERATION, 100, 0),
            strict = CPotionComponent.Strict.ONLY_EFFECT
        )
    ),
    anyAmount = true
)
```

### Setting predicates to null to allow potions with no effects

```kotlin
// Setting potionComponents to empty and predicates to null
// checks only the item type and does not inspect potion effects
val anyPotion = CPotionMatterImpl(
    name = "any-potion",
    candidate = setOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION),
    potionComponents = emptySet(),
    predicates = null
)
```

---

## Extending via Inheritance

```kotlin
// Example: defining a CMatter that pre-configures a specific set of effects
class HealingPotionMatter(name: String) : CPotionMatterImpl(
    name = name,
    candidate = setOf(Material.POTION),
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0),
            strict = CPotionComponent.Strict.ONLY_EFFECT
        )
    ),
    anyAmount = true
)
```
