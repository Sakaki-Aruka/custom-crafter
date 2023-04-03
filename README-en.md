# custom-crafter

# A grammar of the configuration files.

## about "config.yml"
### location sections
setting files set section
- baseBlock : write base block's config file location.
- results : write "result" config files location.
- matters : write "matter" config files location.
- recipes : write "recipe" config files location.

example (default)
```yaml
baseBlock: "plugins/Custom_Crafter/baseBlock"
result: "plugins/Custom_Crafter/result"
matter: "plugins/Custom_Crafter/matter"
recipes: "plugins/Custom_Crafter/recipe"
```

### download settings section
- download_threshold (int)
- load_interval (int)
- download (list | string)

example
```yaml
download_threshold: 20
load_interval: 20
download:
  - 
```
Write download "command" in download section.  
(Only run commands.)  
example) :  
`wget -P plugins/Custom_Crafter/baseBlock https://github.com/Sakaki-Aruka/custom-crafter-config/raw/master/baseBlock.yml`


## about Matter
Need sections
- name : a name of a matter. (string)
- amount : a quantity of a matter. (int)
- mass : (bool)
- candidate : this matter's candidate (list)

example 
```yaml
name: stone
amount: 1
mass: false
candidate: [stone,cobblestone]
```

### Optional settings in the candidate section.  
You can use regularly compression to write those.

example
```yaml
candidate: ["R|([a-zA-Z_]{1,20})_CONCRETE_POWDER"]
```
This regex means to set each colored concrete powder to a matter's candidate.

## about Result
Need sections
- name: a name of a result. (string)
- amount : a quantity of a result. (int)
- nameOrRegex : a result item's material name. (string | written in regex or normal Material-name)
- matchPoint : regex match point (int | when you do not want to use regex, you must set -1)

example
```yaml
name: alice
amount: 3
nameOrRegex: iron_block
matchPoint: -1
```
or in a case of to use regex in nameOrRegex section.

example(regex)
```yaml
name: bob
amount: 3
nameOrRegex: "([a-zA-Z_]{1,20})_CONCRETE_POWDER@{R}_CONCRETE"
matchPoint: 1
```
this regex pattern means each colored concretes.
(black_concrete, blue_concrete, ... and more)

Optional sections
- metadata

### metadata
You can set data of
- DisplayName (string)
- Enchantment (string,int)
- Lore (string)
- CustomModelData (int)
- ItemFlag (string)
- Unbreakable (bool)

separate with ",".

example
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

## about Recipe
Need section
- name: a name of a recipe. (string)
- tag : a type of a recipe. (string | normal or amorphous)
- result : a name of a result item's name. (string)
- coordinate : a coordinate of a recipe. (list)

("amorphous" means "shapeless".)

example (coordinate type is normal.)
```yaml
name: recipe_one
tag: normal
result: bob
coordinate:
  - null,stone
  - stone,stone
```

coordinate section can set 1 * 1 to 6 * 6 __square__ size.  
(1 * 1, 2 * 2, 3 * 3, 4 * 4, 5 * 5, 6 * 6)

Optional section
- returns

You can set return items when you push crafting button. (anvil)

```yaml
returns: 
  - stone,cobblestone,3
```
grammar of this section :
- target material name (string)
- return material name (string)
- return material quantity (int)

separate with ",".