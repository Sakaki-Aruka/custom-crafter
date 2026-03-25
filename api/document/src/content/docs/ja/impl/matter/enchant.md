---
title: エンチャント系 CMatter 実装
---

## 概要

エンチャントに関する検査を行う CMatter 実装として、以下の 3 クラスが提供されています。

| クラス | 対象 |
|--------|------|
| `CEnchantMatterImpl` | アイテムに直接付与されたエンチャント |
| `CEnchantmentStoreMatterImpl` | アイテムに格納されたエンチャント (主にエンチャント本) |
| `CEnchantBothMatterImpl` | 直接付与 + 格納の両方 |

いずれも `CMatterImpl` のサブクラスではなく、それぞれ `CEnchantMatter` / `CEnchantmentStoreMatter` インターフェースを実装した独立したクラスです。
`open class` として定義されているため、継承して拡張することもできます。

---

## CEnchantMatterImpl

### コンストラクタ

```kotlin
open class CEnchantMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CEnchantMatter
```

| パラメータ | デフォルト値 | 概要 |
|------------|-------------|------|
| `name` | — | CMatter の識別名 |
| `candidate` | — | 受け付けるアイテムの種類の集合 |
| `enchantComponents` | — | 要求するエンチャントの条件セット |
| `amount` | `1` | 要求する最小個数 |
| `mass` | `false` | `true` にすると個数計算から除外される |
| `predicates` | `defaultMatterPredicates()` | 追加の検査ロジックセット |

### DEFAULT_ENCHANT_CHECKER

`CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER` はアイテムに直接付与されたエンチャントを検査するデフォルトの `CMatterPredicate` です。
`CMatterImpl.defaultMatterPredicates()` に含まれているため、`predicates` を省略すると自動的に有効になります。

処理の流れ:
1. 入力アイテムが Air であれば `true` を返す
2. `CMatter` を `CEnchantMatter` にキャストできない場合は `true` を返す (他の CMatter 型との共存)
3. `enchantComponents` が空であれば `true` を返す
4. アイテムにエンチャントが一切ない場合は `false` を返す
5. `enchantComponents` のすべての `CEnchantComponent` が `enchantBaseCheck()` を通過するかを確認する

### enchantBaseCheck()

`enchantBaseCheck(enchants, required)` は内部ユーティリティ関数で、`CEnchantmentStoreMatterImpl` からも再利用されます。

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

`ONLY_ENCHANT` の場合はエンチャントの種類のみを確認し、レベルは問いません。
`STRICT` の場合はエンチャントの種類とレベルが完全に一致している必要があります。

### 使用例

```kotlin
// 効率強化が付いたダイヤモンドツルハシ (レベル問わず)
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

// 耐久力 III のダイヤモンドツルハシ (ちょうど III)
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

// 複数エンチャント条件 (すべての条件を満たす必要がある)
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

### コンストラクタ

```kotlin
open class CEnchantmentStoreMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val storedEnchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CEnchantmentStoreMatter
```

`CEnchantMatterImpl` と対称的な構造ですが、フィールド名が `storedEnchantComponents` である点と、
アイテムに直接付与されたエンチャントではなく `EnchantmentStorageMeta` が持つ格納済みエンチャントを検査する点が異なります。

### DEFAULT_ENCHANT_STORE_CHECKER

`CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER` は格納済みエンチャントを検査するデフォルトの `CMatterPredicate` です。
`CMatterImpl.defaultMatterPredicates()` に含まれています。

処理の流れ:
1. 入力アイテムが空 (Air) であれば `true` を返す
2. `CMatter` を `CEnchantmentStoreMatter` にキャストできない場合は `true` を返す
3. `storedEnchantComponents` が空であれば `true` を返す
4. アイテムのメタが `EnchantmentStorageMeta` でない場合は `false` を返す
5. `storedEnchants` を取得し、すべての `storedEnchantComponents` が `enchantBaseCheck()` を通過するかを確認する

### 使用例

```kotlin
// 効率強化 III が格納されたエンチャント本 (ちょうど III)
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
    mass = true  // バケツと同様にスタック不可なので mass = true が適切
)

// シャープネスが格納されたエンチャント本 (レベル問わず)
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

### コンストラクタ

```kotlin
open class CEnchantBothMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val storedEnchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
) : CEnchantMatter, CEnchantmentStoreMatter
```

`CEnchantMatter` と `CEnchantmentStoreMatter` を両方実装しています。
`DEFAULT_ENCHANT_CHECKER` と `DEFAULT_ENCHANT_STORE_CHECKER` の両方が `defaultMatterPredicates()` に含まれるため、
直接付与と格納済みの両方のエンチャントが一括で検査されます。

### 使用例

```kotlin
// ダイヤモンドソードまたはエンチャント本
// ソードであれば直接付与のシャープネスを、エンチャント本であれば格納済みのシャープネスを検査する
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
`CEnchantBothMatterImpl` では `enchantComponents` と `storedEnchantComponents` の両方の条件を満たす必要があります。
「どちらか一方でよい」という OR 条件が必要な場合は、別々の CMatter を定義して `candidate` で吸収するか、カスタムの `predicates` を実装してください。
:::

---

## CEnchantComponent 早見表

| フィールド | 型 | 概要 |
|------------|-----|------|
| `level` | `Int` | 要求するエンチャントレベル |
| `enchantment` | `Enchantment` | 要求するエンチャントの種類 |
| `strict` | `CEnchantComponent.Strict` | 制限の強さ |

| `Strict` 値 | 概要 |
|-------------|------|
| `ONLY_ENCHANT` | 種類が一致すれば良い (レベル問わず) |
| `STRICT` | 種類とレベルが完全一致する必要がある |
