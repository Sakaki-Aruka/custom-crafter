---
title: CMatterImpl
---

## CMatterImpl とは

`CMatterImpl` は `CMatter` インターフェースの標準実装クラスです。
`open class` として定義されているため、継承して独自フィールドを追加した拡張クラスを作ることもできます。

---

## コンストラクタ

```kotlin
open class CMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int = 1,
    override val anyAmount: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = defaultMatterPredicates()
) : CMatter
```

| パラメータ | デフォルト値 | 概要 |
|------------|-------------|------|
| `name` | — | CMatter の識別名。重複しても動作するが一意が推奨 |
| `candidate` | — | 受け付けるアイテムの種類の集合 |
| `amount` | `1` | 要求する最小個数 |
| `anyAmount` | `false` | `true` にすると個数を問わず 1 個以上で通過し、一括作成の個数計算から除外される |
| `predicates` | `defaultMatterPredicates()` | 追加の検査ロジックセット。省略時はエンチャント・ストアドエンチャント・ポーションのデフォルトチェッカーが設定される |

:::caution
`predicates` の省略時デフォルトには `CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER` / `CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER` / `CPotionMatterImpl.DEFAULT_POTION_CHECKER` の 3 つが含まれます。
これらはそれぞれの派生型 (`CEnchantMatter`, `CEnchantmentStoreMatter`, `CPotionMatter`) にキャストできない場合は即座に `true` を返すため、`CMatterImpl` をそのまま使う限り実質的に何も検査しません。
`null` を渡した場合は predicates が一切実行されません。
:::

---

## ファクトリメソッド

コンストラクタを直接呼ぶ代わりに、以下のファクトリメソッドを使うと簡潔に書けます。
いずれも内部で `isValidMatter()` を呼び出してバリデーションを行います。

### `of(vararg materials)`

複数の `Material` を受け取り、それらを `candidate` とした `CMatterImpl` を返します。
`name` は各 Material 名を `-` でつないだ文字列になります。
`amount = 1`, `anyAmount = false`, `predicates = defaultMatterPredicates()` で作成されます。

```kotlin
// 石または丸石を受け付ける CMatter
val stones = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)

// Tag から全種類の原木を候補にする
val log = CMatterImpl.of(*Tag.LOGS.getValues().toTypedArray())
```

### `single(material)` / `multi(vararg materials)`

いずれも `of()` の別名です。可読性のための使い分けを想定しています。

```kotlin
val stone = CMatterImpl.single(Material.STONE)        // 1 種類
val stones = CMatterImpl.multi(Material.STONE, Material.COBBLESTONE)  // 複数種類
```

---

## defaultMatterPredicates()

`CMatterImpl.defaultMatterPredicates()` はエンチャント・ストアドエンチャント・ポーションの 3 種類のデフォルトチェッカーを含む `Set<CMatterPredicate>` を返します。

```kotlin
val predicates: Set<CMatterPredicate> = CMatterImpl.defaultMatterPredicates()
// = setOf(
//     CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER,
//     CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER,
//     CPotionMatterImpl.DEFAULT_POTION_CHECKER
// )
```

これを継承クラスのコンストラクタ引数に渡すことで、派生クラスと同じ predicates セットを手動で構築できます。

---

## 使用例

### 最小構成

```kotlin
// 石のみ受け付ける (predicates = null)
val stone = CMatterImpl.of(Material.STONE)
```

### 個数指定あり

```kotlin
// 石を 8 個以上要求する
val stoneStack = CMatterImpl(
    name = "stone-stack",
    candidate = setOf(Material.STONE),
    amount = 8
)
```

### anyAmount = true (スタック不可アイテム向け)

```kotlin
// マグマバケツは個数計算に関与させない
val lavaBucket = CMatterImpl(
    name = "lava-bucket",
    candidate = setOf(Material.LAVA_BUCKET),
    anyAmount = true
)
```

### predicates を追加で挿入する

デフォルト predicates に加えて独自の検査を追加したい場合は、`defaultMatterPredicates()` の結果にマージします。

```kotlin
val myChecker = CMatterPredicate { ctx ->
    // カスタム条件: アイテムのカスタム名が "特別な石" でなければならない
    ctx.input.itemMeta?.displayName()?.let { name ->
        name == Component.text("特別な石")
    } ?: false
}

val specialStone = CMatterImpl(
    name = "special-stone",
    candidate = setOf(Material.STONE),
    predicates = CMatterImpl.defaultMatterPredicates() + myChecker
)
```

### 継承して独自フィールドを追加する

```kotlin
// アイテムのカスタムモデルデータを検査する例
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
