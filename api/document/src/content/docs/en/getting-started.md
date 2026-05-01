---
title: Getting Started with CustomCrafterAPI
---

## What is CustomCrafterAPI?
CustomCrafterAPI is a plugin that provides a foundation for offering recipes ranging from 1×1 to 6×6 in size.
It supports everything from simple recipes that only check item types — like standard Minecraft recipes — to recipes that leverage data held by the input items or the player, and even recipes whose craftable items are determined by querying external databases or APIs.

## Acknowledgement
This product includes software developed by the IMT Atlantique.

(This software includes Choco Solver, created by IMT Atlantique.)

## Important notice
CustomCrafterAPI treats versions 5.X.X as alpha releases and 6.0.0 and later as stable releases.
In the 5.X.X range, a single version bump may introduce significant changes to classes and function signatures.

If you use a plugin that implements CustomCrafterAPI recipes, make sure the version declared as a dependency is compatible with the version of CustomCrafterAPI installed on your server.

## Installation
CustomCrafterAPI cannot be used simply by adding it as a plugin dependency — it works correctly only when it is set up both as a plugin in the server's plugin directory and as a dependency of your plugin.

Note that CustomCrafterAPI on its own only provides the bare essentials, such as the crafting UI and the recipe registration system, so creating your own plugin is required if you want to offer custom recipes.

The features provided by CustomCrafterAPI alone are as follows:
- The ability to craft vanilla recipes from the CustomCrafterAPI crafting UI
- The ability to open the dedicated screen when a crafting table is clicked under the appropriate conditions (enabled by default; can be disabled via permission settings)
- Commands to change various CustomCrafterAPI configuration values (administrator permission required by default)
- A command to open the CustomCrafterAPI crafting UI (disabled by default; can be enabled via permission settings)

### As a plugin
The jar file for use as a plugin can be obtained from [Modrinth](https://modrinth.com/plugin/custom-crafter-api/versions) or [GitHub Releases](https://github.com/Sakaki-Aruka/custom-crafter/releases).
The supported Minecraft version does not change across versions, so using the latest release is recommended.

### As a library

:::tip
The minimum required API version rarely changes between updates. Using the latest release is recommended.
:::

CustomCrafterAPI is provided under the MIT license.

CustomCrafterAPI as a library can be incorporated into your plugin as a dependency from [Maven Central](https://central.sonatype.com/artifact/io.github.sakaki-aruka/custom-crafter-api).

Because CustomCrafterAPI must manage recipe registration and deregistration in a single place, the dependency is provided at runtime rather than bundled.
Therefore you need to set the dependency scope appropriately.
In Maven use `provided`; in Gradle use `compileOnly`.

When implementing a plugin that uses CustomCrafterAPI in Kotlin, the Kotlin standard library (`kotlin-stdlib`) must also be provided at runtime rather than bundled.
Use the `provided` scope in Maven and `compileOnly` in Gradle here as well.

:::note
The majority of CustomCrafterAPI is implemented in Kotlin, and kotlin-stdlib is loaded as a shared library at server startup, which is why the dependency is provided at runtime.
:::

## About recipes

Unless otherwise noted, "recipe" in CustomCrafterAPI refers to the `CRecipe` interface and its implementing classes — not Minecraft's built-in recipes.
To offer a recipe to users, you need to define its constituent parts and register them with CustomCrafterAPI.
The four main parts you must define are:
- Matter (ingredient)
- Result (the item given to the player when the recipe matches)
- Item placement
- Item placement type (shaped or shapeless)

In addition to the above, elements such as functions executed when an item is crafted are also available.

## Implementing elements
For recipes and the matters they require, there are interfaces called `CRecipe` and `CMatter` respectively.
There are also classes called `CRecipeImpl` and `CMatterImpl` that implement them.

:::note
In CustomCrafterAPI, classes whose names end with `Impl` are generally the standard implementations of their respective interfaces.
(e.g.) `CRecipeImpl`, `CMatterImpl`
:::

Using these, you can create recipes that handle basic items.
By further extending the interfaces and creating classes with custom fields, you can implement more advanced logic.
For the result and item placement, the interfaces and classes `ResultSupplier` and `CoordinateComponent` are provided respectively — use these to assemble your recipe.

## Implementing additional elements
As introduced in the section above, CustomCrafterAPI provides mechanisms that can be used to perform advanced processing, such as when an item is crafted.
Using them, you can set fine-grained conditions on recipe searches and customize the crafting screen on a per-player basis.

The representative additional elements are as follows:
- [`CMatterPredicate`](/ja/recipe/matter/#predicates) — Adds arbitrary validation logic for items in individual slots
- [`CRecipePredicate`](/ja/recipe/predicate/) — Adds validation targeting the entire recipe, including player information
- [`CraftUIDesigner`](/ja/extra/craftui-designer/) — Customizes the crafting UI title and slot layout

## Events
In addition to recipes, CustomCrafterAPI fires several events, including one that notifies you when an item has been crafted.
Since these are classes that implement Bukkit's `Event`, they can be listened to and handled in the same way as standard Minecraft event listeners.

See the [Events page](/ja/events/) for details.
