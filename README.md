# custom crafter  
custom crafter is a plugin for minecraft servers (Spigot and Paper) that provides custom recipes. 

---

# Recommended environment
- Paper
  - v4.3: `1.21.3`
  - v4.2: `1.20.x`

Tested in Paper 1.21.3, 1.20.4.  

---

# Recipe Sample
This recipe is provided by two files. 

[Screencast from 2024-06-21 02-42-34.webm](https://github.com/Sakaki-Aruka/custom-crafter/assets/99568054/4dc6149a-4d31-41ae-849f-9b82ba725f04)

<details><summary>Recipe files</summary>
<div>

```yaml
name: multi_stair
amount: 1
candidate: ["R|[A-Z_]+_STAIRS"]
mass: false
```

```yaml
name: saddle_recipe
tag: normal
result: SADDLE
override:
  - LEATHER -> L
coordinate:
  - L,null,L
  - L,multi_stair,L
  - L,L,L
```
</div></details>

---

# License
<details><summary>MIT License</summary><div>
MIT License

Copyright (c) 2023 - 2024 Sakaki-Aruka

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

</div>
</details>

---

# Features
- Set custom recipes to vanilla items.
- Set custom recipes to original items.
- Provide some commands that manages items that material of recipes.
- Provide those recipes to players.
- (custom crafter provides crafting vanilla items from vanilla recipes feature.)

---

# Getting Started
1. Install
   1. [Download from here.(GitHub release page)](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest)
   2. Place the downloaded file to plugins directory. 
   3. Reboot or reload your server.


2. Write configuration files  
If you want to provide custom recipes and custom items to players, you have to write configuration files.


3. Place base block  
custom crafter does not work only a work bench block.  
If you want to use custom crafter features, place the base blocks under a work bench block 3 * 3.  
The default base block is `GOLD_BLOCK`.

---

# Help
## about commands
Run command `/cc help all` from Console or as a player in a server.

## about configuration files
Show [here](https://github.com/Sakaki-Aruka/custom-crafter-config/blob/master/config_description.md). (Jump to `custom-crafter-config/Sakaki-Aruka`)

Written by Japanese only. (wip: English version)
