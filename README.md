# custom crafter
# 概要
custom crafter は，既存のアイテムに新しいレシピを追加，カスタムアイテムにレシピを設定することが出来る Spigot 向けプラグインです．  
使用するには `config.yml` 以外にも yaml ファイルを作成する必要があり，それらファイルを格納しておくためのディレクトリも必要とします.  

本プラグインは MIT ライセンスのもとに配布されます.  
ライセンス本文は LICENSE.txt に記されています.  

不具合などありましたら，[製作者のTwitter](https://twitter.com/yt0f1), もしくは Discord (ytshiyugh #5820) までご連絡ください.  

# コンフィグファイルの書き方
## "config.yml" の書き方
### 設定ファイルのパスを指定する
- baseBlock : 作業台の下に設置する3*3 のブロック名
- results : 成果物の設定ファイルを置いたディレクトリ
- matters : クラフト素材の設定ファイルを置いたディレクトリ
- recipes : レシピの設定ファイルを置いたディレクトリ

設定例　(デフォルトで設定済み)
```yaml
baseBlock: "plugins/Custom_Crafter/baseBlock"
result: "plugins/Custom_Crafter/result"
matter: "plugins/Custom_Crafter/matter"
recipes: "plugins/Custom_Crafter/recipe"
```

### ダウンロードに関する設定
custom crafter では設定ファイルを GitHub などからダウンロードし、適用することが出来ます。  
download セクションにダウンロード「コマンド」をリストの形で書き込むと、それらを実行してファイルをダウンロードします。  
(ダウンロードコマンドには `wget` や `curl` をおすすめします)

- download_threshold : ダウンロードを待機するループ数
- load_interval : １ループの秒数
- download : ダウンロードコマンド(リスト形式)

設定例
```yaml
download_threshold: 20
load_interval: 20
download:
  - 
```

## クラフト素材(Matter)の設定について
必須事項
- name: Matter の名前 (string)
- amount : Matter の個数（１以外に設定した場合の挙動は保証しません） (int)
- mass : クラフト時、この素材の個数を無視して成果物の個数を設定する (true | false)
- candidate : この素材として認識するアイテムのリスト (リスト形式 | string)

設定例
```yaml
name: stone
amount: 1
mass: false
candidate: [stone,cobblestone]

```
### オプション設定
candidate セクションに正規表現を使うことが出来ます。  
正規表現を使用する場合は先頭に "R|"を挿入してください。

設定例
```yaml
candidate: ["R|([a-zA-Z_]{1,20})_CONCRETE_POWDER"]
```
上記の正規表現は各色のコンクリートパウダーを示しています。

---
enchantセクション  
#### 記入方法
この素材に必要とされるエンチャント情報のリスト (リスト形式 | string)  
要求エンチャント名, 要求レベル, 要求厳格性  の順に，カンマ区切りで記入してください.

- 要求エンチャント名 : [Spigot JavaDoc Enchantment](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html) を参考に記入してください.
- 要求レベル名 : 1 ~ 255 の範囲で記入してください.
- 要求厳格性 : NotStrict, OnlyEnchant, Strict のいずれかから選んで記入してください.
  - NotStrict : この厳格性が設定されているエンチャントは，素材に付与されていなくても合同であると判断します.
  - OnlyEnchant : この厳格性が設定されているエンチャントは，素材にこのエンチャントが付与されていればエンチャントレベルが一致しない場合でも合同であると判断します.
  - Strict : この厳格性が設定されているエンチャントは，素材にこのエンチャントが付与されていて，なおかつエンチャントレベルが一致している場合のみ合同であると判断します.

設定例
```yaml
enchant :
   - luck,2,strict
```
上記の例はレベル2の luck (luck_of_the_sea | 宝釣り) を素材に要求しています.

## 成果物(Result)の設定について
必須事項
- name : 成果物の名前
- amount : 成果物の個数
- nameOrRegex : 成果物のアイテム名 (正規表現を使うこともできます)
- matchPoint : 正規表現のマッチ箇所 (正規表現を使用しない場合は -1)

設定例
```yaml
name: alice
amount: 3
nameOrRegex: iron_block
matchPoint: -1
```

nameOrRegex セクションに正規表現を用いた設定例
```yaml
name: bob
amount: 3
nameOrRegex: "([a-zA-Z_]{1,20})_CONCRETE_POWDER@{R}_CONCRETE"
matchPoint: 1
```

nameOrRegex セクションに正規表現を用いる場合は、「素材のアイテム名用の正規表現@成果物のアイテム名」の書式で記述してください。  
また、素材のアイテム名の一部を成果物のアイテム名に適用したい場合は、適用する箇所に"{R}"を挿入してください。  
加えて、matchPoint には素材のアイテム名用の正規表現のマッチ箇所を書いてください。

### オプション設定
- metadata : メタデータ (リスト形式 | Object)

以下に列挙する項目を設定することが出来ます。
- DisplayName : アイテムの表示名 (string)
- Enchantment : 付与するエンチャント、レベル (string,int)
- Lore : アイテムの説明文 (string)
- CustomModelData : カスタムモデルデータの値 (int)
- ItemFlag : 付与したいアイテムフラグ (string)
- Unbreakable : 耐久無限 (true | false)

「項目名,データ」の形式で記述してください。  
また、２つ以上のデータを記述しなくてはいけない場合はカンマで区切ってください。

設定例
```yaml
metadata:
  - "enchantment,luck,1"
  - "displayName,RESULT"
  - "lore,first line"
  - "lore,second line,second line"
  - "customModelData,1"
  - "itemFlag,hide_enchants"
  - "unbreakable,true"
```

## レシピ(Recipe)の設定について
必須事項
- name : レシピの名前 (string)
- tag : レシピのタイプ (string | normal または amorphous)
- result : 成果物として設定するアイテムの名前 (string)
- coordinate : レシピの配置 (list | string)

1. tag について、決められた配置を必要とするレシピでは normal 、不定形レシピでは amorphous としてください。
2. result については、Result を設定したファイルの name セクションに書いたものと同じ内容を記述してください。
3.
   - coordinate については、normal の場合は 1 * 1 から 6 * 6 の正方形で、複数行のリストにして記述してください。
   - amorphous の場合は一行のリストに全ての素材を記述してください。
   - (記述する名前は Matter を設定したファイルの name セクションに書いたものと同じ内容を記述してください)
   - normal の場合、アイテムを必要としない箇所には "null" と記述してください。amorphous の場合には null を記述しないでください。

設定例　(tag : normal の場合)
```yaml
name: recipe_one
tag: normal
result: bob
coordinate:
  - null,stone
  - stone,stone
```

設定例 (tag : amorphous の場合)
```yaml
name: recipe_two
tag: amorphous
result: alice
coordinate: [stone,stone]
```

### オプション設定
- returns : 指定のアイテムに対して返却するアイテム

クラフト時に指定したアイテムに対して任意のアイテムを返却することが出来ます。  
(例：水バケツに対してからのバケツを返却)

設定例
```yaml
returns: 
  - stone,cobblestone,3
```

1. 指定するアイテム名
2. 返却するアイテム名
3. 返却するアイテムの個数

の順番で記述してください。また、それはカンマで区切ってください。