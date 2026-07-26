# custom crafter

custom crafter は PaperMC サーバー向けのカスタムレシピプラグインであり、同時にプラグインからカスタムレシピを定義するためのライブラリ (API) でもある。

自分のプラグインからカスタムレシピを自由に定義し、ゲームに新しい要素を追加できる。

[English README](README.md)

---

## 特徴

- **バニラアイテムへのレシピ追加** — 既存の Minecraft アイテムに新しいレシピを追加できる。
- **カスタムアイテム対応** — 自作プラグインで作成したアイテムを素材・結果として利用できる。
- **素材管理コマンド** — レシピ素材の管理用コマンドをサーバー管理者向けに提供する。
- **プレイヤーへの自動反映** — 登録したレシピは追加設定なしにプレイヤーのクラフト環境に反映される。
- バニラのクラフトは変更されずそのまま動作する。

---

## デモ

<details><summary>クラフトのデモ映像</summary>

1. 基礎ブロック (`GOLD_BLOCK`) を設置する。
2. `infinityIronBlockCore` をクラフトする [(レシピ定義)](./demo/src/main/kotlin/online/aruka/custom_crafter/demo/register/ShapedRecipeProvider.kt)。
3. `infinityIronBlock` で鉄ブロックを圧縮する [(レシピ定義)](./demo/src/main/kotlin/online/aruka/custom_crafter/demo/register/ShapedRecipeProvider.kt)。
4. 同じレシピをもう一度使う。
5. さらにもう一度使う。
6. `infinityIronBlockExtract` で無限鉄ブロックを解凍する [(レシピ定義)](./demo/src/main/kotlin/online/aruka/custom_crafter/demo/register/ShapelessRecipeProvider.kt)。

![](./resources/crafting-demo.gif)

</details>

このリポジトリをクローンし、以下のコマンドを実行すると、上記のレシピを含むデモプラグインをビルドできる。

```
mvn -pl demo package
```

生成された jar は `demo/target` に出力され、そのままサーバーの `plugins` ディレクトリに配置できる。

---

## 対応環境

| Custom_Crafter Version               | Paper Version            |
|:--------------------------------------|:--------------------------|
| **5.3.0 (最新)**                       | 1.21.4 ~ 1.21.11, 26.1.x |
| 5.0.13 ~ 5.0.21, 5.1.0, 5.2.0, 5.2.1  | 1.21.4 ~ 1.21.11, 26.1.x |
| 5.0.0 ~ 5.0.11                        | 1.21.3                    |
| 4.3 (レガシー)                          | 1.21.3                    |
| 4.2 (レガシー)                          | 1.20.1 ~ 1.20.4           |

対応する Paper の下限バージョンは更新のたびに変わることは少ないため、最新版の利用を推奨する。

**注意:** custom crafter は Spigot/Bukkit では動作しない。PaperMC またはその fork で実行すること。

---

## API の利用

custom crafter はバージョン 5.0.0 以降、プラグインからカスタムレシピを定義・登録するための API としても機能する。

### ドキュメント

- [KDoc (Kotlin)](https://sakaki-aruka.github.io/custom-crafter/kdoc/)
- [Javadoc (Java)](https://sakaki-aruka.github.io/custom-crafter/javadoc/)
- [ドキュメントサイト](https://sakaki-aruka.github.io/custom-crafter/document/ja/getting-started/)

ローカルでドキュメントをビルドする場合:

```
mvn -pl api dokka:dokka      # KDoc
mvn -pl api dokka:javadoc    # Javadoc
```

### 依存関係

CustomCrafterAPI に依存するプラグインは、`plugin.yml` の `depend` に `Custom_Crafter` を追加する必要がある。

```yaml
depend:
  - "Custom_Crafter"
```

---

最新バージョン: 5.3.0 ([Maven Central](https://central.sonatype.com/artifact/io.github.sakaki-aruka/custom-crafter-api/versions))

CustomCrafter プラグインは実行時に存在することを前提とするため、依存関係のスコープはコンパイル時のみに設定する。Kotlin でプラグインを書く場合は kotlin-stdlib も同様にコンパイル時のみとする。

ビルドツールごとの「コンパイル時のみ」の設定名:
- Maven: `provided`
- Gradle: `compileOnly`

<details><summary>Maven</summary>

```xml
<!-- CustomCrafterAPI -->
<dependency>
    <groupId>io.github.sakaki-aruka</groupId>
    <artifactId>custom-crafter-api</artifactId>
    <version>5.3.0</version>
    <scope>provided</scope>
</dependency>

<!-- kotlin-stdlib -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>2.3.0</version>
    <scope>provided</scope>
</dependency>
```

</details>

<details><summary>Gradle (Groovy)</summary>

```groovy
dependencies {
    compileOnly 'io.github.sakaki-aruka:custom-crafter-api:5.3.0'
    compileOnly 'org.jetbrains.kotlin:kotlin-stdlib:2.3.0' // Kotlin 利用時のみ
}
```

</details>

<details><summary>Gradle (Kotlin DSL)</summary>

```kotlin
dependencies {
    compileOnly("io.github.sakaki-aruka:custom-crafter-api:5.3.0")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0") // Kotlin 利用時のみ
}
```

</details>

---

## サーバーへの導入

custom crafter は Kotlin で書かれている。

1. [プラグイン本体をダウンロード](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest) し、ダウンロードしたファイルをすべて `plugins` ディレクトリに配置する。
2. サーバーを起動、またはリロードする。
3. 通常の作業台の真下 3x3 の範囲に基礎ブロックを設置する — 通常の作業台だけではカスタムレシピは有効にならない。デフォルトの基礎ブロックは `GOLD_BLOCK`。

---

## コードサンプル

### 互換性チェック

プラグインが依存する CustomCrafterAPI のバージョンと、サーバー上で動作しているバージョンの互換性を確認する例:

```kotlin
class YourPlugin : JavaPlugin() {
    val dependVersion = Triple(5, 3, 0)

    @Override
    fun onEnable() {
        if (CustomCrafterAPI.MAJOR_VERSION == dependVersion.first
            && CustomCrafterAPI.MINOR_VERSION >= dependVersion.second) {
            return
        }
        Bukkit.pluginManager.disablePlugin(this)
    }
}
```

---

### レシピの定義

カスタムレシピは以下の3要素からなる。

1. **素材** (`CMatter`) — クラフトに必要な入力条件。
2. **結果** (`ResultSupplier`) — クラフト成功時に生成されるもの。
3. **レシピ本体** (`CRecipe`) — 素材・結果・形状(定形/不定形)をまとめて登録する。

#### 1. 素材 (`CMatter`)

`CMatter` は、どのアイテムが、いくつ、どの位置にあればクラフトできるかを定義する。`CMatterImpl` または `CMatter` の実装クラスを使う。

石または丸石を1個要求する例:

```kotlin
val matter: CMatter = CMatterImpl(
    name = "test-matter",
    candidate = setOf(Material.STONE, Material.COBBLESTONE),
    amount = 1,
    anyAmount = false, // true: スタック全体で個数を満たしてもよいとする
    predicates = null  // 追加のNBT等の条件
)
```

候補素材が単純な場合、`of` の方が短く書ける。

```kotlin
val matter: CMatter = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
```

#### 2. 結果 (`ResultSupplier`)

`ResultSupplier` はクラフト時の情報 (`Config`) を受け取り、出力アイテムを返す。アイテムでの結果が不要な場合はコマンド実行に置き換えられる。

```kotlin
val supplier = ResultSupplier { config ->
    // config にはプレイヤーやクラフト台などの情報が含まれる
    emptyList<ItemStack>()
}
```

一般的なケースはヘルパーメソッドでカバーできる。

```kotlin
// 常に石1個を返す
val supplier = ResultSupplier.single(ItemStack.of(Material.STONE))

// 一度のクラフト(Shift+クリック等)での個数に応じて出力をスケールする
val supplier2 = ResultSupplier.timesSingle(ItemStack.of(Material.STONE))
```

#### 3. レシピ本体 (`CRecipe`)

`CRecipeImpl` が素材と結果を組み合わせ、登録可能なレシピにする。

```kotlin
val recipe: CRecipe = CRecipeImpl(
    name = "test-recipe",
    items = mapOf(CoordinateComponent(0, 0) to matter),
    containers = null, // 追加条件(権限など)
    results = setOf(ResultSupplier.timesSingle(Material.STONE)),
    type = CRecipe.Type.NORMAL
)
```

- `items`: グリッド座標 (`CoordinateComponent(x, y)`) と、そこに必要な `CMatter` の対応。
- `type`: `CRecipe.Type.NORMAL` は座標が意味を持つ定形レシピ、`CRecipe.Type.AMORPHOUS` は座標の値が任意でよい不定形レシピ。

---

## ライセンス

MIT License

Copyright (c) 2023 - 2026 Sakaki-Aruka

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
