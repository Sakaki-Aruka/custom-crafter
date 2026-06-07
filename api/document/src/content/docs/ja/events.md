---
title: イベント
---

## CustomCrafterAPI のイベント

CustomCrafterAPI は Bukkit の `Event` を実装した独自のイベントをいくつか発火します。
これらは通常の Bukkit イベントと同じ方法でリッスン・ハンドリングできます。

---

## RegisterCustomRecipeEvent

レシピが `CustomCrafterAPI.registerRecipe()` によって登録されようとするときに発火します。
`Cancellable` を実装しており、キャンセルするとレシピの登録が失敗します。

| プロパティ | 型 | 概要 |
|------------|-----|------|
| `recipes` | `List<CRecipe>` | 登録しようとしているレシピのリスト |

```kotlin
class RecipeRegisterListener : Listener {
    @EventHandler
    fun onRegister(event: RegisterCustomRecipeEvent) {
        event.recipes.forEach { recipe ->
            println("レシピ登録: ${recipe.name}")
        }

        // 特定の名前のレシピ登録をキャンセルする例
        if (event.recipes.any { it.name.startsWith("forbidden-") }) {
            event.isCancelled = true
            println("登録をキャンセルしました。")
        }
    }
}
```

---

## UnregisterCustomRecipeEvent

レシピが `CustomCrafterAPI.unregisterRecipe()` または `CustomCrafterAPI.unregisterAllRecipes()` によって解除されようとするときに発火します。
`Cancellable` を実装しており、キャンセルするとレシピの解除が失敗します。

| プロパティ | 型 | 概要 |
|------------|-----|------|
| `recipes` | `List<CRecipe>` | 解除しようとしているレシピのリスト |

```kotlin
class RecipeUnregisterListener : Listener {
    @EventHandler
    fun onUnregister(event: UnregisterCustomRecipeEvent) {
        event.recipes.forEach { recipe ->
            println("レシピ解除: ${recipe.name}")
        }

        // 特定のレシピは解除させない例
        if (event.recipes.any { it.name == "protected-recipe" }) {
            event.isCancelled = true
        }
    }
}
```

---

## CreateCustomItemEvent

アイテムが作成された (クラフトが実行された) ときに発火します。
このイベントはキャンセル不可です。

| プロパティ | 型 | 概要 |
|------------|-----|------|
| `player` | `Player` | クラフトを実行したプレイヤー |
| `view` | `CraftView` | クラフト時の UI 入力状態 |
| `result` | `Search.SearchResult?` | 検索結果。`null` の場合はクラフトが成立しなかったことを示す |
| `shiftUsed` | `Boolean` | シフトキーを押して一括作成したかどうか (5.0.17 以降) |
| `isAsync` | `Boolean` | 非同期スレッドからの呼び出しかどうか (5.0.17 以降) |

```kotlin
class CreateItemListener : Listener {
    @EventHandler
    fun onCreateItem(event: CreateCustomItemEvent) {
        val result = event.result ?: return

        // 作成されたカスタムレシピの名前をログに出力する
        result.customs().forEach { (recipe, _) ->
            println("${event.player.name} が ${recipe.name} を作成しました。")
        }

        // 一括作成の場合に追加処理を行う例
        if (event.shiftUsed) {
            println("一括作成が使用されました。")
        }
    }
}
```

---

## CustomCrafterAPIPropertiesChangeEvent

`CustomCrafterAPI` のミュータブルなプロパティ (設定値) が変更されたときに発火します。
変更前の値と変更後の値の両方が含まれます。

このイベントはジェネリックで、変更されたプロパティの型 `T` を持ちます。

| プロパティ | 型 | 概要 |
|------------|-----|------|
| `propertyName` | `String` | 変更されたプロパティの名前 |
| `oldValue` | `Property<T>` | 変更前の値 |
| `newValue` | `Property<T>` | 変更後の値 |
| `isAsync` | `Boolean` | 非同期スレッドからの変更かどうか |

`Property<T>` から型安全に値を取り出すには `PropertyKey<T>` を使用します。

```kotlin
val key = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK
val value: Material? = property.getOrNull(key)
```

定義済みの `PropertyKey` は以下のとおりです：

| キー | 対応する型 | 対応するプロパティ |
|------|-----------|----------------|
| `RESULT_GIVE_CANCEL` | `Boolean` | ResultGiveCancel |
| `BASE_BLOCK` | `Material` | BaseBlock |
| `USE_MULTIPLE_RESULT_CANDIDATE_FEATURE` | `Boolean` | UseMultipleResultCandidateFeature |
| `USE_CUSTOM_CRAFT_UI` | `Boolean` | UseCustomCraftUI |
| `BASE_BLOCK_SIDE` | `Int` | BaseBlockSide |
| `CRAFT_UI_DESIGNER` | `CraftUIDesigner` | CraftUIDesigner |

### 実装例: ベースブロックの変更を検知する

```kotlin
class PropertiesChangeListener : Listener {
    @EventHandler
    fun <T> onPropertiesChange(event: CustomCrafterAPIPropertiesChangeEvent<T>) {
        val key = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK

        // プロパティ名で対象のイベントだけ絞り込む
        if (event.propertyName != key.propertyName) return

        val oldBlock: Material = event.oldValue.getOrNull(key) ?: return
        val newBlock: Material = event.newValue.getOrNull(key) ?: return

        println("ベースブロックが ${oldBlock.name} から ${newBlock.name} に変更されました。")
    }
}
```

---

### 実装例: 複数プロパティをまとめて監視する

```kotlin
class AllPropertiesChangeListener : Listener {
    @EventHandler
    fun <T> onPropertiesChange(event: CustomCrafterAPIPropertiesChangeEvent<T>) {
        when (event.propertyName) {
            CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK.propertyName -> {
                val newValue = event.newValue.getOrNull(
                    CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK
                )
                println("ベースブロック変更: $newValue")
            }
            CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI.propertyName -> {
                val newValue = event.newValue.getOrNull(
                    CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI
                )
                println("カスタム UI 有効化: $newValue")
            }
        }
    }
}
```

---

## CraftInputInterruptEvent

クラフト処理（レシピ検索または成果物生成）が進行中に、プレイヤーが入力スロットを操作したり CraftUI を閉じたりして、処理が中断されたときに発火します。
このイベントはキャンセル不可です。

| プロパティ | 型 | 概要 |
|------------|-----|------|
| `interrupter` | `Player` | 中断を引き起こしたプレイヤー |
| `isAsync` | `Boolean` | 非同期スレッドから発火されたかどうか |

```kotlin
class CraftInputInterruptListener : Listener {
    @EventHandler
    fun onInterrupt(event: CraftInputInterruptEvent) {
        println("${event.interrupter.name} が進行中のクラフトを中断しました。")
    }
}
```

---

## PreventDoubleCraftEvent

クラフト処理（レシピ検索または成果物生成）がすでに実行中の状態で、プレイヤーが新たなクラフトを開始しようとしてブロックされたときに発火します。
このイベントはキャンセル不可です。

| プロパティ | 型 | 概要 |
|------------|-----|------|
| `player` | `Player` | 二重クラフトを試みたプレイヤー |
| `isAsync` | `Boolean` | 非同期スレッドから発火されたかどうか |

```kotlin
class PreventDoubleCraftListener : Listener {
    @EventHandler
    fun onPrevent(event: PreventDoubleCraftEvent) {
        event.player.sendMessage("現在のクラフトが完了するまでお待ちください。")
    }
}
```

---

## ResultItemGiveFailEvent

CustomCrafterAPI がプレイヤーへの成果物アイテムの付与に 1 件以上失敗したとき（例: インベントリが満杯のとき）に発火します。
このイベントはキャンセル不可です。

`getResultsIfNotObtained()` で未付与アイテムを取得して、自前の配布処理を行えます。
一度取得した後は `getResultsIfNotObtained()` が `null` を返すようになります。

| プロパティ / メソッド | 型 | 概要 |
|------------|-----|------|
| `usedSupplierContext` | `ResultSupplier.Context?` | アイテムを生成した `ResultSupplier` のコンテキスト。取得できない場合は `null` |
| `getResultsIfNotObtained()` | `List<ItemStack>?` | 未配布アイテムを返しつつ取得済みとしてマークする。すでに取得済みの場合は `null` |
| `isResultObtained()` | `Boolean` | アイテムがすでに取得済みかどうかを返す |

```kotlin
class ResultGiveFailListener : Listener {
    @EventHandler
    fun onResultGiveFail(event: ResultItemGiveFailEvent) {
        val items: List<ItemStack> = event.getResultsIfNotObtained() ?: return

        // フォールバック: プレイヤーの場所にアイテムをドロップする
        val player = event.usedSupplierContext?.crafterID
            ?.let { Bukkit.getPlayer(it) } ?: return
        items.forEach { player.world.dropItemNaturally(player.location, it) }
    }
}
```
