# custom crafter

custom crafter is a custom recipe plugin for PaperMC servers, and also a library (API) for defining custom recipes from your own plugin.

Define custom recipes freely from your plugin and introduce new elements into the game.

[Japanese README (日本語ドキュメント)](README_JP.md)

---

## Features

- **Custom recipes for vanilla items** — add new recipes for existing Minecraft items.
- **Custom item support** — use items created by your plugin as results or materials.
- **Material management commands** — for server administrators managing recipe materials.
- **Automatic integration** — registered recipes appear in players' crafting environment without extra setup.
- Vanilla crafting continues to work unmodified.

---

## Demo

<details><summary>Crafting demo video</summary>

1. Place base blocks (`GOLD_BLOCK`).
2. Craft `infinityIronBlockCore` [(recipe definition)](./demo/src/main/kotlin/online/aruka/custom_crafter/demo/register/ShapedRecipeProvider.kt).
3. Compress an iron block into `infinityIronBlock` [(recipe definition)](./demo/src/main/kotlin/online/aruka/custom_crafter/demo/register/ShapedRecipeProvider.kt).
4. Repeat with the same recipe.
5. Repeat again.
6. Extract the infinity iron block with `infinityIronBlockExtract` [(recipe definition)](./demo/src/main/kotlin/online/aruka/custom_crafter/demo/register/ShapelessRecipeProvider.kt).

![](./resources/crafting-demo.gif)

</details>

Clone this repository and run the following command to build a demo plugin containing the recipes shown above, along with several others:

```
mvn -pl demo package
```

The resulting jar, ready to be placed in a server's `plugins` directory, is written to `demo/target`.

---

## Supported Environments

| Custom_Crafter Version               | Paper Version            |
|:--------------------------------------|:--------------------------|
| **5.3.0 (latest)**                    | 1.21.4 ~ 1.21.11, 26.1.x |
| 5.0.13 ~ 5.0.21, 5.1.0, 5.2.0, 5.2.1  | 1.21.4 ~ 1.21.11, 26.1.x |
| 5.0.0 ~ 5.0.11                        | 1.21.3                    |
| 4.3 (legacy)                          | 1.21.3                    |
| 4.2 (legacy)                          | 1.20.1 ~ 1.20.4           |

The minimum supported Paper version rarely changes between releases, so using the latest release is recommended.

**Note:** custom crafter does not run on Spigot/Bukkit. Use PaperMC or a Paper fork.

---

## Using the API

Since version 5.0.0, custom crafter also works as an API for defining and registering custom recipes from your own plugin.

### Documentation

- [KDoc (Kotlin)](https://sakaki-aruka.github.io/custom-crafter/kdoc/)
- [Javadoc (Java)](https://sakaki-aruka.github.io/custom-crafter/javadoc/)
- [Documentation site](https://sakaki-aruka.github.io/custom-crafter/document/en/getting-started/)

To build the documentation locally:

```
mvn -pl api dokka:dokka      # KDoc
mvn -pl api dokka:javadoc    # Javadoc
```

### Dependency

Plugins depending on CustomCrafterAPI must list `Custom_Crafter` under `depend` in `plugin.yml`:

```yaml
depend:
  - "Custom_Crafter"
```

---

Latest version: 5.3.0 ([Maven Central](https://central.sonatype.com/artifact/io.github.sakaki-aruka/custom-crafter-api/versions))

The CustomCrafter plugin is assumed to be present at runtime, so set its dependency scope to compile-time only — the same applies to the Kotlin stdlib if your plugin is written in Kotlin.

Compile-time-only scope name by build tool:
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
    compileOnly 'org.jetbrains.kotlin:kotlin-stdlib:2.3.0' // if using Kotlin
}
```

</details>

<details><summary>Gradle (Kotlin DSL)</summary>

```kotlin
dependencies {
    compileOnly("io.github.sakaki-aruka:custom-crafter-api:5.3.0")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0") // if using Kotlin
}
```

</details>

---

## Server Installation

custom crafter is written in Kotlin.

1. [Download the plugin jar](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest) and place the downloaded files in your `plugins` directory.
2. Start or reload the server.
3. Place base blocks in a 3x3 area directly beneath a standard workbench — a standard workbench alone does not enable custom recipes. The default base block is `GOLD_BLOCK`.

---

## Code Samples

### Compatibility check

Checks whether the CustomCrafterAPI version your plugin depends on is compatible with the version running on the server:

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

### Defining a recipe

A custom recipe consists of three parts:

1. **Material** (`CMatter`) — the input requirements for crafting.
2. **Result** (`ResultSupplier`) — what a successful craft produces.
3. **Recipe** (`CRecipe`) — combines materials, results, and shape (shaped/shapeless) for registration.

#### 1. Materials (`CMatter`)

`CMatter` defines which item, how many, and where it must be placed. Use `CMatterImpl` or your own implementation of `CMatter`.

Accept 1 stone or 1 cobblestone:

```kotlin
val matter: CMatter = CMatterImpl(
    name = "test-matter",
    candidate = setOf(Material.STONE, Material.COBBLESTONE),
    amount = 1,
    anyAmount = false, // true: allow the amount to be satisfied across a stack
    predicates = null  // additional NBT or other conditions
)
```

For a simple set of candidate materials, `of` is shorter:

```kotlin
val matter: CMatter = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
```

#### 2. Results (`ResultSupplier`)

A `ResultSupplier` receives the crafting context (`Config`) and returns the output items. Command execution can be used instead where no item result is needed.

```kotlin
val supplier = ResultSupplier { config ->
    // 'config' holds crafting context: player, workbench, etc.
    emptyList<ItemStack>()
}
```

Helper methods cover common cases:

```kotlin
// Always returns 1 stone
val supplier = ResultSupplier.single(ItemStack.of(Material.STONE))

// Scales output with the number of items crafted in one action (e.g. shift-click)
val supplier2 = ResultSupplier.timesSingle(ItemStack.of(Material.STONE))
```

#### 3. Recipe (`CRecipe`)

`CRecipeImpl` combines materials and results into a registrable recipe.

```kotlin
val recipe: CRecipe = CRecipeImpl(
    name = "test-recipe",
    items = mapOf(CoordinateComponent(0, 0) to matter),
    containers = null, // additional conditions, e.g. permissions
    results = setOf(ResultSupplier.timesSingle(Material.STONE)),
    type = CRecipe.Type.NORMAL
)
```

- `items`: maps grid coordinates (`CoordinateComponent(x, y)`) to the required `CMatter`.
- `type`: `CRecipe.Type.NORMAL` is a shaped recipe, where coordinates matter; `CRecipe.Type.AMORPHOUS` is shapeless, where coordinate values are arbitrary.

---

## License

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
