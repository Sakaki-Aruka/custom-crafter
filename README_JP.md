# 💎 custom crafter - 強力なカスタムレシピ作成プラグイン＆ライブラリ

custom crafter は、**PaperMCサーバー専用**のカスタムレシピ提供プラグインであり、同時に強力な**カスタムレシピ作成ライブラリ (API)** としても機能します。

あなたのプラグイン内でカスタムレシピを自由に定義し、ゲームに新しい要素を追加できます。

---

## 🚀 主な特徴 (Features)

* **バニラアイテムへのカスタムレシピ設定:** 既存のMinecraftアイテムの新しいレシピを簡単に追加できます。
* **オリジナルアイテムのクラフト対応:** あなたのプラグインで作成した**カスタムアイテム**を結果や素材として使用できます。
* **高度な素材管理機能:** レシピの素材となるアイテムの管理用コマンドを提供します。（サーバー管理者向け）
* **プレイヤーへのシームレスな提供:** 定義したカスタムレシピをプレイヤーのクラフト環境に自動で組み込みます。
* **(互換性):** バニラのクラフトもそのまま利用可能です。

---

## 🎥 デモ映像

<details><summary>クラフトのデモ映像</summary>

1. 基礎ブロックを設置 (`GOLD_BLOCK`).
2. `infinityIronBlockCore` レシピを利用 [(レシピが定義されたファイルへジャンプ)](./demo/src/main/kotlin/online/aruka/demo/register/ShapedRecipeProvider.kt).
3. `infinityIronBlock` レシピを利用して鉄ブロックを圧縮 [(レシピが定義されたファイルへジャンプ)](./demo/src/main/kotlin/online/aruka/demo/register/ShapedRecipeProvider.kt).
4. 同じレシピを利用 (`infinityIronBlock`)
5. 同じレシピを利用 (`infinityIronBlock`)
6. `infinityIronBlockExtract` レシピを利用して無限鉄ブロックを解凍 [(レシピが定義されたファイルへジャンプ)](./demo/src/main/kotlin/online/aruka/demo/register/ShapelessRecipeProvider.kt).

![](./resources/crafting-demo.gif)

</details>  

上のビデオで利用しているレシピはデモプラグインに含まれています。  
デモプラグインは、このリポジトリをローカルへクローンした後に以下のコマンドを実行することで作成できます。  

`mvn -pl demo package`  

サーバーの plugins ディレクトリに配置可能な jar ファイルは `demo/target` ディレクトリに出力されます。  

---

## 🛠️ 対応環境とバージョン

| Custom_Crafter Version   | Paper Version        |
|:-------------------------|:---------------------|
| **5.0.13 ~ 5.0.17 (最新)** | **1.21.4 ~ 1.21.11** |
| 5.0.0 ~ 5.0.11           | 1.21.3               |
| 4.3 (レガシー)               | 1.21.3               |
| 4.2 (レガシー)               | 1.20.1 ~ 1.20.4      |

> **⚠️ 警告:**
> custom crafter は **Spigot/Bukkit サーバーでの動作をサポートしていません**。必ず **PaperMC** またはそのフォークで実行してください。

---

## 💻 APIを利用する - プラグインへの組み込み

custom crafter はバージョン 5.0.0 以降、プラグインとしてだけでなく、カスタムレシピを定義・登録するための**API**として設計されています。

あなたのプラグインからカスタムレシピを自由に作成し、CustomCrafterシステムに登録できます。

### ドキュメント

より詳細な情報や、すべてのクラス・メソッドについてはこちらを参照してください。

* [KDoc (Kotlin)](https://sakaki-aruka.github.io/custom-crafter/)
* [Wiki (GitHub)](https://github.com/Sakaki-Aruka/custom-crafter/wiki/Intro)

ドキュメントをビルドしてローカルで閲覧することもできます。  
* KDoc Style: `mvn -pl api dokka:dokka`
* JavaDoC Style: `mvn -pl api dokka:javadoc`

### 依存関係の設定

APIを利用する際は、プラグイン実行時にはCustomCrafterプラグインが存在することを前提とするため、実行環境から提供される設定にしてください。

* Maven: `provided`
* Gradle: `compileOnly`

最新バージョン: 5.0.17 [Maven Central (versions)](https://central.sonatype.com/artifact/io.github.sakaki-aruka/custom-crafter-api/versions)

<details><summary>Maven Configuration Example</summary>

From Maven Central

```xml
<dependency>
    <groupId>io.github.sakaki-aruka</groupId>
    <artifactId>custom-crafter-api</artifactId>
    <version>5.0.17</version>
    <scope>provided</scope>
</dependency>
```

</details>

<details><summary>Gradle (Groovy) Configuration Example</summary>

```groovy
dependencies {
   compileOnly 'io.github.sakaki-aruka:custom-crafter-api:5.0.17'
}
```

</details>

<details><summary>Gradle (Kotlin DSL) Configuration Example</summary>

```Kotlin
dependencies {
    compileOnly("io.github.sakaki-aruka:custom-crafter-api:5.0.17")
}
```

</details>

---

## ⚙️ サーバーへの導入ステップ

CustomCrafterはKotlinで書かれており、実行には前提となるライブラリが必要です。

1.  **CustomCrafterプラグインのダウンロード**
    1.  [CustomCrafterAPI 本体をダウンロード (GitHubリリースページ)](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest)
    2.  **MCKotlin-Paper プラグイン (Kotlin実行環境) をダウンロード**
        * [MCKotlin-Paper (GitHubリリースページ)](https://github.com/4drian3d/MCKotlin/releases) から、ご自身のサーバーバージョンに合ったファイルをダウンロードしてください。
    3.  ダウンロードしたファイルをすべて `plugins` ディレクトリに入れます。
2.  **サーバーの起動/リロード**
3.  **カスタムクラフト台の設置 (ベースブロック)**
    * CustomCrafterのレシピは、通常の作業台ブロックだけでは動作しません。
    * **通常の作業台の真下 3x3 の範囲**にベースブロックを敷き詰めることで、カスタムクラフト機能が有効になります。
    * デフォルトのベースブロックは **`GOLD_BLOCK` (金ブロック)** です。

---

## 🧑‍💻 コードサンプルとAPIの利用

CustomCrafterAPIを使って、あなたのプラグインでカスタムレシピを定義する基本的な流れを解説します。

### 互換性チェック（安全な起動のために）

あなたのプラグインが依存しているCustomCrafterAPIのバージョンと、サーバーにデプロイされているバージョンが完全に互換性があるかを確認するコード例です。

```kotlin
class YourPlugin: JavaPlugin() {
    // 依存するAPIのバージョンを定数として定義
    const val DEPEND_CCAPI_VERSION = "5.0.17"
    
    @Override
    fun onEnable() {
        // 互換性がない場合はプラグインを無効化し、エラーを防ぐ
        if (!CustomCrafterAPI.hasFullCompatibility(DEPEND_CCAPI_VERSION)) { 
            Bukkit.pluginManager.disablePlugin(this)
            return
        }
    }
}
```

---

### 📝 レシピの定義

カスタムレシピは、主に以下の3つの要素で構成されます。

1.  **カスタム素材** (`CMatter`): クラフトに必要な「材料」の条件を定義します。
2.  **クラフト結果** (`ResultSupplier`): クラフト成功時に「結果」として何が生成されるかを定義します。
3.  **レシピ本体** (`CRecipe`): 素材と結果、クラフトの形状（定形/不定形）をグループ化して登録します。

#### 1. カスタム素材（CMatter）の作成

レシピの素材となるアイテム（`CMatter`）を定義します。`CMatter`は、**どのアイテムが**、**いくつ**、**どの位置**にあればクラフト可能かを決定します。

* `CMatterImpl` または `CMatter` インターフェースの実装クラスを利用します。

**例：石 または 丸石を1個、素材として使用**

```Kotlin
// In Kotlin
val matter: CMatter = CMatterImpl(
    name = "test-matter",
    candidate = setOf(Material.STONE, Material.COBBLESTONE), // 石、または丸石が使える
    amount = 1, // 必要な個数
    mass = false, // true: レシピ中に 1 つだけ存在していれば良いとマークするか (通常はfalse)
    predicates = null // 追加のNBTなどの条件 (通常はnull)
)
```

**ショートハンド (`of`) の利用例**

`candidate` に複数の `Material` を指定するシンプルなケースでは、以下の省略記法が便利です。

```Kotlin
// In Kotlin
val matter: CMatter = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
```
この例は、**「石」** または **「丸石」** のどちらかがあれば素材として機能する**柔軟な素材**を作成します。

---

#### 2. クラフト結果（ResultSupplier）の作成

レシピが完成したときに、プレイヤーに渡されるアイテムや実行される処理を定義します。

* 複雑な処理が不要な場合は、結果アイテムの定義をスキップし、代わりに**コマンド実行**を結果とすることも可能です。

**ResultSupplier** は、クラフト時の様々な条件（`Config`）を受け取り、最終的な出力アイテム (`List<ItemStack>`) を決定する **関数（サプライヤー）** です。

**例：複雑な条件に基づいて結果を返す**

```Kotlin
// In Kotlin
val supplier = ResultSupplier { config ->
    // 'config' にはクラフト時の環境（プレイヤー、クラフト台など）に関する情報が含まれます。
    
    // ここで ItemStack のリストを作成し、返す処理を記述します。
    emptyList<ItemStack>() // 例として、何も返さない
}
```

**簡略化されたヘルパーメソッドの利用**

複雑な処理が不要な場合は、CustomCrafterが提供するヘルパーメソッドが便利です。

```Kotlin
// In Kotlin
// 常に指定した ItemStack (例: 石1個) を返す
val supplier = ResultSupplier.single(ItemStack(Material.STONE))

// プレイヤーが Shift+クリックで複数回クラフトした場合に、回数分だけ ItemStack を増量して返す (スマート！)
val supplier2 = ResultSupplier.timesSingle(ItemStack(Material.STONE))
```
`ResultSupplier#timesSingle` は、プレイヤーが一度に大量にクラフトする際の動作を自動で制御します。

---

#### 3. レシピ本体（CRecipe）の作成と登録

カスタム素材とクラフト結果を組み合わせ、CustomCrafterシステムに**レシピとして登録**するための核心的なコンポーネントです。

* 通常はデフォルト実装の `CRecipeImpl` を使用します。

**例：シンプルな整形レシピの定義**

```Kotlin
// In Kotlin
val recipe: CRecipe = CRecipeImpl(
    name = "test-recipe",
    items = mapOf(CoordinateComponent(0, 0) to matter), // 'matter' は 1.で作った CMatter
    containers = null, // 追加条件 (権限など)。不要なら null
    results = setOf(ResultSupplier.timesSingle(Material.STONE)), // 2.で作った ResultSupplier のセット
    type = CRecipeType.NORMAL // 整形レシピ (Shaped Recipe)
)
```

* `items`: クラフトグリッド内の座標 (`CoordinateComponent(x, y)`) と、そこに必要な素材 (`CMatter`) のマップ。
* `type`: レシピの形状を指定します。
    * `CRecipeType.NORMAL`: **整形レシピ** (Shaped Recipe)。`items` の座標が重要になります。
    * `CRecipeType.AMORPHOUS`: **不定形レシピ** (Shapeless Recipe)。`items` の座標値は任意で構いません。😊

---

## 🔑 ライセンス

MIT License

Copyright (c) 2023 - 2025 Sakaki-Aruka

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

---

## 🙏 Acknowledgement

## choco-solver

This product includes software developed by the IMT Atlantique.