---
title: AdjacentRecipe
---

## AdjacentRecipe とは

`AdjacentRecipe` は「定形レシピほどしっかり配置を決めたいわけではないが、不定形レシピほどバラバラの配置でもない」といった曖昧な配置検索に対応します。  
具体的には
- `CROSS 制限`: 配置されるアイテムはそれぞれ上下左右の 4 スロットのいずれかに隣り合うアイテムを持たなければならない
- `DIAGONAL 制限`: 配置されるアイテムはそれぞれ斜め 4 スロットのいずれかに近接したアイテムを持たなければならない
- `BOTH 制限`: `CROSS` + `DIAGONAL` となった制限 

という条件を持ちます。  
このレシピは CRecipe.Type.SHAPELESS のみを提供します。

---

## 静的メソッド

### checker(relationType: AdjacentRecipe.RelationType): CRecipePredicate

AdjacentRecipe の中核を成す判定機構を返します。  
制限を指定することで検索時に実行される処理を取得できます。  
通常はインスタンスの初期化時に自動で追加されるため、明示的に呼び出す必要はありません。  

---

## インスタンスの作成

```kotlin
val recipe = AdjacentRecipe(
    name   = "my_recipe",
    matters = listOf(
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
    ),
    relationType     = AdjacentRecipe.RelationType.CROSS,  // 省略時は BOTH
    customPredicates = null,                               // 追加条件がない場合は省略可
    results          = null,
)
```

`matters` に渡した数のアイテムが不定形でグリッドに配置されます。  
`relationType` を切り替えることで許容する隣接パターンを制御できます。

---

## 使用例

以下の図では `#` が配置アイテム、`_` が空スロットを表します（6×6 グリッドの左上を抜粋）。

---

### CROSS 制限

**「斜め方向に隣接するアイテムが存在してはならない」** 制限です。  
あるアイテムから見て斜め 4 方向（↖ ↗ ↙ ↘）のいずれかに別のアイテムがある場合、そのアイテムはレシピにマッチしません。

#### マッチする配置

```kotlin
val recipe = AdjacentRecipe(
    name        = "cross_line",
    matters     = listOf(
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
    ),
    relationType = AdjacentRecipe.RelationType.CROSS,
)
```

```
# # # _    ← 横一列。各アイテムは左右方向のみで隣接しており、斜め隣接なし → マッチ
_ _ _ _
_ _ _ _
```

```
# _ _ _
# _ _ _    ← 縦一列。各アイテムは上下方向のみで隣接 → マッチ
# _ _ _
```

#### マッチしない配置

```
# _ _ _    ← (0,0) と (1,1) は斜め隣接
# # _ _    ← L 字の角で対角関係が生まれる → CROSS では禁止 → マッチしない
_ _ _ _
```

```
# _ _ _    ← (0,0) と (1,1) のみの 2 個配置でも同様
_ # _ _    → マッチしない
_ _ _ _
```

---

### DIAGONAL 制限

**「上下左右方向（クロス）に隣接するアイテムが存在してはならない」** 制限です。  
あるアイテムから見て上下左右（↑ ↓ ← →）のいずれかに別のアイテムがある場合、マッチしません。

#### マッチする配置

```kotlin
val recipe = AdjacentRecipe(
    name        = "diagonal_line",
    matters     = listOf(
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
    ),
    relationType = AdjacentRecipe.RelationType.DIAGONAL,
)
```

```
# _ _ _    ← 右下斜め一列。各アイテムは斜め方向のみで隣接 → マッチ
_ # _ _
_ _ # _
```

```
_ _ # _    ← 左下斜め一列でも同様 → マッチ
_ # _ _
# _ _ _
```

#### マッチしない配置

```
# # _ _    ← (0,0) と (1,0) は左右方向（クロス）で隣接 → DIAGONAL では禁止 → マッチしない
_ _ _ _
_ _ _ _
```

```
# _ _ _    ← (0,0) と (1,1) は斜め隣接（OK）だが、(1,1) と (2,1) はクロス隣接
_ # # _    → マッチしない
_ _ _ _
```

---

### BOTH 制限

**全 8 方向が許可** されるため、隣接方向に制限はありません。  
ただし、各アイテムは少なくとも 1 つの隣接アイテム（距離 1 以内）を持つ必要があります。距離が 2 以上離れた孤立した配置はマッチしません。

#### マッチする配置

```kotlin
val recipe = AdjacentRecipe(
    name        = "both_example",
    matters     = listOf(
        CMatterImpl.of(Material.STONE),
        CMatterImpl.of(Material.STONE),
    ),
    relationType = AdjacentRecipe.RelationType.BOTH,
)
```

```
# # _ _    ← クロス方向 → マッチ
_ _ _ _
```

```
# _ _ _    ← 斜め方向 → マッチ
_ # _ _
```

---

## 配置可能な最大数

6×6 グリッドにおける制限ごとの配置可能最大数は次のとおりです。

| 制限 | 最大数 | 最大配置のパターン例 |
|------|--------|----------------------|
| CROSS | 18 | 偶数列（x = 0, 2, 4）をすべての行で埋める |
| DIAGONAL | 18 | x・y がともに偶数または両方奇数のセルを埋める |
| BOTH | 36 | 制限なし（全セル配置可能） |

この上限を超えた `matters` を指定して `isValidRecipe()` を呼び出すと、配置パターンの参考図とともにエラーが返されます。
