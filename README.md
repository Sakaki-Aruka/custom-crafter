# 💎 custom crafter - The Ultimate Custom Recipe Plugin & Library

custom crafter is a custom recipe provider plugin for **PaperMC servers**, which also functions as a powerful **Custom Recipe Creation Library (API)**.

You can define custom recipes freely within your own plugin and introduce new elements into the game.

([Japanese Document (日本語ドキュメント)](README_JP.md))

---

## 🚀 Key Features

* **Set Custom Recipes for Vanilla Items:** Easily add new recipes for existing Minecraft items.
* **Support for Original Item Crafting:** Use **custom items** created by your plugin as results or materials.
* **Advanced Material Management:** Provides commands for managing items used as recipe materials. (For Server Administrators)
* **Seamless Provision to Players:** Automatically integrate your defined custom recipes into the players' crafting environment.
* **(Compatibility):** Vanilla crafting remains fully functional.

---

## 🎥 Demo Video

<details><summary>Crafting Demo Video</summary>

1. Place base blocks (`GOLD_BLOCK`).
2. Create `infinityIronBlockCore` [(Jump to the file where the recipe is defined)](./demo/src/main/kotlin/online/aruka/demo/register/ShapedRecipeProvider.kt).
3. Compress Iron Block with `infinityIronBlock` [(Jump to the file where the recipe is defined)](./demo/src/main/kotlin/online/aruka/demo/register/ShapedRecipeProvider.kt).
4. Use same recipe (`infinityIronBlock`)
5. Use same recipe (`infinityIronBlock`)
6. Extract Infinity Iron Block with `infinityIronBlockExtract` [(Jump to the file where the recipe is defined)](./demo/src/main/kotlin/online/aruka/demo/register/ShapelessRecipeProvider.kt).

![](./resources/crafting-demo.gif)

</details>  

After cloning this repository locally, you can build a demo plugin that provides the recipe included in this video, as well as several other recipes, by running the following commands.  

`mvn -pl demo package`  

The jar file, which can be placed in the server's plugins directory, will be created in the demo/target directory.

---

## 🛠️ Supported Environments and Versions

| Custom_Crafter Version       | Paper Version        |
|:-----------------------------|:---------------------|
| **5.0.13 ~ 5.0.19 (Latest)** | **1.21.4 ~ 1.21.11** |
| 5.0.0 ~ 5.0.11               | 1.21.3               |
| 4.3 (Legacy)                 | 1.21.3               |
| 4.2 (Legacy)                 | 1.20.1 ~ 1.20.4      |

> **⚠️ Essential Warning:**
> custom crafter **does not support running on Spigot/Bukkit servers**. Please ensure you run it on **PaperMC** or a PaperMC-fork.

---

## 💻 Utilizing the API - Integrating into Your Plugin

Since version 5.0.0, custom crafter is designed not only as a plugin but also as an **API** for defining and registering custom recipes.

You can freely create custom recipes from your plugin and register them into the CustomCrafter system.

### Documentation

For more detailed information and a complete list of classes and methods, please refer to:

* [KDoc (Kotlin)](https://sakaki-aruka.github.io/custom-crafter/)
* [Wiki (GitHub)](https://github.com/Sakaki-Aruka/custom-crafter/wiki/Intro)

or Build:  
* KDoc Style: `mvn -pl api dokka:dokka`
* JavaDoC Style: `mvn -pl api dokka:javadoc`

### Dependency Information

Plugins that depend on CustomCrafterAPI must add `Custom_Crafter` to the `depend` section of their `plugin.yml`.  
```yaml
depend:
  - "Custom_Crafter"
```

---

Latest Version: 5.0.19 [Maven Central (versions)](https://central.sonatype.com/artifact/io.github.sakaki-aruka/custom-crafter-api/versions)  

When using the API, you must assume the CustomCrafter plugin will be present at runtime. Therefore, set the scope to **compile-time only**.  
Also, if you are creating plugins in Kotlin, please set the Kotlin-stdlib dependency to "compile-time only".  

"compile-time" scope names in:
* Maven: `provided`
* Gradle: `compileOnly`

<details><summary>Maven Configuration Example</summary>

From Maven Central

```xml
<!-- CustomCrafterAPI Dependency -->
<dependency>
    <groupId>io.github.sakaki-aruka</groupId>
    <artifactId>custom-crafter-api</artifactId>
    <version>5.0.19</version>
    <scope>provided</scope>
</dependency>

<!-- kotlin-stdlib Dependency -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>2.3.0</version>
    <scope>provided</scope>
</dependency>
```

</details>

<details><summary>Gradle (Groovy) Configuration Example</summary>

```groovy
dependencies {
    // CustomCrafterAPI Dependency
    compileOnly 'io.github.sakaki-aruka:custom-crafter-api:5.0.19'
    
    // kotlin-stdlib Dependency (If you needed)
    compileOnly 'org.jetbrains.kotlin:kotlin-stdlib:2.3.0'
}
```

</details>

<details><summary>Gradle (Kotlin DSL) Configuration Example</summary>

```Kotlin
dependencies {
    // CustomCrafterAPI Dependency
    compileOnly("io.github.sakaki-aruka:custom-crafter-api:5.0.19")
    
    // kotlin-stdlib Dependency (If you needed)
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
}
```

</details>

---

## ⚙️ Server Installation Steps

CustomCrafter is written in Kotlin and requires a prerequisite library to run.

1.  **Download the CustomCrafter Plugin**
    1.  [Download the CustomCrafterAPI jar (GitHub Releases)](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest)
    2. Place all downloaded files into your `plugins` directory.
2.  **Start/Reload Your Server**
3.  **Set Up the Custom Crafting Station (Base Block)**
    * CustomCrafter recipes do not work with just a standard workbench block.
    * The custom crafting feature is enabled by placing the base blocks in a **3x3 area directly underneath the standard workbench block**.
    * The default base block is **`GOLD_BLOCK`**.

---

## 🧑‍💻 Code Samples and API Usage

Here is a basic guide on defining custom recipes in your plugin using the CustomCrafterAPI.

### Compatibility Check (For Safe Startup)

Example code to check if the CustomCrafterAPI version your plugin depends on is fully compatible with the version deployed on the server.

```kotlin
/*
 * This code is intended to strictly check compatibility with the API installed on the server,
 * and it's not necessarily required for the plugin to function.
 */
class YourPlugin: JavaPlugin() {
    // Define the dependent API version as a constant
    const val DEPEND_API_VERSION = "5.0.19"
    
    @Override
    fun onEnable() {
        // Disable the plugin if there is no compatibility to prevent errors
        if (!CustomCrafterAPI.hasFullCompatibility(DEPEND_API_VERSION)) { 
            Bukkit.pluginManager.disablePlugin(this)
            return
        }
    }
}
```

---

### 📝 Recipe Definition

A custom recipe is mainly composed of three elements:

1.  **Custom Material** (`CMatter`): Defines the conditions for the "materials" required for crafting.
2.  **Crafting Result** (`ResultSupplier`): Defines what is generated as the "result" upon successful crafting.
3.  **Recipe Body** (`CRecipe`): Groups the materials, results, and crafting shape (shaped/shapeless) for registration.

#### 1. Creating Custom Materials (CMatter)

Define the items (`CMatter`) that serve as materials for the recipe. `CMatter` determines **which item**, **how many**, and **where** it needs to be placed to allow crafting.

* Use `CMatterImpl` or an implementation class of the `CMatter` interface.

**Example: Use 1 Stone OR 1 Cobblestone as a material**

```Kotlin
// In Kotlin
val matter: CMatter = CMatterImpl(
    name = "test-matter",
    candidate = setOf(Material.STONE, Material.COBBLESTONE), // Stone or Cobblestone can be used
    amount = 1, // Required amount
    mass = false, // true: consider stacking (usually false)
    predicates = null // Additional NBT or other conditions (usually null)
)
```

**Using the Shorthand (`of`)**

In simple cases where multiple `Material` types are accepted for the `candidate`, the following shorthand is convenient:

```Kotlin
// In Kotlin
val matter: CMatter = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
```
This example creates a **flexible material** that functions as a material if either **Stone** or **Cobblestone** is present.

---

#### 2. Creating Crafting Results (ResultSupplier)

Define the items given to the player or the process executed when the recipe is completed.

* If complex processing is not needed, you can skip defining result items and opt for **command execution** as the result instead.

A **ResultSupplier** is a **function (supplier)** that receives various crafting conditions (`Config`) and determines the final output items (`List<ItemStack>`).

**Example: Returning a result based on complex conditions**

```Kotlin
// In Kotlin
val supplier = ResultSupplier { config ->
    // 'config' includes information about the crafting environment (player, workbench, etc.)
    
    // Write the process to create and return a list of ItemStacks here.
    emptyList<ItemStack>() // Example: returns nothing
}
```

**Utilizing Simplified Helper Methods**

If complex processing is not required, the helper methods provided by CustomCrafter are useful.

```Kotlin
// In Kotlin
// Always returns the specified ItemStack (e.g., 1 Stone)
val supplier = ResultSupplier.single(ItemStack.of(Material.STONE))

// Returns the ItemStack multiplied by the number of times the player shift-clicked to craft multiple items (smart behavior)
val supplier2 = ResultSupplier.timesSingle(ItemStack.of(Material.STONE))
```
`ResultSupplier#timesSingle` is very convenient as it automatically manages the behavior when a player crafts a large amount at once. 😊

---

#### 3. Creating and Registering the Recipe Body (CRecipe)

This is the core component that combines the custom materials and crafting results to **register the recipe** within the CustomCrafter system.

* The default implementation, `CRecipeImpl`, is typically used.

**Example: Defining a Simple Shaped Recipe**

```Kotlin
// In Kotlin
val recipe: CRecipe = CRecipeImpl(
    name = "test-recipe",
    items = mapOf(CoordinateComponent(0, 0) to matter), // 'matter' is the CMatter created in step 1.
    containers = null, // Additional conditions (permissions, etc.). null if not needed.
    results = setOf(ResultSupplier.timesSingle(Material.STONE)), // A Set of ResultSuppliers created in step 2.
    type = CRecipeType.NORMAL // Shaped Recipe
)
```

* `items`: A map of coordinates (`CoordinateComponent(x, y)`) and the required material (`CMatter`) in the crafting grid.
* `type`: Specifies the recipe shape.
    * `CRecipeType.NORMAL`: **Shaped Recipe**. The coordinates in `items` are crucial.
    * `CRecipeType.AMORPHOUS`: **Shapeless Recipe**. The `CoordinateComponent` values in `items` can be arbitrary. 😊

---

## 🔑 License

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

---

## 🙏 Acknowledgement

### choco-solver  

This product includes software developed by the IMT Atlantique.