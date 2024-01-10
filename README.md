# custom crafter  
custom crafter is a plugin for minecraft servers (Spigot or Paper) that provides custom recipes.  

# Features
- Set custom recipes to vanilla items.
- Set custom recipes to original items.

# Getting Start
## Install
- [Download here.](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest)
 - Place the downloaded file to plugins directory.
 - Reboot or reload your server.

# 概要
custom crafter は，既存のアイテムに新しいレシピを追加，カスタムアイテムにレシピを設定することが出来る Spigot 向けプラグインです．  
使用するには `config.yml` 以外にも yaml ファイルを作成する必要があり，それらファイルを格納しておくためのディレクトリも必要とします.  

本プラグインは MIT ライセンスのもとに配布されます.  
ライセンス本文は LICENSE.txt に記されています.  

不具合などありましたら，[製作者のTwitter](https://twitter.com/yt0f1), もしくは Discord (ytshiyugh #5820) までご連絡ください.  


# 使い方
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
