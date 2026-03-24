---
title: 成果物について
---

## 成果物について

CustomCrafterAPI におけるレシピでは、必ずしも入力されたアイテムに対して返却するアイテムを設定する必要はありません。  
要は、アイテムを返さないレシピを提供することが可能ということです。  

しかしバニラではすべてのレシピがアイテムを返しますし、多くの場面においてアイテムを返すレシピを作成することになると思います。  
このページではそのようなアイテムを返す `ResultSupplier` について記載します。  

## ResultSupplier

`ResultSupplier` はレシピがアイテムを返す仕組みに利用されている、単一の関数を持つ関数型インターフェースです。  
アイテムを作成する部分を自らのプラグインでハンドリングしない限り、 ResultSupplier は仮想スレッド上で非同期的に実行されます。  

Kotlin での定義は以下のようになっています。  

```kotlin
fun interface ResultSupplier {
    fun supply(ctx: Context): List<ItemStack>
}
```

コンテキストを受け取り、アイテムを示す ItemStack のリストを返却する supply のみが定義されています。  
コンテキストは supply が実行される際のプレイヤーのアイテム配置やクリック種別、呼び出し回数、さらに非同期呼び出し時には割り込み処理の有無などの状態を持ちます。  

## コンテキスト

上記の通り、コンテキストは様々な状態を提供します。

| フィールド | 型 | 概要 |
|------------|-----|------|
| `recipe` | `CRecipe` | この ResultSupplier を含むレシピ |
| `relation` | `MappedRelation` | レシピの配置と実際のアイテム配置の対応関係 |
| `mapped` | `Map<CoordinateComponent, ItemStack>` | 実際のアイテム配置 |
| `shiftClicked` | `Boolean` | 一括作成モードであるか (Shift キーが押下されているか) |
| `calledTimes` | `Int` | 作成回数 (一括作成時は作成可能な最大個数) |
| `crafterID` | `UUID` | クラフトを実行したプレイヤーの UUID |
| `isMultipleDisplayCall` | `Boolean` | 全候補一括表示機能からの呼び出しかどうか |
| `asyncContext` | `AsyncContext?` | 非同期実行時用コンテキスト。同期実行時は `null` |

これらの値を利用して ResultSupplier の中で作成するアイテムを決定できます。

### calledTimes の計算

`calledTimes` は、入力されたアイテムのうち `mass = false` な CMatter に対応するものを対象に、
「入力個数 ÷ CMatter の amount」の最小値として計算されます。

たとえばレシピが「石 2 個 + 金インゴット 1 個」を要求し、プレイヤーが「石 64 個 + 金インゴット 32 個」を入力して Shift 一括作成した場合：

- 石: 64 ÷ 2 = 32
- 金インゴット: 32 ÷ 1 = 32
- `calledTimes` = min(32, 32) = **32**

`ResultSupplier.timesSingle` を使うと成果物の個数が `calledTimes` 倍になります。

### isMultipleDisplayCall について

`UseMultipleResultCandidateFeature` が有効な場合、入力されたアイテムに合致する複数のレシピの成果物をプレビューとして表示する機能があります。
この表示のための呼び出し時には `isMultipleDisplayCall = true` となります。

プレビュー呼び出し時にも副作用（データベース書き込みなど）が発生しないよう、
処理内容に応じてこのフラグを確認することを推奨します。

```kotlin
val result = ResultSupplier { ctx ->
    if (ctx.isMultipleDisplayCall) {
        // プレビュー表示のみの場合は副作用を発生させない
        return@ResultSupplier listOf(ItemStack.of(Material.DIAMOND))
    }
    // 実際のクラフト時のみデータベースを更新する
    MyDatabase.recordCraft(ctx.crafterID)
    listOf(ItemStack.of(Material.DIAMOND))
}
```

### 非同期実行時の注意

`asyncContext` が非 `null` の場合、`supply` は仮想スレッド上で実行されています。
非同期スレッドでは BukkitAPI のワールド・エンティティへのアクセスが許可されていないため、
プレイヤーへのアイテム付与や座標取得などは行えません。

また、仮想スレッドは協調的に動作するため強制割り込みができません。
定期的に `asyncContext.isInterrupted()` を確認し、割り込み要請があった場合は処理を打ち切るようにしてください。

```kotlin
val asyncAwareResult = ResultSupplier { ctx ->
    val asyncCtx = ctx.asyncContext
    if (asyncCtx != null) {
        // 非同期実行中に割り込みチェック
        if (asyncCtx.isInterrupted()) {
            return@ResultSupplier emptyList()
        }
        // 非同期では BukkitAPI のワールドアクセス不可
        // ctx.crafterID を使ってデータを取得する
        val data = MyDatabase.fetchData(ctx.crafterID)
        return@ResultSupplier listOf(ItemStack.of(data.material))
    }
    // 同期実行時は通常処理
    listOf(ItemStack.of(Material.DIAMOND))
}
```

## 便利な事前定義オブジェクト

ResultSupplier はとてもシンプルな定義ゆえにどのような処理も記述できますが、入力されたアイテムに対してただ 1 つのアイテムを返却したい場合などには、望む動作のために多くのコードの記述が必要になる場合があります。
このようなことを避け、簡単に ResultSupplier を利用できるように上記のようなシチュエーションに対応した定義済みオブジェクトが提供されています。

`ResultSupplier.single(vararg items: ItemStack)` は、与えられたアイテムをレシピからの呼び出し時にプレイヤーへ与えるだけの ResultSupplier を作成します。
複数個分のアイテムを配置してシフトキーを押し一括作成モードで作成した際でも、 `items` に指定したアイテムの個数を変更せずプレイヤーに与えます。

`ResultSupplier.timesSingle(vararg items: ItemStack)` は、提供するアイテムの個数を変更すること以外は `single` と同じように振る舞います。
一括作成モードで作成した際には、 `items` に指定したアイテムの個数をそれぞれ「作成個数」に変更して提供します。
例として、必要とされる 2 倍のアイテムを配置して一括作成モードで作成すると、 `items` に指定したアイテムの個数が 2 倍になった状態で提供されます。
このオブジェクトの振る舞いは、バニラのアイテム作成時の振る舞いとほとんど同じものと考えてもらって構いません。

```kotlin
// single と timesSingle の使い分け例
val stone = CMatterImpl(
    name = "stone",
    candidate = setOf(Material.STONE),
    amount = 2,  // 石を 2 個要求
    mass = false,
    predicates = null
)

// single: 何個作っても常に 1 個のダイヤモンドを返す
val singleResult = ResultSupplier.single(ItemStack.of(Material.DIAMOND))

// timesSingle: 石 64 個でシフト一括作成すると 32 個のダイヤモンドを返す
val timesResult = ResultSupplier.timesSingle(ItemStack.of(Material.DIAMOND))

val recipe = CRecipeImpl(
    name = "stone-to-diamond",
    items = mapOf(CoordinateComponent(0, 0) to stone),
    results = listOf(timesResult),
    type = CRecipe.Type.SHAPED
)
```

## 完全なカスタム実装例

プレイヤーの UUID を参照して返すアイテムを変える例です。

```kotlin
val customResult = ResultSupplier { ctx ->
    // 特定プレイヤーには特別なアイテムを返す
    val specialPlayer = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
    if (ctx.crafterID == specialPlayer) {
        return@ResultSupplier listOf(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE))
    }
    listOf(ItemStack.of(Material.GOLDEN_APPLE))
}
```

