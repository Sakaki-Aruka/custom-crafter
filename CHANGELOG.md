# Changelog

# 5.0.18

## ✨ New Features and Enhancements

* **[Enhancement]** MCKotlin-Paper is no longer required to run CustomCrafterAPI.

# 5.0.17-p2

## 🛠 Fix
* Result item give fix

# 5.0.17-p1

## ✨ New Features and Enhancements
* **[Enhancement]** Add recipe relation map utility functions on `CoordinateComponent`.

## 🛠 Fix
* Page close error fix

# 5.0.17

## ⚠️ Breaking Changes

* Refactored **`*Predicate`** and **`ResultSupplier`** interfaces as functional interfaces (**SAM**) and removed their default implementations. This allows for concise lambda expressions, but existing code relying on the previous inheritance structure will require updates.
* Removed internal utility and container classes, specifically **`CRecipeContainer`** and **`InventoryUtil`**. Logic depending on these classes must be migrated to the new API structure.
* The recipe evaluation system has been overhauled with the introduction of `CRecipePredicate`, which may require adjustments to how evaluation logic is invoked in your implementation.

---

## ✨ New Features and Enhancements

* **[New Feature]** Added **`CRecipePredicate`**, a new system that allows for comprehensive evaluation across the entire recipe. This enables the implementation of highly flexible and advanced recipe validation logic.
* **[New Feature]** Added support for **asynchronous operations**. This improves server performance by allowing heavy processing tasks to be handled off the main thread.

---

## 🛠 Fix

* Conducted internal cleanup by removing obsolete classes and optimizing interfaces to improve overall maintainability.

# 5.0.16

## ⚠️ Breaking Changes

* The Enums used for configuring `CMatter` and `CRecipe` have been moved from standalone classes to **nested classes** within their respective parent classes. This requires changes to how these Enums are referenced in existing code.
* Several functions within the `CustomCrafterAPI` have been relocated to more appropriate classes based on their role. Users consuming the API should review their function calls and make necessary adjustments.

---

## ✨ New Features and Enhancements

* **[New Feature]** Added a new feature that allows the customization of the recipe arrangement (layout) on the crafting screen. This enables more flexible UI design. (`CraftUIDesigner`)
* **[Enhancement]** Enhanced Java interoperability for static functions and values.
* **[Enhancement]** Users can now initialize several classes.

---

## 🛠 Fix

* We can now return correct answers for several search patterns.

# 5.0.15(-p1)

## ⚠️ Breaking Changes / Important Dependency Change

* The **MCKotlin-Paper** plugin is now **required** to be installed for the library to run, starting from this version.

---

## ✨ New Features and Enhancements

* **[New Feature]** Added a new recipe that allows the use of air items as inputs.
* **[Enhancement]** Subclasses of `CRecipe` can now override the required input item amount used during the recipe search process.
* **[Enhancement]** Several default implementation classes have been made `open` to allow for extension through inheritance.
* **[Other]** Added demonstration plugin code for the `CustomCrafterAPI` to the `demo` module.

# 5.0.14
Delete auto-crafting feature, internal changes.  

## Feature
- [Deleted] Auto-Crafting feature deleted.
## Code
- [Added] `CRecipeImpl` amorphous recipe init wrapper
- [Added] `VanillaSearch` new search function signature
- [Added] To `CustomCrafterAPI#registerRecipe`, recipe validation
- [Added] `CMatter` check function. `CMatter#isValidCMatter`
- [Added] `CRecipe` check function. `CRecipe#isValidCRecipe`
- [Changed] `Search#search` signatures
  - `items: List<ItemStack>` type changed to `Array<ItemStack>`
  - `inventory: Inventory` changed to `view: CraftView`
- [Deleted] Auto-Crafting settings, events, properties and more.

# 5.0.13-1
This version contains one change.
## Code
- [Added] Can control 'Custom Craft UI open ' enable or not.

# 5.0.13
This plugin changed to depend PaperAPI version from 1.21.3 to 1.21.4.
## Feature
- [Added] `cc get registered-recipe-names` command added. (Shows all registered recipe names.) 
## Code
- [Added] Added to set CustomCrafterAPI properties to default value commands.
- [Added] `CustomCrafterAPIPropertiesChangeEvent.PropertyKey#AUTO_CRAFTING_BASE_BLOCK` added.
- [Changed] `ResultSupplier` divided to `ResultSupplier` and `AutoCraftResultSupplier`.
  - [Changed] `ResultSupplier#func` renamed to `ResultSupplier#f`
  - [Changed] `AutoCraftRecipe#autoCraftResults` type changed. 
  - [Changed] `AutoCraftRecipeImpl#autoCraftResults` type changed.
  - [Changed] `ResultSupplier.NormalConfig` changed to `ResultSupplier.Config`
  - [Changed] `ResultSupplier.AutoCraftConfig` changed to `AutoCraftResultSupplier.Config`


# 5.0.12
## Code
- [Changed] `(Normal / AutoCraft)(Predicate / Consumer)` to `CRecipeContainer` and `CAutoCraftRecipeContainer`
- [Changed] `CustomCrafterAPIPropertiesChangeEvent` has come to accept async calls.

# 5.0.11
## Features
- [Added] an automatic crafting function.
- [Added] a command to get and change some of the properties of the CustomCrafterAPI (/cc).
- [Fixed] an issue with the multi-candidate display function.
## Code
- [Changed] some variables of the CustomCrafterAPI to be accessible via getters and setters.

- [Changed] the properties of AutoCraftRecipe.

- [Added] an event that is fired when some of the variables of the CustomCrafterAPI are changed (CustomCrafterAPIPropertiesChangeEvent).

## CI/CD
- [Fixed] Updated to support changes in the PaperMC Download API.

# 5.0.10-1
## Code
- [Fixed] CAssert log option arguments and a function
- [Added] CRecipeContainer.(Normal / AutoCraft)(Consumer / Predicate)
- [Changed] CRecipeContainer.Consumer / Predicate become marker interfaces
- [Deleted] CraftingGUIAccessor
## Document
- [Improved] Rewrite GitHub Wiki pages what are parts of fixed (ResultSupplier, CRecipeContainer and more...)
- [Added] GitHub Wiki pages what are added in 5.0.10*. (AutoCraftRecipe, ...)

## CI/CD
- [CI / Added] Test of Custom Crafter features on GitHub Actions

# 5.0.10
## Feature
- [Added] auto crafting
- [Fixed] vanilla recipe search
## Code
- [Added] An interface for auto crafting recipe feature added. (AutoCraftRecipe)
- [Added] An implementation class of AutoCraftRecipe added. (AutoCraftRecipeImpl)
- [Added] A class what is specified to auto crafting added to ResultSupplier config. (ResultSupplier#AutoCraftConfig)
- [Added] You can make custom implementation interfaces and classes to use ResultSupplier. ((interface) ResultSupplierConfig)
- [Changed] CRecipeContainer#Consumer has been begun an interface.
- [Added] Implementation classes of CRecipeContainer#Consumer added. (NormalConsumer and CraftingGUIAccessor)
- [Changed] ResultSupplier.Config moved to ResultSupplier#NormalConfig.
- [Changed] CAssert has been begun public from internal.
- [Changed] `api.object` to `api.objects`
- [Changed] Some classes what are named `*Impl`, them package moved to `impl` from `api.object`.
- [Changed] `api.processor` package moved to `impl.util`.
- [Changed] Search#search added new argument. (sourceRecipes)
- [Internal] GUI process all changed
- [Internal] DB accessor added
- [Internal] Some utilities removed

# 5.0.9
## Code
- [Added] Standard items used when there are no displayable items in the full candidate display function can now be set and retrieved.
- [Added] Default values have been set for the constructor of the default implementation class of CMatter.
- [Changed] Restrictions have been placed on the Material that can be set for BaseBlock.
- [Changed] ResultSupplier.Config can no longer be initialized by the user.
- [Changed] The recipeType in the CRecipeImpl constructor now comes before optional elements.
## Document
- [Added] GitHub Wiki