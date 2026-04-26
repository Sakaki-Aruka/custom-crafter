---
title: CMatter について
---

## CMatter とは

CMatter とは CustomCrafterAPI においてレシピの中核を成す要素です。
CMatter はインターフェースであり、利用する際は標準の実装である `CMatterImpl` や、 CMatter を実装した独自のクラスを用いることになります。
レシピ検査時に呼び出され、様々な要素を用いたチェックを行うことが出来ます。  

## 各フィールドの役割

CMatter のフィールドのうち、 candidate, anyAmount, predicates について解説します。  

### candidate

candidate は文字の通り「候補」を示すものであり、「配置されたアイテムの種類が何であればよいのか」ということを示します。  
そのため candidate に指定されていない種類のアイテムが配置されている場合には、検査に失敗したとみなされます。  
棒のレシピでは作成するために必要な木材はどの木のものでも問題ありませんが、それと似たような制限を独自に定めることができます。  

```kotlin
val stoneOrCobblestone = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
// 配置されたアイテムが石もしくは丸石であれば良い

val onlyStone = CMatterImpl.of(Material.STONE)
// 配置されたアイテムが石でなければいけない
```

一部の例外を除き、要素を 0 個にすることや `Material.isAir == true` になるようなアイテムの種類を指定した場合には、レシピ登録時のチェックで弾かれます。  

### anyAmount

anyAmount は少し特殊な値で、false に指定されている場合はレシピ作成に影響を及ぼしませんが、 true に設定するといくつかの挙動が変更されます。  
true に設定すると、 CMatter の個数指定 (`amount`) を無視するようになり、アイテム作成時の個数計算にも影響を及ぼします。  
個数指定を無視するというのは、この値が「1 つでもアイテムが存在していれば良いことを示すフラグ」であるためです。  
1 つでも存在していれば良い、とは「CMatter の個数以外すべての検査を通過するアイテム」の個数が 1 以上であれば良いことを示します。  
そのため、レシピの成果物を作成する関数の呼び出し回数を計算する際に、 anyAmount = true が指定された CMatter に対応するアイテムの個数は考慮されず、それ以外のアイテムの個数を CMatter の個数指定で割った中の最も小さい値が使用されます。  

この性質は、スタック可能なアイテムと水バケツやポーションなど通常ではスタック出来ないアイテムを同時に扱いやすくするものです。  

```kotlin
val wetSponge = CMatterImpl.of(Material.WET_SPONGE) // anyAmount = false
val lavaBucket = CMatterImpl(
    name = "lava bucket",
    candidate = setOf(Material.LAVA_BUCKET),
    anyAmount = true
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

// このレシピは濡れたスポンジとマグマバケツを要求し、
// 濡れたスポンジと同数の乾いたスポンジを返す
```

### predicates

predicates には candidate だけでは検査できない領域の検査処理を含めることができます。  
`CMatterPredicate` という関数型インターフェースの集合であり、それらは `CMatterPredicate.Context` (コンテキスト) を受け取って Boolean を返さなくてはなりません。  
アイテムに対しての検査だけでなく、データベースやファイルから情報を読み込んで判定を行うことも可能です。  
非同期スレッドで実行される可能性があるため、コンテキストの `isAsync` を呼び出して実行されているスレッドが非同期スレッドであるかを確認することを推奨します。  

<details><summary>CPotionMatter の検査処理のデフォルト実装</summary>

```kotlin
// CPotionMatter の検査処理のデフォルト実装
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
非同期実行には Java 21 以降で利用可能な仮想スレッドが利用されます。  

Java の非同期処理は協調的に動作するので、処理が不要になっても強制的に割り込むことが出来ません。  
そのため適宜非同期コンテキスト (`asyncContext`) の `isInterrupted()` を監視し、割り込み処理 (処理中断の要請) があるかを確認するようにしてください。  
:::

## その他

CustomCrafterAPI に登録するレシピに使用されている CMatter はいくつかの基準を満たしている必要があり、 `CMatter#isValidMatter` をオーバーライドしない限りはデフォルトのチェック機構がレシピを登録するたびに呼びされ整合性を確認します。  
`isValidMatter` のチェックを通過しなかった CMatter を含むレシピは CustomCrafterAPI へ登録することは出来ません。  

## 派生

### CEnchantMatter(Impl)

エンチャントを含むアイテム向けの拡張がされた CMatter 実装です。
エンチャントの制限を定義した `CEnchantComponent` の集合 (`enchantComponents`) を持ちます。
集合内のすべての `CEnchantComponent` が合致した場合のみ検査を通過します。

`CEnchantComponent` は以下のフィールドを持ちます。

| フィールド | 型 | 概要 |
|------------|-----|------|
| `level` | `Int` | 要求するエンチャントのレベル |
| `enchantment` | `Enchantment` | 要求するエンチャントの種類 |
| `strict` | `CEnchantComponent.Strict` | 制限の強さ |

`CEnchantComponent.Strict` の値は以下のとおりです。

| 値 | 概要 |
|----|------|
| `ONLY_ENCHANT` | 指定した種類のエンチャントが含まれていれば良い (レベルは問わない) |
| `STRICT` | 指定した種類かつ指定したレベルのエンチャントを含む必要がある |

```kotlin
// 効率強化 I 以上のダイヤモンドツルハシを要求する
val efficientPickaxe = CEnchantMatterImpl(
    name = "efficient-pickaxe",
    candidate = setOf(Material.DIAMOND_PICKAXE),
    amount = 1,
    anyAmount = false,
    predicates = null,
    enchantComponents = setOf(
        CEnchantComponent(
            level = 1,
            enchantment = Enchantment.EFFICIENCY,
            strict = CEnchantComponent.Strict.ONLY_ENCHANT // レベルは 1 以上なら何でも良い
        )
    )
)

// 耐久力ちょうど III のダイヤモンドツルハシを要求する
val durablePickaxe = CEnchantMatterImpl(
    name = "durable-pickaxe",
    candidate = setOf(Material.DIAMOND_PICKAXE),
    amount = 1,
    anyAmount = false,
    predicates = null,
    enchantComponents = setOf(
        CEnchantComponent(
            level = 3,
            enchantment = Enchantment.UNBREAKING,
            strict = CEnchantComponent.Strict.STRICT // ちょうど III でなければいけない
        )
    )
)
```

この `CEnchantComponent` はエンチャント本への制限などにも利用可能です。

### CEnchantmentStoreMatter(Impl)

エンチャントを格納したアイテム (主にエンチャント本) 向けの拡張がされた CMatter 実装です。
`CEnchantMatter` と同じく `CEnchantComponent` を持ちますが、フィールド名は `storedEnchantComponents` です。
アイテムに直接付与されたエンチャントではなく、格納されたエンチャント (Stored Enchantments) を検査します。

```kotlin
// 効率強化 III が格納されたエンチャント本を要求する
val efficiencyBook = CEnchantmentStoreMatterImpl(
    name = "efficiency-book",
    candidate = setOf(Material.ENCHANTED_BOOK),
    amount = 1,
    anyAmount = true,
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

`CEnchantMatter` と `CEnchantmentStoreMatter` の両方を実装した CMatter 実装です。
アイテムに直接付与されたエンチャントと格納されたエンチャントの両方を 1 つの CMatter で検査したい場合に使用します。

```kotlin
// 直接付与とストアドの両方のエンチャントを検査する
val bothEnchant = CEnchantBothMatterImpl(
    name = "both-enchant",
    candidate = setOf(Material.DIAMOND_SWORD, Material.ENCHANTED_BOOK),
    amount = 1,
    anyAmount = false,
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

ポーション向けの拡張がされた CMatter 実装です。
ポーションの制限を定義した `CPotionComponent` の集合 (`potionComponents`) を持ちます。
集合内のすべての `CPotionComponent` が合致した場合のみ検査を通過します。

`CPotionComponent` は以下のフィールドを持ちます。

| フィールド | 型 | 概要 |
|------------|-----|------|
| `effect` | `PotionEffect` | 要求するポーション効果 (種類・効果時間・レベルを含む) |
| `strict` | `CPotionComponent.Strict` | 制限の強さ |

`CPotionComponent.Strict` の値は以下のとおりです。

| 値 | 概要 |
|----|------|
| `ONLY_EFFECT` | 指定した種類のポーション効果が含まれていれば良い (レベルは問わない) |
| `STRICT` | 指定した種類かつ指定したレベルのポーション効果を含む必要がある |

```kotlin
// 発光効果 (レベル問わず) が付いたポーションを要求する
val glowingPotion = CPotionMatterImpl(
    name = "glowing-potion",
    candidate = setOf(Material.POTION, Material.SPLASH_POTION),
    amount = 1,
    anyAmount = true,
    predicates = null,
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.GLOWING, 200, 0),
            strict = CPotionComponent.Strict.ONLY_EFFECT // レベルは問わない
        )
    )
)

// 耐火ポーション (レベル 0、効果時間 600 ティック) を厳密に要求する
val fireResistPotion = CPotionMatterImpl(
    name = "fire-resist-potion",
    candidate = setOf(Material.POTION),
    amount = 1,
    anyAmount = true,
    predicates = null,
    potionComponents = setOf(
        CPotionComponent(
            effect = PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0),
            strict = CPotionComponent.Strict.STRICT // 種類とレベルが完全一致する必要がある
        )
    )
)
```
