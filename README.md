# custom crafter
custom crafter is a plugin for PaperMC servers that provides custom recipes.

---

# Features
- Set custom recipes to vanilla items.
- Set custom recipes to original items.
- Provide some commands that manages items that material of recipes.
- Provide those recipes to players.
- (custom crafter provides crafting vanilla items from vanilla recipes feature.)

---

# Support versions
Paper
- v5.0.13 -: `1.21.4 -`
- v5.0.0 - v5.0.11: `1.21.3 - `
- v4.3: `1.21.3`
- v4.2: `1.20.1 - 1.20.4`

**WARNING: custom crafter does not support to run on Spigot servers.**

---

# API

custom crafter works API and also a plugin since version 5.0.0 .  
You can make custom recipes in your plugin and register those.

## Documents
[KDoc](https://sakaki-aruka.github.io/custom-crafter/) / [Wiki (on GitHub)](https://github.com/Sakaki-Aruka/custom-crafter/wiki/Intro)  
You can build a document what type of JavaDoc with `mvn dokka:javadoc` on the project root.

## Dependency Information

**Note: the version name must be a real version string or `master-SNAPSHOT`.**

The latest provided version  
[![](https://jitpack.io/v/Sakaki-Aruka/custom-crafter.svg)](https://jitpack.io/#Sakaki-Aruka/custom-crafter)


<details><summary>Maven</summary>

(repository)
```xml
<repositories>
   <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
   </repository>
</repositories>
```
(dependency)
```xml
<dependencies>
   <dependency>
      <groupId>com.github.Sakaki-Aruka</groupId>
      <artifactId>custom-crafter</artifactId>
      <version>5.0.13-1</version>
      <scope>provided</scope>
   </dependency>
</dependencies>

```

</details>

<details><summary>Gradle (Groovy) </summary>

### Gradle (Groovy)
(repository)
```groovy
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' }
   }
}
```
(dependency)
```groovy
dependencies {
   compileOnly 'com.github.Sakaki-Aruka:custom-crafter:5.0.13-1'
}
```

</details>

<details><summary>Gradle (Kotlin DSL)</summary>

(repository)
```
repositories { 
    mavenCentral()
    maven("https://jitpack.io") {
        name = "jitpack"
    }
}
```

(dependency)
```
dependencies {
    compileOnly("com.github.Sakaki-Aruka:custom-crafter:5.0.13-1")
}
```

</details>

---

# Getting Started
0. Make a plugin
   1. create plugin that uses custom-crafter API

1. Install
   1. [Download from here.(GitHub release page)](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest)
   2. Place the downloaded file to plugins directory.
   3. Reboot or reload your server.


2. Place base block  
   custom crafter does not work only a work bench block.  
   If you want to use custom crafter features, place the base blocks under a work bench block 3 * 3.  
   The default base block is `GOLD_BLOCK`.

---

# Code Example

## Version check
Example code to check if the CustomCrafterAPI version your plugin depends on is fully compatible with the version deployed on your server.
 
````kotlin
class YourPlugin: JavaPlugin() {
    const val DEPEND_CCAPI_VERSION = "0.1.13"
    
    @Override
    fun onEnable() {
        if (!CustomCrafterAPI.hasFullCompatibility(DEPEND_CCAPI_VERSION)) { 
            Bukkit.pluginManager.disablePlugin(this)
            return
        }
    }
}
````

## Recipe
### Make Materials
When creating a custom recipe, you need to specify the arrangement of materials (instances of an implementation class of [CMatterImpl](https://sakaki-aruka.github.io/custom-crafter/custom-crafter/io.github.sakaki_aruka.customcrafter.api.object.matter/-c-matter-impl/index.html) or  [CMatter](https://sakaki-aruka.github.io/custom-crafter/custom-crafter/io.github.sakaki_aruka.customcrafter.api.interfaces.matter/-c-matter/index.html) ) along with their coordinates.  
Below is an example of how to create the material items used in this process.
```Kotlin
// In Kotlin
val matter: CMatter = CMatterImpl(
    name = "test-matter",
    candidate = setOf(Material.STONE, Material.COBBLESTONE),
    amount = 1,
    mass = false,
    predicates = null
)
```

```Kotlin
// In Kotlin
val matter: CMatter = CMatterImpl.single(Material.STONE)
```
This is a common example of creating a Matter that acts as a material when either "Stone" or "Cobblestone" is placed.  
If only one item type is needed in the `candidate` field and other parameters use default values (which makes it almost identical to vanilla Minecraft recipes), you can use the following shorthand:

---

### Make Results
Generally, after defining the materials, the next step is to define the crafting result.  
In CustomCrafter, it is not mandatory to provide an item as a result of crafting. You can also choose to execute a command instead. If you prefer this approach, you can skip this "Results" section and move on to the recipe definition section.

The creation of results in a custom recipe is mainly handled by [ResultSupplier](https://sakaki-aruka.github.io/custom-crafter/custom-crafter/io.github.sakaki_aruka.customcrafter.api.object.result/-result-supplier/index.html) .
It allows you to retrieve various conditions during crafting and determine the final output item accordingly.
```Kotlin
// In Kotlin
val supplier = ResultSupplier { config ->
    // 'config' is 'ResultSupplier.Config' or 'AutoCraftResultSupplier.Config'. 
    // This contains some useful values.
    
    // Write processes here.
    // ResultSupplier is a lambda expression.
    // Return type is `List<ItemStack>`
    emptyList<ItemStack>()
}
```
For recipes that do not require complex processing, CustomCrafter provides simplified versions of the above expression.
```Kotlin
// In Kotlin
val supplier = ResultSupplier.single(ItemStack(Material.STONE))
val supplier2 = ResultSupplier.timesSingle(ItemStack(Material.STONE))
```
[ResultSupplier#single](https://sakaki-aruka.github.io/custom-crafter/custom-crafter/io.github.sakaki_aruka.customcrafter.api.object.result/-result-supplier/-companion/single.html) returns the specified `ItemStack` when the recipe is invoked.
[ResultSupplier#timesSingle](https://sakaki-aruka.github.io/custom-crafter/custom-crafter/io.github.sakaki_aruka.customcrafter.api.object.result/-result-supplier/-companion/times-single.html) is slightly smarter than `single`. When a player crafts multiple items at once (using Shift + Left Click), it multiplies the number of `ItemStack`s provided by the number of times the recipe is invoked.
(It might be easier to understand by testing it yourself. 🙂)

---

### Make Recipe
Recipes are the core component of the custom crafting system.  
They group together custom materials and results, registering them in the CustomCrafter system as a single craftable recipe.
The default implementation of recipes is [CRecipeImpl](https://sakaki-aruka.github.io/custom-crafter/custom-crafter/io.github.sakaki_aruka.customcrafter.api.object.recipe/-c-recipe-impl/index.html) , but if you require additional customization, you can implement your own `CRecipe` class.

Below is an example of a simple recipe:
```Kotlin
// In Kotlin
val recipe: CRecipe = CRecipeImpl(
    name = "test-recipe",
    items = mapOf(CoordinateComponent(0, 0) to matter), // 'matter' is CMatter
    containers = null,
    results = setOf(ResultSupplier.timesSingle(Material.STONE)),
    type = CRecipeType.NORMAL
)
```
-   `items`: Specifies the coordinates and corresponding `CMatter` in a map for the crafting grid.
-   `containers`: Defines additional conditions (such as required player permissions) that must be met for the recipe to be crafted. For simple recipes, this is not needed.
-   `results`: A set of `ResultSupplier` instances. If all crafting conditions are met, these are executed, and the resulting items are given to the player.
-   `type`: Specifies the type of the custom recipe. Options include `NORMAL` (shaped recipe) and `AMORPHOUS` (shapeless recipe).  
    If the recipe is shapeless, the `CoordinateComponent` values in `items` can be arbitrary. 🙂

For elements not covered in this section, please refer to the [documentation](https://sakaki-aruka.github.io/custom-crafter/index.html).

---

# LICENSE
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

