---
title: GroupRecipe
---

## GroupRecipe とは

`GroupRecipe` は `CRecipeImpl` とは別の `CRecipe` 実装クラスで、**スロットをグループ化して最小配置数を柔軟に制御できる定形レシピ**です。

通常の定形レシピでは各スロットが必須ですが、`GroupRecipe` では複数のスロットを 1 つの `Context` にまとめ、そのグループ内で「最低 N 個のスロットにアイテムが置かれていればよい」という条件を設定できます。

また、スロットに `Material.AIR` を候補として含む `GroupRecipe.Matter` を使用することで、空スロットを許容するアイテム条件を作成できます。

:::note
`GroupRecipe` は `CRecipe.Type.SHAPED` のみサポートします。`SHAPELESS` は使用できません。
:::

---

## コンストラクタ

```kotlin
open class GroupRecipe @JvmOverloads constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    val groups: Set<Context>,
    override val predicates: List<CRecipePredicate>? = listOf(recipePredicate),
    override val results: List<ResultSupplier>? = null,
    override val type: CRecipe.Type = CRecipe.Type.SHAPED
) : CRecipe
```

| パラメータ | デフォルト値 | 概要 |
|------------|-------------|------|
| `name` | — | レシピの識別名 |
| `items` | — | 座標とアイテム条件のマッピング |
| `groups` | — | スロットのグループ化設定 (`Context` の集合) |
| `predicates` | `listOf(recipePredicate)` | `GroupRecipe.recipePredicate` を含む必要がある |
| `results` | `null` | 成果物を生成する `ResultSupplier` のリスト |
| `type` | `SHAPED` | 常に `CRecipe.Type.SHAPED` |

:::caution
`predicates` のデフォルト値は `listOf(GroupRecipe.recipePredicate)` です。
カスタムの `predicates` を追加する場合は、必ず `recipePredicate` を含めてください。
`recipePredicate` を省略するとグループの最小配置数チェックが機能しません。
:::

---

## GroupRecipe.Context

`Context` はスロットのグループ化設定を表すクラスです。

| フィールド | 型 | 概要 |
|------------|-----|------|
| `members` | `Set<CoordinateComponent>` | このグループに属するスロットの座標集合 |
| `min` | `Int` | クラフト時にアイテムが配置されている必要がある最小スロット数 |
| `name` | `String` | グループ名 (デフォルトはランダム UUID) |

### Context.of()

バリデーション付きで `Context` を生成するファクトリメソッドです。

```kotlin
GroupRecipe.Context.of(
    members: Set<CoordinateComponent>,
    min: Int,
    name: String = UUID.randomUUID().toString()
): Context
```

`members` が空の場合や `min` が 0 未満の場合は `IllegalArgumentException` がスローされます。

### Context.default()

単一の座標に対してデフォルト設定 (`min = 1`) の `Context` を生成します。
グループ化が必要ない座標を補完する際に内部的に使われます。

```kotlin
GroupRecipe.Context.default(coordinate: CoordinateComponent): Context
// → members: {coordinate}, min: 1
```

### Context.isValidGroups()

`Context` の集合と `items` が整合しているかを確認するメソッドです。

```kotlin
GroupRecipe.Context.isValidGroups(
    groups: Set<Context>,
    items: Map<CoordinateComponent, CMatter>
): Result<Unit>
```

以下の場合に失敗します:
- `members` が空の `Context` が存在する
- `members` に含まれる座標が `items` のキーに存在しない
- 複数の `Context` に同じ座標が含まれている (重複)
- グループ内の Air 許容 Matter の数が不足している (`members.size - min` 個必要)
- `items` の最小インデックス座標の Matter が `Material.AIR` を候補に含んでいる

---

## GroupRecipe.Matter

`GroupRecipe.Matter` は `CMatter` の特殊な実装で、`Material.AIR` を候補に含めることができます。

通常の `CMatterImpl` は Air を候補に含めることができませんが、`GroupRecipe.Matter` はグループ内のオプションスロットを表現するために Air を許容します。

### Matter.of()

既存の `CMatter` から `Matter` を生成するファクトリメソッドです。

```kotlin
GroupRecipe.Matter.of(
    matter: CMatter,
    includeAir: Boolean = false
): Matter
```

| パラメータ | デフォルト値 | 概要 |
|------------|-------------|------|
| `matter` | — | ベースとなる CMatter |
| `includeAir` | `false` | `Material.AIR` を候補に追加するかどうか |

`matter` が既に `GroupRecipe.Matter` である場合は `IllegalArgumentException` がスローされます。

生成された `Matter` は内部的に `originalChecker` という `CMatterPredicate` を使用し、
配置されたアイテムが Air でない場合は元の `CMatter` の `predicates` チェックに委譲します。

---

## createGroups() ヘルパー

`GroupRecipe.createGroups()` はグループ設定を補完するユーティリティメソッドです。
指定した `missingGroups` に含まれない座標に対して自動的にデフォルトの `Context` を作成して追加します。

```kotlin
GroupRecipe.createGroups(
    items: Map<CoordinateComponent, CMatter>,
    missingGroups: Set<Context>
): Set<Context>
```

内部で `Context.isValidGroups()` を呼び出してバリデーションを行い、
不正な `Context` があれば例外をスローします。

---

## recipePredicate

`GroupRecipe.recipePredicate` は `GroupRecipe` のグループ検査を担う `CRecipePredicate` です。

処理の流れ:
1. `CRecipe` を `GroupRecipe` にキャストできない場合は `true` を返す
2. レシピと入力のオフセット (dx, dy) を計算する
3. 各座標に対応するグループを特定し、配置されているアイテムの数をカウントする
4. すべてのグループで「実際の配置数 ≥ `min`」を確認する

---

## 制約事項

GroupRecipe には以下の制約があります:

1. **最小座標の Matter は Air 不可** — `items` の中で `CoordinateComponent#toIndex()` が最小の座標に対応する `CMatter` は、`Material.AIR` を候補に含めることができません。これはレシピのアンカー (基準点) となるためです。

2. **グループ内の Air 許容 Matter 数** — グループ内でオプション (省略可能) にできるスロット数は `members.size - min` 個です。そのため、グループ内に Air を候補に持つ Matter が少なくとも `members.size - min` 個必要です。

3. **座標の重複禁止** — 1 つの座標は複数の `Context` に属することができません。

4. **SHAPED のみ** — `CRecipe.Type.SHAPELESS` には対応していません。

---

## 使用例

### オプションスロットを持つレシピ

「石 1 個は必須、残り 3 スロットは石が置かれていれば追加で成果物が増える」のような柔軟なレシピを定義する例です。

```kotlin
val stone = CMatterImpl.of(Material.STONE)
// Air を含む Matter (オプションスロット用)
val optionalStone = GroupRecipe.Matter.of(stone, includeAir = true)

// (0,0) は必須 (Air 不可)
// (1,0), (2,0), (0,1) はオプション (0 個以上でよい)
val items = mapOf(
    CoordinateComponent(0, 0) to stone,
    CoordinateComponent(1, 0) to optionalStone,
    CoordinateComponent(2, 0) to optionalStone,
    CoordinateComponent(0, 1) to optionalStone
)

val mandatoryGroup = GroupRecipe.Context.of(
    members = setOf(CoordinateComponent(0, 0)),
    min = 1,
    name = "mandatory"
)

val optionalGroup = GroupRecipe.Context.of(
    members = setOf(
        CoordinateComponent(1, 0),
        CoordinateComponent(2, 0),
        CoordinateComponent(0, 1)
    ),
    min = 0,  // 0 個以上なのでオプション
    name = "optional"
)

val recipe = GroupRecipe(
    name = "flexible-stone",
    items = items,
    groups = setOf(mandatoryGroup, optionalGroup),
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.COBBLESTONE)))
)
```

### createGroups() を使った簡潔な記述

グループ設定が済んでいないスロットを自動補完するために `createGroups()` を使うと便利です。

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val optionalStone = GroupRecipe.Matter.of(stone, includeAir = true)

val items = mapOf(
    CoordinateComponent(0, 0) to stone,           // 必須
    CoordinateComponent(1, 0) to optionalStone,   // オプション
    CoordinateComponent(0, 1) to optionalStone    // オプション
)

// オプションスロットのグループのみ手動で定義し、残りは自動補完
val optionalGroup = GroupRecipe.Context.of(
    members = setOf(CoordinateComponent(1, 0), CoordinateComponent(0, 1)),
    min = 1,  // 2 スロット中 1 個以上
    name = "optional"
)

val groups = GroupRecipe.createGroups(items, setOf(optionalGroup))
// (0,0) に対して自動的にデフォルト Context (min=1) が追加される

val recipe = GroupRecipe(
    name = "stone-optional",
    items = items,
    groups = groups,
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.COBBLESTONE)))
)
```

### カスタム predicates を追加する

`GroupRecipe.recipePredicate` を維持しながら追加の検査を行う場合は、リストに含めます。

```kotlin
val recipe = GroupRecipe(
    name = "op-group-recipe",
    items = items,
    groups = groups,
    predicates = listOf(
        GroupRecipe.recipePredicate,  // 必須: グループチェック
        CRecipePredicate { ctx -> ctx.player?.isOp ?: false }  // OP のみ
    ),
    results = listOf(ResultSupplier.single(ItemStack.of(Material.DIAMOND)))
)
```
