# custom crafter

![logo](https://github.com/Sakaki-Aruka/custom-crafter/assets/99568054/65e00fbd-acd4-45c0-837b-33a16ebd7c44)

# 概要
custom crafter は，既存のアイテムに新しいレシピを追加，カスタムアイテムにレシピを設定することが出来る Spigot 向けプラグインです．  
使用するには `config.yml` 以外にも yaml ファイルを作成する必要があり，それらファイルを格納しておくためのディレクトリも必要とします.  

本プラグインは MIT ライセンスのもとに配布されます.  
ライセンス本文は LICENSE.txt に記されています.  

不具合などありましたら，[製作者のTwitter](https://twitter.com/yt0f1), もしくは Discord (ytshiyugh #5820) までご連絡ください.  

# コマンド
 本プラグインでは `/cc` コマンドを使用することが出来ます.  

## 使用可能コマンド一覧
- `/cc`
- `/cc [recipeName]`
- `/cc -permission [permissionName]`
- `/cc -permission -permissions [targetPlayerName]`
- `/cc -permission -modify [targetPlayerName] [operation] [targetRecipePermission]`
- `/cc -file -make defaultPotion`
- `/cc -give [matter|result] [name]`

### /cc
このコマンドは、すべてのレシピについての情報を表示する。

### /cc [recipeName]
このコマンドは、指定したレシピについての情報を表示する。

### /cc -permission [permissionName]
このコマンドは、指定したレシピ権限のツリー（再帰的に取得した親権限）をコンソールに表示する。

### /cc -permission -permissions [targetPlayerName]
このコマンドは、指定したプレイヤーが保持している全ての権限を表示する。<br>
指定したプレイヤーがオンラインではないか、または権限を1つも保持していない場合は、失敗した旨のメッセージを表示する。<br>

例)
`/cc -permission -permissions ytshiyugh`

### /cc -permission -modify [targetPlayerName] [operation] [targetRecipePermission]
このコマンドは、指定したプレイヤーが保持する権限の状態を変更する。<br>
`operation` には `add` と `remove` を適用することが出来る。<br>
それぞれ、権限の追加と削除を行う。<br>
対象のプレイヤーがオンラインでない場合は、操作に失敗した旨のメッセージを表示する。<br>

例)
`/cc -permissions -modify ytshiyugh add ROOT`

### /cc -file -make defaultPotion
このコマンドは、バニラのマインクラフトで取得することが出来るポーションについての Matter ファイルを作成する。<br>
`config.yml` の `matters` セクションに記載した matters ファイルを格納しているディレクトリ配下に `lingering`, `normal`, `splash` ディレクトリを作成する。<br>
また、その中にそれぞれの Matter ファイルを作成する。


### /cc -give -matter [Matter Name]
指定された Matter をアイテムとしてコマンド実行者に付与する。

### /cc -give -result [Result Name]
指定された Result をアイテムとしてコマンド実行者に付与する。

---
OP権限を持つプレイヤーのみ，上記のコマンドに加えて `/cc -reload` を利用することが出来る。  
`/cc -reload` を実行すると，
- 開かれているすべてのカスタムクラフターの画面を閉じる
- `baseBlock`, `matters`, `results`, `recipes` のファイルを再読み込みして展開する
- プレイヤーにプラグインをリロードしたことを伝える  

という3つの処理を行う.  

プレイヤーにプラグインをリロードしたことを伝えるメッセージは `config.yml` の `notice` セクションに書かれた文字列になります.  
メッセージを変更したい場合は，リスト形式で `notice` セクションを書き直してください.  
空文字列を指定する場合は `""` のみを指定してください.

---
# カスタムクラフターの使い方
## 作成編
- コンフィグファイルで設定した baseBlock を 3*3 で設置し，その中心の1マス上に作業台を設置する.
---
## 使用編
- 作業台をクリックし，カスタムクラフターの画面を開く.  
- スロットにアイテムを配置し，画面右の金床をクリックして成果物を得る.
  - 画面右の金床を左クリックすると成果物を1つだけ作成します．この際，素材となるアイテムの個数がレシピの個数と一致している必要があります．
  - 画面右の金床を右クリックすると，配置された素材から作成することが出来る最大個数の成果物を作成します. この際，素材となるアイテムの個数はレシピの個数と一致している必要はありません．
---

## 注意
- スロットにアイテムを配置した状態でカスタムクラフターの画面を閉じると，プレイヤーの座標にそれらのアイテムをドロップします.  
- 成果物の数量が1スタックを超える場合，プレイヤーの座標にそれらをドロップします．
---

# コンフィグファイルの書き方
[ここ](https://github.com/Sakaki-Aruka/custom-crafter-config/blob/master/config_description.md) をご覧ください。