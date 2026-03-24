---
title: CMatter について
---

## CMatter とは

CMatter とは CustomCrafterAPI においてレシピの中核を成す要素です。
CMatter はインターフェースであり、利用する際は標準の実装である `CMatterImpl` や、 CMatter を実装した独自のクラスを用いることになります。
レシピ検査時に呼び出され、様々な要素を用いたチェックを行うことが出来ます。  

## 各フィールドの役割

CMatter のフィールドのうち、 candidate, mass, predicates について解説します。  

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

### mass

mass は少し特殊な値で、false に指定されている場合はレシピ作成に影響を及ぼしませんが、 true に設定するといくつかの挙動が変更されます。  
true に設定すると、 CMatter の個数指定 (`amount`) を無視するようになり、アイテム作成時の個数計算にも影響を及ぼします。  
個数指定を無視するというのは、この値が「1 つでもアイテムが存在していれば良いことを示すフラグ」であるためです。  
1 つでも存在していれば良い、とは「CMatter の個数以外すべての検査を通過するアイテム」の個数が 1 以上であれば良いことを示します。  
そのため、レシピの成果物を作成する関数の呼び出し回数を計算する際に、 mass = true が指定された CMatter に対応するアイテムの個数は考慮されず、それ以外のアイテムの個数を CMatter の個数指定で割った中の最も小さい値が使用されます。  

この性質は、スタック可能なアイテムと水バケツやポーションなど通常ではスタック出来ないアイテムを同時に扱いやすくするものです。  

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
エンチャントの制限を定義した `CEnchantComponent` の集合を持ちます。  

CEnchantComponent は
- レベル
- エンチャントの種類
- 制限の強さ
  - 指定された種類のエンチャントが含まれていれば良い
  - 指定された種類とレベルのエンチャントを含む必要がある

を要素に持ち、配置されたアイテムがそれらの制限に合致するかどうかを判定します。  

この `CEnchantComponent` はエンチャント本への制限などにも利用可能です。  

### CEnchantmentStoreMatter(Impl)

エンチャントを付与可能なアイテム (主にエンチャント本) 向けの拡張がされた CMatter 実装です。  
CEnchantMatter と同じく、 `CEnchantComponent` を持ちます。  

### CEnchantBothMatterImpl

CEnchantMatter, CEnchantmentStoreMatter の両方を実装した CMatter 実装です。

### CPotionMatter(Impl)

ポーション向けの拡張がされた CMatter 実装です。  
ポーションの制限を定義した `CPotionComponent` の集合を持ちます。  

CPotionComponent は
- PotionEffect (種類、効果時間、レベルなどを含む)
- 制限の強さ
  - 指定された種類のポーションが含まれていれば良い
  - 指定された種類とレベルのポーションを含む必要がある

を要素に持ち、配置されたアイテムがそれらの制限に合致するかどうかを判定します。  
