---
title: レシピについて
---

## CustomCrafterAPI におけるレシピ

[入門ページ](/ja/getting-started/)に書いたとおり、 CustomCrafterAPI でのレシピは Minecraft 標準のレシピとは区別され、完全に異なるコンポーネントによって構成されます。  

`CRecipe` のフィールドは以下のように定義されています。  
```kotlin
interface CRecipe {
    val name: String
    val items: Map<CoordinateComponent, CMatter>
    val predicates: List<CRecipePredicate>?
    val results: List<ResultSupplier>?
    val type: Type // CRecipe.Type
}
```

name, items, type は必須の要素であり、それぞれレシピの名前、アイテムの配置、レシピの種類 (定形・不定形) を示します。  

## name
name はレシピの名前を示し、一致レシピ全表示機能や登録済みレシピ一括表示コマンドなどで利用されます。  
登録するレシピ全体で重複なく、ユーザーが認識しやすい名前をつけることが推奨されます。  

## items
items はアイテムの配置を表し、レシピでの座標を示す `CoordinateComponent` と要求するアイテムを示す `CMatter` のマッピングです。  

`CoordinateComponent` は 6 x 6 の盤面上での座標を `X`, `Y` の 2 つの整数値で表します。  
座標は以下のとおりです。  
```
// (X, Y) で座標を示すと

(0,0) (1,0) (2,0) (3,0) (4,0) (5,0)
(0,1) (1,1) (2,1) (3,1) (4,1) (5,1)
(0,2) (1,2) (2,2) (3,2) (4,2) (5,2)
(0,3) (1,3) (2,3) (3,3) (4,3) (5,3)
(0,4) (1,4) (2,4) (3,4) (4,4) (5,4)
(0,5) (1,5) (2,5) (3,5) (4,5) (5,5)
```

定形レシピ (`CRecipe.Type.SHAPED`) ではアイテム配置の「形」を示すため、 2 マス四方の正方形の配置を示すためには
```
(0,0), (1,0)
(0,1), (1,1)
```
を指定すればよく、上記の指定では
```
(4,0), (4,0)
(4,1), (5,1)
```
にアイテムが配置されても同一の形であるとして検査を通過します。  

---

CMatter は対応するスロットに配置されるアイテムの検査条件の集合体を示します。  
最も簡単な条件である入力されるアイテムの種類だけを検査条件とする場合は、個数やエンチャント、メタデータなどに関しては検査せず `Material` だけ合致していれば検査を通過します。  

```kotlin
// アイテムの種類だけを検査条件とする場合の CMatter の取得方法
// 石、もしくは丸石を配置した場合に検査を通過する

val matter1 = CMatterImpl(
    name = "only-stone",
    candidate = setOf(Material.STONE, Material.COBBLESTONE),
    amount = 1,
    mass = false,
    predicates = CRecipeImpl.defaultMatterPredicates()
)

// 上記と同じ CMatter のより簡単な取得方法
val matter2 = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
```

デフォルト実装の CMatterImpl では name, candidate, amount, mass, predicates という要素を持ち、それぞれが以下のように働きます。  
- name: CMatter の名前 (滅多に使用されないので適当で良い)
- candidate: 入力されるアイテムの種類がここに列挙したものに当てはまるものであれば良い
- amount: 入力されるアイテムの個数がここに指定された数以上であれば良い
- mass: true が指定された場合は `amount` を無視し、個数が 1 以上であれば良く、一括作成時の個数計算に関与しない。
    false が指定されたときは `amount` の個数指定に従う
- predicates: 上記以外の要素を用いた検査ロジックを挿入できる。1 つでも false を返すものがあれば検査全体が失敗とみなされる。デフォルトではエンチャント、ポーションなどの判定ロジックが挿入される

対応する配置の入力されたアイテムに対して上記で指定した検査が実行され、 1 つでも条件に合致しないものがあればそのレシピは入力されたアイテムに対して合致しなかったと判定されます。  

### candidate
ここには

### predicates
predicates は `candidate`, `amount` では検査できない要素に対する検査ロジックを持つことができます。  
`CMatterPredicate.Context` が与えられ、それをもとに検査を行い結果を Boolean として返す `test` 関数を持つ `CMatterPredicate` の集合です。

ここでは独自のロジックを記述できるためデータベースや外部 API を呼び出して結果の判定に利用することができます。

## type
type はレシピの種類を示します。  
CustomCrafterAPI では、入力されるアイテムの配置がレシピ通りでなければいけない `定形レシピ (CRecipe.Type.SHAPED)` と、配置は関係なく、指定されたアイテムが全て存在すれば検査を通過する `不定形レシピ (CRecipe.Type.SHAPELESS)` の 2 種類を提供することができます。  



