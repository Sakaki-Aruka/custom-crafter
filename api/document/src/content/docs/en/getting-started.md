---
title: Hello CustomCrafterAPI!
---

## What is CustomCrafterAPI?
CustomCrafterAPI is a plugin that provides a foundation for crafting recipes ranging from 1×1 to 6×6 in size.
It supports everything from simple recipes that only check item types—just like Minecraft's standard recipes—to complex ones that leverage data held by input items or players, and even recipes that determine craftable items by querying external databases or APIs.

## Acknowledgement
This product includes software developed by the IMT Atlantique.

## Important notice
CustomCrafterAPI versions 5.X.X are treated as alpha releases; 6.0.0 and later are treated as stable releases.
In 5.X.X, classes and functions may change significantly between a single version bump.

If you are using a plugin that implements recipes with CustomCrafterAPI, make sure that the version used as a dependency is compatible with the version of CustomCrafterAPI installed on your server.

## Installation
CustomCrafterAPI cannot be used simply by adding it as a plugin dependency — it must be placed in the server's plugin directory **and** configured as a dependency of your plugin for it to function correctly.

Note that CustomCrafterAPI alone only provides the bare minimum — the crafting UI and the recipe registration system. Creating your own plugin is required to provide custom recipes.

Features provided by CustomCrafterAPI alone:
- Crafting vanilla recipes from the CustomCrafterAPI crafting UI
- Opening a dedicated screen when a crafting table is clicked under appropriate conditions (enabled by default; can be disabled via permission settings)
- Commands to change various CustomCrafterAPI settings (requires administrator permissions by default)
- A command to open the CustomCrafterAPI crafting UI (disabled by default; can be enabled via permission settings)

### As a plugin
The jar file for use as a plugin can be obtained from [Modrinth](https://modrinth.com/plugin/custom-crafter-api/versions) or [GitHub Releases](https://github.com/Sakaki-Aruka/custom-crafter/releases).
The supported Minecraft version does not change across versions, so using the latest release is recommended.

### As a library

CustomCrafterAPI is provided under the MIT License.

CustomCrafterAPI as a library can be added as a dependency via [Maven Central](https://central.sonatype.com/artifact/io.github.sakaki-aruka/custom-crafter-api).

Because CustomCrafterAPI manages recipe registration and unregistration in one place, it must be provided as a runtime dependency rather than bundled.
Set the dependency scope appropriately: `provided` in Maven, `compileOnly` in Gradle.

If you are implementing your plugin in Kotlin, the Kotlin standard library (`kotlin-stdlib`) must also be declared as a provided/runtime dependency.
Use `provided` in Maven and `compileOnly` in Gradle for this as well.

:::note
Most of CustomCrafterAPI is implemented in Kotlin. It loads `kotlin-stdlib` as a shared library at server startup, which is why a provided-scope declaration is required.
:::

## About Recipes

In CustomCrafterAPI, unless otherwise noted, "recipe" refers to the `CRecipe` interface and its implementing classes — not Minecraft's original recipe system.
To provide a recipe to players, you must define its components and register them with CustomCrafterAPI.
The main parts you need to define are:
- Material (ingredient)
- Result item (the item given to the player when the recipe matches)
- Item arrangement
- Arrangement type (shaped or shapeless)

In addition to the above, elements such as functions executed when an item is crafted are also available.

## Implementing Elements
The interfaces `CRecipe` and `CMatter` are provided for recipes and their required materials respectively.
The standard implementing classes `CRecipeImpl` and `CMatterImpl` are also provided.

:::note
In CustomCrafterAPI, classes whose names end in `Impl` are the standard implementations of their respective interfaces.
Examples: `CRecipeImpl`, `CMatterImpl`
:::

Using these, you can create recipes that handle basic items.
By further extending the interfaces and adding custom fields, you can implement more advanced logic.
For results and item arrangement, `ResultSupplier` and `CoordinateComponent` are provided; you assemble recipes using these.

## Implementing Advanced Elements
As mentioned above, CustomCrafterAPI provides mechanisms for performing advanced processing at crafting time.
Using these, you can set fine-grained conditions for recipe matching or customize the crafting screen per player.

## Events
In addition to recipes, CustomCrafterAPI fires several Bukkit events, including one that notifies when an item has been crafted.
Since these implement Bukkit's `Event`, they can be listened to and handled the same way as any standard Minecraft event listener.
