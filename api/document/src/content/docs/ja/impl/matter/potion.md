---
title: CPotionMatterImpl
---

## CPotionMatterImpl とは

`CPotionMatterImpl` はポーション効果を持つアイテムの検査を行う `CPotionMatter` インターフェースの標準実装クラスです。
`open class` として定義されているため、継承して拡張することもできます。

---

## コンストラクタ

```kotlin
open class CPotionMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val potionComponents: Set<CPotionComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CPotionMatter
```

| パラメータ | デフォルト値 | 概要 |
|------------|-------------|------|
| `name` | — | CMatter の識別名 |
| `candidate` | — | 受け付けるアイテムの種類の集合 |
| `potionComponents` | — | 要求するポーション効果の条件セット |
| `amount` | `1` | 要求する最小個数 |
| `mass` | `false` | `true` にすると個数計算から除外される |
| `predicates` | `defaultMatterPredicates()` | 追加の検査ロジックセット |

:::note
ポーション系アイテム (`POTION`, `SPLASH_POTION`, `LINGERING_POTION`) はスタックできないため、
`mass = true` を指定して個数計算から除外することを推奨します。
:::

---

## DEFAULT_POTION_CHECKER

`CPotionMatterImpl.DEFAULT_POTION_CHECKER` はポーション効果を検査するデフォルトの `CMatterPredicate` です。
`CMatterImpl.defaultMatterPredicates()` に含まれているため、`predicates` を省略すると自動的に有効になります。

処理の流れ:
1. 入力アイテムが Air であれば `true` を返す
2. `CMatter` を `CPotionMatter` にキャストできない場合は `true` を返す
3. `potionComponents` が空であれば `true` を返す
4. アイテムのメタが `PotionMeta` でない場合は `false` を返す (ポーション効果の条件があるのにポーションでない)
5. `basePotionType` のエフェクトと `customEffects` を収集する
6. すべての `potionComponents` がいずれかのエフェクトに合致するかを確認する

```kotlin
val DEFAULT_POTION_CHECKER = CMatterPredicate { ctx ->
    if (ctx.input.type.isAir) return@CMatterPredicate true
    val potionMatter = ctx.matter as? CPotionMatter
        ?: return@CMatterPredicate true

    if (potionMatter.potionComponents.isEmpty()) return@CMatterPredicate true
    else if (ctx.input.itemMeta !is PotionMeta) return@CMatterPredicate false

    // Key: ポーション効果の種類, Value: エフェクトレベル (amplifier)
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

`CPotionComponent` はポーション効果の条件を表すデータクラスです。

| フィールド | 型 | 概要 |
|------------|-----|------|
| `effect` | `PotionEffect` | 要求するポーション効果 (種類・効果時間・レベルを含む) |
| `strict` | `CPotionComponent.Strict` | 制限の強さ |

| `Strict` 値 | 概要 |
|-------------|------|
| `ONLY_EFFECT` | 効果の種類が含まれていれば良い (レベル・時間は問わない) |
| `STRICT` | 効果の種類とレベル (`amplifier`) が一致する必要がある |

:::note
`STRICT` では `PotionEffect` の `amplifier` (レベル - 1 の値) が比較されます。
例えば「速さ II」は `amplifier = 1` です。
効果時間 (`duration`) は `STRICT` でも比較対象ではありません。
:::

---

## 使用例

### 発光効果のポーション (レベル問わず)

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
    mass = true
)
```

### 速さ II のポーション (レベル厳密指定)

```kotlin
// 速さ II は amplifier = 1
val swiftPotion = CPotionMatterImpl(
    name = "swift-potion",
    candidate = setOf(Material.POTION),
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.SPEED, 200, 1),
            strict = CPotionComponent.Strict.STRICT
        )
    ),
    mass = true
)
```

### 複数効果の条件

```kotlin
// 耐火 + 再生を両方持つスプラッシュポーション
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
    mass = true
)
```

### predicates を null にして効果なしポーションを許可する

```kotlin
// potionComponents を空にして predicates を null に設定すると、
// 種類のチェックのみ行い、ポーション効果は問わない
val anyPotion = CPotionMatterImpl(
    name = "any-potion",
    candidate = setOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION),
    potionComponents = emptySet(),
    predicates = null
)
```

---

## 継承して拡張する

```kotlin
// 特定の複数効果セットをあらかじめ持った CMatter を定義する例
class HealingPotionMatter(name: String) : CPotionMatterImpl(
    name = name,
    candidate = setOf(Material.POTION),
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0),
            strict = CPotionComponent.Strict.ONLY_EFFECT
        )
    ),
    mass = true
)
```
