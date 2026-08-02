---
title: Advanced Debugging for Recipe Search
---

## What is Explainer?

`Explainer` is a diagnostic class that records what happened during a recipe search (available from 5.3.0 onwards).

While implementing recipes, you will run into the situation where a recipe *should* match but never appears in the results.
Because `Search.search` returns only the list of recipes that matched, the result tells you nothing about why the others did not — whether the recipe never became a candidate, whether an item type differed, or whether a predicate rejected it.

Passing an `Explainer` to a search method records each decision the search made, one line at a time, so you can read back afterwards exactly where things went wrong.

---

## Basic usage

Pass an instance to the `explainer` parameter of `Search.search` or `Search.asyncSearch`, then read the lines back with `getStringList()`.

```kotlin
val explainer = Explainer(Explainer.Loglevel.DEBUG, "debug")

val result: Search.SearchResult = Search.search(
    crafterId = player.uniqueId,
    view = view,
    sourceRecipes = listOf(myRecipe),
    explainer = explainer
)

explainer.getStringList().forEach { println(it) }
```

The `explainer` parameter defaults to `null`, and no recording work is performed at all when it is omitted.
Once you are done debugging, simply drop the argument (or pass `null`) to return to the original behaviour.

:::caution
`Explainer` is meant for debugging only. Recorded lines accumulate in memory indefinitely, so it is not designed for routine use in production or for a single instance to be reused over a long period.
:::

---

## Log levels

The first constructor parameter, `verbosity`, controls how much detail is recorded.

| Level | What gets recorded |
|-------|--------------------|
| `WARN` | Errors that nothing else catches. Used in very few places |
| `INFO` (default) | *What happened* — candidate filtering results, matched recipes, how the search finished |
| `DEBUG` | *What the values were* — the concrete values behind each individual decision |

`verbosity` acts as a **threshold**: only lines whose detail level is at or below it are recorded.
Specifying `Loglevel.INFO` records `INFO` and `WARN` while discarding `DEBUG`.

:::note
Most logging libraries assign larger numbers to higher severity, but `Explainer.Loglevel` is the opposite: **larger numbers mean more detail** (`WARN` = 1, `INFO` = 2, `DEBUG` = 3).
Think of it as specifying how finely you want things recorded.
:::

When tracking down a cause, specify `DEBUG`.

```kotlin
// record everything, down to the concrete values
val explainer = Explainer(Explainer.Loglevel.DEBUG, "debug")

// omitting the first parameter gives you INFO
val infoOnly = Explainer(name = "debug")
```

The second parameter, `name`, is a label for telling instances apart. It defaults to a random UUID.

---

## Reading the output

Each line has the form `[LEVEL] [function/check] message`.
The `function/check` prefix identifies exactly which check in which routine produced the line.

### Symptom 1: the recipe never becomes a candidate

The most common case is a recipe that never even enters the search.
A `CRecipe` is only considered when the input item count falls within `requiresInputItemAmountMin()` to `requiresInputItemAmountMax()`.

Here is the output for a recipe requiring two iron ingots when only one is placed.

```
[INFO] [search/candidateFilter] candidates filtered by input size: inputSize=1, sourceRecipes=1, candidates=0, shaped=0, shapeless=0
[DEBUG] [search/candidateFilter] excluded: name=iron_plate, type=SHAPELESS, inputSize=1 not in requires=2..2
[DEBUG] [VanillaSearch/search] Bukkit#getCraftingRecipe: world=World, found=true
[INFO] [search] search finished: matchedCustoms=0, vanillaFound=true
```

`candidates=0` shows there were no candidates, and the following `excluded` line gives the reason.
`inputSize=1 not in requires=2..2` means "the input has one item, but this recipe requires two".

:::tip
`excluded` lines are recorded at `DEBUG` only.
When a recipe seems completely unresponsive, switch to `DEBUG` first and check whether it was filtered out of the candidate list.
:::

### Symptom 2: a shapeless slot cannot be filled

Matching a shapeless recipe (`CRecipe.Type.SHAPELESS`) is solved as the problem of assigning input items to recipe slots without reusing an input.
When the assignment fails, the log records which slot could not be filled and why.

Here is the output for a recipe requiring two iron ingots when one iron ingot and one dirt are placed.

```
[INFO] [search/candidateFilter] candidates filtered by input size: inputSize=2, sourceRecipes=1, candidates=1, shaped=0, shapeless=1
[DEBUG] [search/candidateFilter] candidate(0): name=iron_plate, type=SHAPELESS, requires=2..2
[DEBUG] [shapeless/flowCheck] recipe=iron_plate, achieved=1, requiredMin=2
[DEBUG] [shapeless/flowCheck] recipe=iron_plate, group#1 unsatisfied: matched=0 < min=1, members=(1,0)
[DEBUG] [shapeless/flowCheck] recipe=iron_plate, slot=(1,0) unmatched: 1 acceptable input(s) [(0,0)] all taken by other slots
[DEBUG] [VanillaSearch/search] Bukkit#getCraftingRecipe: world=World, found=false
[INFO] [search] search finished: matchedCustoms=0, vanillaFound=false
```

This time the recipe did become a candidate (`candidates=1`), but only one of the two required assignments succeeded (`achieved=1, requiredMin=2`).

The last line gives the cause. The slot at `(1,0)` has one input that satisfies its conditions, but that input is at `(0,0)` and has already been taken by another slot.
In other words, you are one iron ingot short.

Three reasons can be reported for an unfilled slot.

| Output | Meaning |
|--------|---------|
| `no input passes candidate=[...] amount>=N` | No input satisfies the type or amount requirement at all |
| `N input(s) pass candidate/amount>=N but all rejected by matter predicates` | Type and amount passed, but every `CMatterPredicate` rejected them |
| `N acceptable input(s) [...] all taken by other slots` | Acceptable inputs exist, but they are already assigned to other slots |

### Symptom 3: a predicate is rejecting the input

When a `CMatterPredicate` or `CRecipePredicate` is responsible, the log records which one rejected it.

```
[DEBUG] [shaped/recipePredicateCheck] recipe=reinforced_ingot, failed=predicate#1/2
```

`predicate#1/2` means "index 1 (the second) of the two predicates rejected the input".
Naming a predicate makes it appear by name instead of by index — see [Naming predicates](#naming-predicates) below.

### When the search succeeds

On a match, the finalised coordinate mapping is recorded at `INFO` level.

```
[INFO] [search/candidateFilter] candidates filtered by input size: inputSize=2, sourceRecipes=1, candidates=1, shaped=0, shapeless=1
[INFO] [shapeless] recipe matched: name=iron_plate, relation=(0,0)->(1,0), (1,0)->(0,0)
[INFO] [search] search finished: matchedCustoms=1, vanillaFound=false
```

`relation` maps `recipeCoordinate->inputCoordinate`.
In the example above, the recipe's `(0,0)` was assigned the input at `(1,0)`, and the recipe's `(1,0)` was assigned the input at `(0,0)`.
Since shapeless recipes may reorder assignments, this is useful for confirming that the mapping is what you intended.

---

## Naming predicates

A Kotlin lambda carries no identity of its own, so by default it can only be shown by index, as in `predicate#1/2`.
Wrapping it with `CMatterPredicates.named` or `CRecipePredicates.named` makes it appear by name.

```kotlin
val undamaged = CMatterPredicates.named("undamaged") { ctx ->
    ctx.input.amount >= 64
}
```

Here is the output when that predicate rejects the input.

```
[DEBUG] [shaped/matterPredicateCheck] recipe=reinforced_ingot, coordinate=(0,0), failed=undamaged (predicate#0/1)
```

Predicates composed with combinators such as `and` have their names composed automatically as well.

```kotlin
val combined = CMatterPredicates.named("isIron") { /* ... */ }
    .and(CMatterPredicates.named("isFullStack") { /* ... */ })
```

```
[DEBUG] [shaped/matterPredicateCheck] recipe=reinforced_ingot, coordinate=(0,0), failed=(isIron and isFullStack) (predicate#0/1)
```

A composed predicate is treated as a single unit, so the log cannot tell you which of the inner predicates rejected the input.
If you need that level of detail, list them as separate predicates instead of composing them.

For details on `named` and the combinators, see [About CRecipePredicate](/en/recipe/predicate/).

---

## Writing logs from inside a predicate

`CMatterPredicate.Context` and `CRecipePredicate.Context` both expose an `explainer` field (available from 5.3.0 onwards).
Use it to let your own predicate record why it rejected an input.

```kotlin
val fullStack = CMatterPredicate { ctx ->
    val ok = ctx.input.amount >= 64
    ctx.explainer?.writeLog(
        Explainer.Loglevel.DEBUG,
        "full stack check: amount=${ctx.input.amount}, required=64, passed=$ok"
    )
    ok
}
```

```
[DEBUG] full stack check: amount=1, required=64, passed=false
[DEBUG] [shaped/matterPredicateCheck] recipe=reinforced_ingot, coordinate=(0,0), failed=predicate#0/1
```

`explainer` is non-null only when one was passed to the search.
Always use `?.` so that nothing is recorded when no explainer is in use.

:::tip
Keep each line to a single line and include the values the decision was based on.
The point is to record not just *that* the input was rejected, but *what it was compared against*.
:::

---

## Retrieving the records

### getLogs

Returns the `Log` objects in the order they were recorded.
Each has a `level` and a `line` field, which is useful when you want to format them yourself.

```kotlin
explainer.getLogs().forEach { log ->
    if (log.level == Explainer.Loglevel.WARN) {
        plugin.logger.warning(log.line)
    }
}
```

### getStringList

Returns the records formatted as `[LEVEL] message`.
Passing levels as arguments narrows the result down to those levels.

```kotlin
// retrieve everything
val all: List<String> = explainer.getStringList()

// only INFO (when you just want the overall flow)
val infoOnly: List<String> = explainer.getStringList(Explainer.Loglevel.INFO)

// multiple levels can be specified
val some: List<String> = explainer.getStringList(
    Explainer.Loglevel.INFO,
    Explainer.Loglevel.WARN
)
```

:::note
The filtering done by `getStringList` behaves differently from the constructor's `verbosity`.
`verbosity` is a threshold that records everything at or below the given detail level, whereas `getStringList` returns **only the levels that match exactly**.
This is what lets `getStringList(Loglevel.WARN)` pull out warnings alone.
:::

---

## Notes on asynchronous search

`Explainer` is thread-safe, and no lines are lost even when several worker threads from `asyncSearch` write to it simultaneously.

However, `asyncSearch` evaluates candidate recipes in parallel, so **lines from different recipes are interleaved**.
Every line contains `recipe=`, so filter on it when you want to follow a single recipe.

```kotlin
val future = Search.asyncSearch(
    crafterId = player.uniqueId,
    view = view,
    explainer = explainer
)

future.thenAccept {
    explainer.getStringList()
        .filter { it.contains("recipe=my_recipe") }
        .forEach { println(it) }
}
```

The recording order faithfully reflects the actual execution order, so it can also be used to follow the order in which work proceeded.

:::caution
When `SearchMode.ONLY_FIRST` is used, the remaining search tasks are interrupted as soon as the first match is found.
An interrupted recipe fails for reasons unrelated to its actual conditions, and this is recorded as such.

```
[INFO] [shapeless/flowCheck] recipe=..., matching abandoned: async context interrupted
```

Do not analyse the failure reasons of any recipe showing this line.
Use `SearchMode.ALL` when investigating a cause.
:::

---

## Supported search methods

| Method | Supported |
|--------|-----------|
| `Search.search` | Yes |
| `Search.asyncSearch` | Yes |
| `PartialSearch.asyncPartialSearch` | Yes |
| `VanillaSearch.search` | Yes |

Note that the standard CustomCrafterAPI crafting screen (`CraftUI`) provides no route for passing an `Explainer`.
The standard screen performs fixed work that does not vary with recipe contents, so a problem occurring there is a bug in the library rather than in your code.
To verify how a recipe behaves, call the search methods above directly.
