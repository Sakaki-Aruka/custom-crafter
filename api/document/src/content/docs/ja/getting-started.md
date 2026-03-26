---
title: CustomCrafterAPI 入門
---

## CustomCrafterAPI とは？
CustomCrafterAPI は 1 x 1 から 6 x 6 サイズのレシピを提供できる基盤を実装したプラグインです。  
Minecraft 標準のレシピのようにアイテムの種類だけを参照する単純なものから、入力されたアイテムが持つデータやプレイヤーが持つデータを活用したもの、外部のデータベースや API を参照して作成可能なアイテムを決定するようなレシピを提供することができます。  

## Acknowledgement
This product includes software developed by the IMT Atlantique.

(このソフトウェアは IMT Atlantique によって作成された Choco Solver を含んでいます。)

## Important notice
CustomCrafterAPI はバージョン 5.X.X がアルファ版、 6.0.0 以降が正式版として扱われます。  
5.X.X では 1 つのバージョンアップでクラスや関数の使用が大幅に変更される可能性があります。  

CustomCrafterAPI のレシピを実装したプラグインを利用する場合は、依存関係として利用しているバージョンとサーバーに導入されている CustomCrafterAPI のバージョンに互換性があるかを確認するようにしてください。  

## インストール
CustomCraterAPI はプラグインの依存関係に追加するだけでは利用することはできず、サーバーのプラグインディレクトリとプラグインの依存関係の両方として設定されることによってはじめて正しく動作します。  

なお、 CustomCrafterAPI 単体ではクラフト UI やレシピ登録システムなど最低限の要素のみの提供となるため、独自のレシピを提供する場合はプラグインを作成することが必須となります。  

CustomCrafterAPI 単体で提供される機能は以下のとおりになります。  
- CustomCrafterAPI のクラフト UI からバニラのレシピをクラフトする機能
- 適切な条件下で作業台をクリックしたときに専用画面を開く機能 (デフォルトでは有効。権限設定で無効化可能)
- CustomCrafterAPI の各種設定値を変更可能なコマンド (デフォルトでは管理者権限が必要)
- CustomCrafterAPI のクラフト UI を開くコマンド (デフォルトでは無効。権限設定で有効化可能)

### プラグインとして
プラグインとして利用できる jar ファイルは [Modrinth](https://modrinth.com/plugin/custom-crafter-api/versions) や [GitHub のリリース](https://github.com/Sakaki-Aruka/custom-crafter/releases)から取得することができます。  
どのバージョンでも対応している Minecraft バージョンは変わらないので、最新版を利用することを推奨します。  

### ライブラリとして

CustomCrafterAPI は MIT ライセンスのもと提供されています。  

ライブラリとしての CustomCrafterAPI は [Maven Central](https://central.sonatype.com/artifact/io.github.sakaki-aruka/custom-crafter-api) から依存関係としてプラグインに組み込むことができます。  

CustomCrafterAPI はレシピの登録・解除を 1 箇所で管理しなければいけない都合上、実行時に依存関係が提供される形を取ります。  
そのため依存関係のスコープを適切に設定する必要があります。  
Maven では `provided`, Gradle では `compileOnly` が適切なスコープとなります。  

CustomCrafterAPI を利用するプラグインを Kotlin で実装する場合には、 Kotlin 標準ライブラリ (`kotlin-stdlib`) も実行時に依存関係が提供される形を取らなければいけません。  
こちらでも Maven では `provided`, Gradle では `compileOnly` のスコープで利用してください。

:::note
CustomCrafterAPI の大部分が Kotlin で実装されており、サーバー起動時に kotlin-stdlib を共有のライブラリとしてロードするようにしているため、実行時に依存関係が提供される形を取ります。  
:::

## レシピについて

CustomCrafterAPI における「レシピ」とは特筆のない限り Minecraft オリジナルのレシピとは別の `CRecipe` インターフェースやその実装クラスのことを指します。  
レシピをユーザーに提供するためにはそれを構成するパーツを定義し、 CustomCrafterAPI に登録する必要があります。  
主に定義しなければいけないパーツは以下の 4 つになります。
- 素材
- 成果物 (レシピがマッチしたときにプレイヤーへ渡されるアイテム)
- アイテムの配置
- アイテム配置のタイプ (定形もしくは不定形)

上記の要素に加えて、アイテム作成時に実行される関数などの要素が用意されています。  

## 要素の実装
レシピとそれに必要な素材については、それぞれ CRecipe と CMatter というインターフェースが存在します。  
また、それらを実装した CRecipeImpl, CMatterImpl クラスが存在します。  

:::note
CustomCrafterAPI では基本的に名前が `Impl` で終わるクラスがインターフェースの標準実装となります。  
(例) `CRecipeImpl`, `CMatterImpl`
:::

それらを利用することで基本的なアイテムを扱うレシピを作成することができます。  
更にインターフェースを継承し独自のフィールドを持ったクラスを作成することでより高度な処理を実装することも可能です。  
成果物とアイテムの配置に関してはそれぞれ ResultSupplier と CoordinateComponent というインターフェース・クラスが用意されているので、それらを利用して組み立てることになります。  

## 追加要素の実装
上記のセクションで紹介したとおり、 CustomCrafterAPI にはアイテム作成時などに、高度な処理を行うために利用可能な仕組みが用意されています。
それらを利用することでレシピの検索に細かい条件を設けたり、クラフト画面をプレイヤーごとにカスタムすることが可能です。

代表的な追加要素は以下のとおりです。
- [`CMatterPredicate`](/ja/recipe/matter/#predicates) — 個別スロットのアイテムに対して任意の検査ロジックを追加できる
- [`CRecipePredicate`](/ja/recipe/predicate/) — レシピ全体を対象にプレイヤー情報などを含めた検査を追加できる
- [`CraftUIDesigner`](/ja/extra/craftui-designer/) — クラフト UI のタイトルやスロットレイアウトをカスタマイズできる

## イベント
CustomCrafterAPI はレシピ以外に、アイテムを作成したことを通知するものなどいくつかのイベントを発行します。
これらは Bukkit の Event を実装するクラスであるため、 Minecraft 標準のイベントを扱うイベントリスナーと同じようにリッスン・ハンドリングすることができます。

詳細は[イベントのページ](/ja/events/)を参照してください。
