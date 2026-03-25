---
title: GroupRecipe
---

## What is GroupRecipe

`GroupRecipe` is a `CRecipe` implementation class separate from `CRecipeImpl` that provides a **shaped recipe with flexible control over the minimum number of placed items by grouping slots together**.

In a normal shaped recipe every slot is required, but `GroupRecipe` lets you combine multiple slots into a single `Context` and set a condition such as "at least N slots in this group must have an item placed in them".

Additionally, by using a `GroupRecipe.Matter` that includes `Material.AIR` as a candidate, you can create item conditions that allow empty slots.

:::note
`GroupRecipe` only supports `CRecipe.Type.SHAPED`. `SHAPELESS` cannot be used.
:::

---

## Constructor

```kotlin
open class GroupRecipe @JvmOverloads constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    val groups: Set<Context>,
    override val predicates: List<CRecipePredicate>? = listOf(recipePredicate),
    override val results: List<ResultSupplier>? = null,
    override val type: CRecipe.Type = CRecipe.Type.SHAPED
) : CRecipe
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `name` | — | Identifier name for the recipe |
| `items` | — | Mapping of coordinates to item conditions |
| `groups` | — | Slot grouping configuration (a set of `Context` instances) |
| `predicates` | `listOf(recipePredicate)` | Must include `GroupRecipe.recipePredicate` |
| `results` | `null` | List of `ResultSupplier` instances that produce the output |
| `type` | `SHAPED` | Always `CRecipe.Type.SHAPED` |

:::caution
The default value for `predicates` is `listOf(GroupRecipe.recipePredicate)`.
When adding custom `predicates`, make sure to always include `recipePredicate`.
Omitting `recipePredicate` will cause the minimum placement count check for groups to stop working.
:::

---

## GroupRecipe.Context

`Context` is a class that represents the grouping configuration for slots.

| Field | Type | Description |
|-------|------|-------------|
| `members` | `Set<CoordinateComponent>` | The set of slot coordinates belonging to this group |
| `min` | `Int` | The minimum number of slots that must have an item placed during crafting |
| `name` | `String` | Group name (defaults to a random UUID) |

### Context.of()

A factory method that creates a `Context` with validation.

```kotlin
GroupRecipe.Context.of(
    members: Set<CoordinateComponent>,
    min: Int,
    name: String = UUID.randomUUID().toString()
): Context
```

Throws `IllegalArgumentException` if `members` is empty or `min` is less than 0.

### Context.default()

Creates a `Context` with default settings (`min = 1`) for a single coordinate.
Used internally to fill in coordinates that do not need explicit grouping.

```kotlin
GroupRecipe.Context.default(coordinate: CoordinateComponent): Context
// → members: {coordinate}, min: 1
```

### Context.isValidGroups()

A method that checks whether a set of `Context` instances is consistent with `items`.

```kotlin
GroupRecipe.Context.isValidGroups(
    groups: Set<Context>,
    items: Map<CoordinateComponent, CMatter>
): Result<Unit>
```

Fails in the following cases:
- A `Context` with an empty `members` set exists
- A coordinate in `members` does not exist as a key in `items`
- The same coordinate appears in multiple `Context` instances (duplicate)
- The number of Air-permitting Matter instances inside a group is insufficient (requires `members.size - min`)
- The Matter at the minimum-index coordinate in `items` includes `Material.AIR` as a candidate

---

## GroupRecipe.Matter

`GroupRecipe.Matter` is a special implementation of `CMatter` that can include `Material.AIR` as a candidate.

A standard `CMatterImpl` cannot include Air as a candidate, but `GroupRecipe.Matter` allows Air in order to represent optional slots within a group.

### Matter.of()

A factory method that creates a `Matter` from an existing `CMatter`.

```kotlin
GroupRecipe.Matter.of(
    matter: CMatter,
    includeAir: Boolean = false
): Matter
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `matter` | — | The base CMatter |
| `includeAir` | `false` | Whether to add `Material.AIR` as a candidate |

Throws `IllegalArgumentException` if `matter` is already a `GroupRecipe.Matter`.

The generated `Matter` internally uses a `CMatterPredicate` called `originalChecker`, which
delegates to the original `CMatter`'s `predicates` check when the placed item is not Air.

---

## createGroups() Helper

`GroupRecipe.createGroups()` is a utility method that fills in missing group configurations.
It automatically creates and adds a default `Context` for any coordinate not already covered by the supplied `missingGroups`.

```kotlin
GroupRecipe.createGroups(
    items: Map<CoordinateComponent, CMatter>,
    missingGroups: Set<Context>
): Set<Context>
```

Internally calls `Context.isValidGroups()` for validation and throws an exception if any invalid `Context` is found.

---

## recipePredicate

`GroupRecipe.recipePredicate` is the `CRecipePredicate` responsible for checking the groups in a `GroupRecipe`.

Processing flow:
1. If the `CRecipe` cannot be cast to `GroupRecipe`, returns `true`
2. Calculates the offset (dx, dy) between the recipe and the input
3. Identifies the group corresponding to each coordinate and counts the number of items placed
4. Verifies that "actual placement count >= `min`" holds for every group

---

## Constraints

GroupRecipe has the following constraints:

1. **The Matter at the minimum coordinate must not allow Air** — The `CMatter` corresponding to the coordinate with the smallest `CoordinateComponent#toIndex()` value in `items` cannot include `Material.AIR` as a candidate. This coordinate serves as the anchor (reference point) of the recipe.

2. **Number of Air-permitting Matter instances per group** — The number of slots that can be made optional (skippable) within a group is `members.size - min`. Therefore, at least `members.size - min` Matter instances that accept Air as a candidate are required within the group.

3. **No duplicate coordinates** — A single coordinate cannot belong to more than one `Context`.

4. **SHAPED only** — `CRecipe.Type.SHAPELESS` is not supported.

---

## Usage Examples

### Recipe with Optional Slots

An example of defining a flexible recipe where "1 stone is required, and up to 3 additional slots can optionally hold stone to increase the output".

```kotlin
val stone = CMatterImpl.of(Material.STONE)
// Matter that includes Air (for optional slots)
val optionalStone = GroupRecipe.Matter.of(stone, includeAir = true)

// (0,0) is mandatory (Air not allowed)
// (1,0), (2,0), (0,1) are optional (0 or more required)
val items = mapOf(
    CoordinateComponent(0, 0) to stone,
    CoordinateComponent(1, 0) to optionalStone,
    CoordinateComponent(2, 0) to optionalStone,
    CoordinateComponent(0, 1) to optionalStone
)

val mandatoryGroup = GroupRecipe.Context.of(
    members = setOf(CoordinateComponent(0, 0)),
    min = 1,
    name = "mandatory"
)

val optionalGroup = GroupRecipe.Context.of(
    members = setOf(
        CoordinateComponent(1, 0),
        CoordinateComponent(2, 0),
        CoordinateComponent(0, 1)
    ),
    min = 0,  // optional — 0 or more
    name = "optional"
)

val recipe = GroupRecipe(
    name = "flexible-stone",
    items = items,
    groups = setOf(mandatoryGroup, optionalGroup),
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.COBBLESTONE)))
)
```

### Concise Description Using createGroups()

Using `createGroups()` is convenient for automatically filling in slots that have not yet been assigned to a group.

```kotlin
val stone = CMatterImpl.of(Material.STONE)
val optionalStone = GroupRecipe.Matter.of(stone, includeAir = true)

val items = mapOf(
    CoordinateComponent(0, 0) to stone,           // mandatory
    CoordinateComponent(1, 0) to optionalStone,   // optional
    CoordinateComponent(0, 1) to optionalStone    // optional
)

// Only define the optional slot group manually; the rest is filled in automatically
val optionalGroup = GroupRecipe.Context.of(
    members = setOf(CoordinateComponent(1, 0), CoordinateComponent(0, 1)),
    min = 1,  // at least 1 out of 2 slots
    name = "optional"
)

val groups = GroupRecipe.createGroups(items, setOf(optionalGroup))
// A default Context (min=1) is automatically added for (0,0)

val recipe = GroupRecipe(
    name = "stone-optional",
    items = items,
    groups = groups,
    results = listOf(ResultSupplier.timesSingle(ItemStack.of(Material.COBBLESTONE)))
)
```

### Adding Custom predicates

When adding extra validation while keeping `GroupRecipe.recipePredicate`, include it in the list.

```kotlin
val recipe = GroupRecipe(
    name = "op-group-recipe",
    items = items,
    groups = groups,
    predicates = listOf(
        GroupRecipe.recipePredicate,  // required: group check
        CRecipePredicate { ctx -> ctx.player?.isOp ?: false }  // OP only
    ),
    results = listOf(ResultSupplier.single(ItemStack.of(Material.DIAMOND)))
)
```
