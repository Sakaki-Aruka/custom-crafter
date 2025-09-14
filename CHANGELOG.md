# Changelog

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