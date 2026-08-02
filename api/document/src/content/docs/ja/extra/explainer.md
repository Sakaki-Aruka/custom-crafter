---
title: レシピ検索の高度なデバッグ
---

## Explainer とは

`Explainer` はレシピ検索の過程を記録する診断用のクラスです (5.3.0 以降)。

レシピを実装していると「合致するはずなのに検索結果に出てこない」という状況に遭遇します。
`Search.search` が返すのは合致したレシピの一覧のみであるため、合致しなかった理由 — 候補にすら入らなかったのか、アイテムの種類が違ったのか、predicate が弾いたのか — は結果からは分かりません。

`Explainer` を検索メソッドに渡すと、検索がどの判定でどう分岐したかが1行ずつ記録され、あとから読み返せるようになります。

---

## 基本的な使い方

`Search.search` および `Search.asyncSearch` の `explainer` 引数にインスタンスを渡し、検索後に `getStringList()` で読み出します。

```kotlin
val explainer = Explainer(Explainer.Loglevel.DEBUG, "debug")

val result: Search.SearchResult = Search.search(
    crafterId = player.uniqueId,
    view = view,
    sourceRecipes = listOf(myRecipe),
    explainer = explainer
)

explainer.getStringList().forEach { println(it) }
```

`explainer` 引数はデフォルトが `null` で、渡さなければ記録処理は一切行われません。
デバッグが終わったら引数を外す (あるいは `null` を渡す) だけで元の動作に戻ります。

:::caution
`Explainer` はデバッグ専用です。記録した内容はメモリ上に蓄積され続けるため、本番環境の常用や、1インスタンスを長期間使い回すことは想定していません。
:::

---

## ログレベル

コンストラクタの第1引数 `verbosity` で、どこまで詳細に記録するかを指定します。

| レベル | 記録される内容 |
|--------|----------------|
| `WARN` | 捕捉されなかったエラーなど。使用箇所はごく少数 |
| `INFO` (デフォルト) | 「何がどうなったか」。候補の絞り込み結果、合致したレシピ、検索の終了状態 |
| `DEBUG` | 「変数がどうだったか」。個々の判定に使われた具体的な値 |

`verbosity` は**しきい値**として働き、指定したレベル以下の詳細度を持つログのみが記録されます。
`Loglevel.INFO` を指定すると `INFO` と `WARN` が記録され、`DEBUG` は捨てられます。

:::note
一般的なロギングライブラリでは「重大度が高いほど数値が大きい」ことが多いですが、`Explainer.Loglevel` は**詳細度が高いほど数値が大きい**という逆の設計です (`WARN` = 1, `INFO` = 2, `DEBUG` = 3)。
「どこまで細かく記録するか」を指定するもの、と考えてください。
:::

原因を追跡する場合は `DEBUG` を指定してください。

```kotlin
// 具体的な値まですべて記録する
val explainer = Explainer(Explainer.Loglevel.DEBUG, "debug")

// 第1引数を省略すると INFO になる
val infoOnly = Explainer(name = "debug")
```

第2引数の `name` は複数のインスタンスを区別するためのラベルです。省略するとランダムな UUID が設定されます。

---

## 出力の読み方

各行は `[レベル] [関数名/検査名] 内容` という形式です。
`関数名/検査名` を見れば、どの処理のどの検査で発生した事象かが特定できます。

### 症状1: レシピが候補にすら入らない

もっとも多いのが「レシピが検索対象にすら入っていない」ケースです。
`CRecipe` は入力アイテム数が `requiresInputItemAmountMin()` 〜 `requiresInputItemAmountMax()` の範囲内にある場合のみ検索対象になります。

鉄インゴット2個を要求するレシピに対し、1個だけ置いた場合の出力です。

```
[INFO] [search/candidateFilter] candidates filtered by input size: inputSize=1, sourceRecipes=1, candidates=0, shaped=0, shapeless=0
[DEBUG] [search/candidateFilter] excluded: name=iron_plate, type=SHAPELESS, inputSize=1 not in requires=2..2
[DEBUG] [VanillaSearch/search] Bukkit#getCraftingRecipe: world=World, found=true
[INFO] [search] search finished: matchedCustoms=0, vanillaFound=true
```

`candidates=0` から候補が0件であることが分かり、続く `excluded` 行が理由を示しています。
`inputSize=1 not in requires=2..2` は「入力が1個だが、このレシピは2個を要求している」という意味です。

:::tip
`excluded` 行は `DEBUG` レベルでのみ記録されます。
「レシピがまったく反応しない」という症状のときは、まず `DEBUG` にして候補から除外されていないかを確認してください。
:::

### 症状2: 不定形レシピのスロットが埋まらない

不定形レシピ (`CRecipe.Type.SHAPELESS`) の照合は、レシピの各スロットに入力アイテムを重複なく割り当てられるかという問題として解かれます。
割り当てに失敗した場合、どのスロットがなぜ埋まらなかったかが記録されます。

鉄インゴット2個を要求するレシピに対し、鉄インゴット1個と土1個を置いた場合の出力です。

```
[INFO] [search/candidateFilter] candidates filtered by input size: inputSize=2, sourceRecipes=1, candidates=1, shaped=0, shapeless=1
[DEBUG] [search/candidateFilter] candidate(0): name=iron_plate, type=SHAPELESS, requires=2..2
[DEBUG] [shapeless/flowCheck] recipe=iron_plate, achieved=1, requiredMin=2
[DEBUG] [shapeless/flowCheck] recipe=iron_plate, group#1 unsatisfied: matched=0 < min=1, members=(1,0)
[DEBUG] [shapeless/flowCheck] recipe=iron_plate, slot=(1,0) unmatched: 1 acceptable input(s) [(0,0)] all taken by other slots
[DEBUG] [VanillaSearch/search] Bukkit#getCraftingRecipe: world=World, found=false
[INFO] [search] search finished: matchedCustoms=0, vanillaFound=false
```

今回は候補には入っています (`candidates=1`) が、割り当てが2件必要なところ1件しか成立していません (`achieved=1, requiredMin=2`)。

最後の行が原因を示しています。座標 `(1,0)` のスロットは条件を満たす入力を1つ持っていますが、それは座標 `(0,0)` の入力であり、すでに別のスロットに取られています。
つまり「鉄インゴットが1個足りない」ことが分かります。

スロットが埋まらない理由は3種類が記録されます。

| 出力 | 意味 |
|------|------|
| `no input passes candidate=[...] amount>=N` | 種類または個数の条件を満たす入力が1つも存在しない |
| `N input(s) pass candidate/amount>=N but all rejected by matter predicates` | 種類・個数は通ったが、`CMatterPredicate` がすべて弾いた |
| `N acceptable input(s) [...] all taken by other slots` | 条件を満たす入力はあるが、他のスロットに割り当て済み |

### 症状3: predicate が弾いている

`CMatterPredicate` や `CRecipePredicate` が原因の場合、どの predicate が弾いたかが記録されます。

```
[DEBUG] [shaped/recipePredicateCheck] recipe=reinforced_ingot, failed=predicate#1/2
```

`predicate#1/2` は「2つある predicate のうち添字1 (2番目) が弾いた」という意味です。
predicate に名前を付けると添字ではなく名前で表示されます。詳しくは後述の [predicate に名前を付ける](#predicate-に名前を付ける) を参照してください。

### 検索が成功した場合

合致した場合は `INFO` レベルで、確定した座標の対応関係が記録されます。

```
[INFO] [search/candidateFilter] candidates filtered by input size: inputSize=2, sourceRecipes=1, candidates=1, shaped=0, shapeless=1
[INFO] [shapeless] recipe matched: name=iron_plate, relation=(0,0)->(1,0), (1,0)->(0,0)
[INFO] [search] search finished: matchedCustoms=1, vanillaFound=false
```

`relation` は `レシピ座標->入力座標` の対応です。
上の例では、レシピの `(0,0)` に入力の `(1,0)` が、レシピの `(1,0)` に入力の `(0,0)` が割り当てられています。
不定形レシピでは並び順が入れ替わることがあるため、意図した対応になっているかの確認に使えます。

---

## predicate に名前を付ける

Kotlin のラムダ式は識別子を持たないため、そのままでは `predicate#1/2` のように添字でしか表示できません。
`CMatterPredicates.named` / `CRecipePredicates.named` で包むと、名前で表示されるようになります。

```kotlin
val undamaged = CMatterPredicates.named("undamaged") { ctx ->
    ctx.input.amount >= 64
}
```

この predicate が弾いた場合の出力です。

```
[DEBUG] [shaped/matterPredicateCheck] recipe=reinforced_ingot, coordinate=(0,0), failed=undamaged (predicate#0/1)
```

`and` などのコンビネータで合成した predicate は、名前も自動的に合成されます。

```kotlin
val combined = CMatterPredicates.named("isIron") { /* ... */ }
    .and(CMatterPredicates.named("isFullStack") { /* ... */ })
```

```
[DEBUG] [shaped/matterPredicateCheck] recipe=reinforced_ingot, coordinate=(0,0), failed=(isIron and isFullStack) (predicate#0/1)
```

合成した predicate は全体で1つとして扱われるため、内側のどちらが弾いたかまでは分かりません。
そこまで特定したい場合は、合成せずに個別の predicate としてリストに並べてください。

`named` やコンビネータの詳細は [CRecipePredicate について](/ja/recipe/predicate/) を参照してください。

---

## predicate の中からログを書く

`CMatterPredicate.Context` および `CRecipePredicate.Context` は `explainer` フィールドを持ちます (5.3.0 以降)。
これを使うと、自作の predicate が「なぜ弾いたか」を自分で記録できます。

```kotlin
val fullStack = CMatterPredicate { ctx ->
    val ok = ctx.input.amount >= 64
    ctx.explainer?.writeLog(
        Explainer.Loglevel.DEBUG,
        "full stack check: amount=${ctx.input.amount}, required=64, passed=$ok"
    )
    ok
}
```

```
[DEBUG] full stack check: amount=1, required=64, passed=false
[DEBUG] [shaped/matterPredicateCheck] recipe=reinforced_ingot, coordinate=(0,0), failed=predicate#0/1
```

`explainer` は検索時に渡された場合のみ非 null になります。
必ず `?.` を使い、渡されていない場合は何も記録しないようにしてください。

:::tip
ログは1行にまとめ、判定に使った値を含めるようにすると読みやすくなります。
「弾いた」という事実だけでなく「何と比較して弾いたか」を残すのが要点です。
:::

---

## 記録の取り出し

### getLogs

`Log` オブジェクトのリストを、記録された順に返します。
`level` と `line` のフィールドを持つため、独自の整形を行いたい場合に使用します。

```kotlin
explainer.getLogs().forEach { log ->
    if (log.level == Explainer.Loglevel.WARN) {
        plugin.logger.warning(log.line)
    }
}
```

### getStringList

`[レベル] 内容` の形式に整形した文字列のリストを返します。
引数にレベルを渡すと、そのレベルのものだけに絞り込めます。

```kotlin
// すべて取得する
val all: List<String> = explainer.getStringList()

// INFO のみに絞る (全体の流れだけを確認したい場合)
val infoOnly: List<String> = explainer.getStringList(Explainer.Loglevel.INFO)

// 複数指定も可能
val some: List<String> = explainer.getStringList(
    Explainer.Loglevel.INFO,
    Explainer.Loglevel.WARN
)
```

:::note
`getStringList` の絞り込みは、コンストラクタの `verbosity` と挙動が異なります。
`verbosity` は「指定レベル以下の詳細度をすべて記録する」しきい値ですが、`getStringList` は**指定したレベルと完全に一致するもののみ**を返します。
そのため `getStringList(Loglevel.WARN)` は警告だけを取り出せます。
:::

---

## 非同期検索での注意点

`Explainer` はスレッドセーフであり、`asyncSearch` から複数のワーカースレッドが同時に書き込んでもログが失われることはありません。

ただし `asyncSearch` は候補レシピを並列に評価するため、**複数レシピのログが混ざった状態で記録されます**。
各行には必ず `recipe=` が含まれるので、特定のレシピだけを追う場合は絞り込んでください。

```kotlin
val future = Search.asyncSearch(
    crafterId = player.uniqueId,
    view = view,
    explainer = explainer
)

future.thenAccept {
    explainer.getStringList()
        .filter { it.contains("recipe=my_recipe") }
        .forEach { println(it) }
}
```

記録される順序は実際の実行順序をそのまま反映しているため、処理がどの順に進んだかを追うのにも利用できます。

:::caution
`SearchMode.ONLY_FIRST` を使用した場合、最初の合致が見つかった時点で他の検索タスクは中断されます。
中断されたレシピは実際の条件とは無関係に失敗するため、その旨が記録されます。

```
[INFO] [shapeless/flowCheck] recipe=..., matching abandoned: async context interrupted
```

この行が出ているレシピについては、失敗の理由を検証しないでください。
原因を調べる際は `SearchMode.ALL` を使用してください。
:::

---

## 対応している検索メソッド

| メソッド | 対応 |
|----------|------|
| `Search.search` | ○ |
| `Search.asyncSearch` | ○ |
| `PartialSearch.asyncPartialSearch` | ○ |
| `VanillaSearch.search` | ○ |

なお、CustomCrafterAPI 標準のクラフト画面 (`CraftUI`) には `Explainer` を渡す経路がありません。
標準画面の処理はレシピの内容によって変動しない固定の処理であり、そこで問題が発生する場合は利用者側ではなくライブラリ側の不具合であるためです。
レシピの動作を確認する場合は、上記の検索メソッドを直接呼び出してください。
