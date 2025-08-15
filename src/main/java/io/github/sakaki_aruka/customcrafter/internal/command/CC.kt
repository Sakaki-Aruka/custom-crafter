package io.github.sakaki_aruka.customcrafter.internal.command

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

object CC {
    /*
     * Custom Crafter Command (CC Command)
     *
     * - Get Part
     * Format: "${ELEMENT_NAME}: ${ELEMENT_VALUE}"
     *   - API_VERSION
     *   - IS_STABLE
     *   - IS_BETA
     *   - AUTHORS
     *   - RESULT_GIVE_CANCEL
     *   - BASE_BLOCK
     *   - AUTO_CRAFTING_BASE_BLOCK
     *   - USE_MULTIPLE_RESULT_CANDIDATE_FEATURE
     *   - USE_AUTO_CRAFTING_FEATURE
     *   - BASE_BLOCK_SIDE
     *   - AUTO_CRAFTING_BASE_BLOCK_SIDE (Internal API)
     *
     * - Set Part
     *   - RESULT_GIVE_CANCEL
     *   - BASE_BLOCK
     *   - USE_MULTIPLE_RESULT_CANDIDATE_FEATURE
     *   - USE_AUTO_CRAFTING_FEATURE
     *   - BASE_BLOCK_SIDE
     */

    private const val SINGLE_SUCCESS: Int = 1

    private fun CommandContext<CommandSourceStack>.msg(m: String) {
        this.source.sender.sendMessage(m.toComponent())
    }

    private val Get: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("get")
        .then(Commands.literal("api-version")
            .executes { ctx ->
                ctx.msg("API_VERSION: ${CustomCrafterAPI.API_VERSION}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("is-stable")
            .executes { ctx ->
                ctx.msg("IS_STABLE: ${CustomCrafterAPI.IS_STABLE}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("is-beta")
            .executes { ctx ->
                ctx.msg("IS_BETA: ${CustomCrafterAPI.IS_BETA}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("authors")
            .executes { ctx ->
                ctx.msg("AUTHORS: ${CustomCrafterAPI.AUTHORS.joinToString(", ")}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("result-give-cancel")
            .executes { ctx ->
                ctx.msg("RESULT_GIVE_CANCEL: ${CustomCrafterAPI.getResultGiveCancel()}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("base-block")
            .executes { ctx ->
                ctx.msg("BASE_BLOCK: ${CustomCrafterAPI.getBaseBlock().name}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("auto-crafting-base-block")
            .executes { ctx ->
                ctx.msg("AUTO_CRAFTING_BASE_BLOCK: ${CustomCrafterAPI.getAutoCraftingBaseBlock()}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("use-multiple-result-candidates-feature")
            .executes { ctx ->
                ctx.msg("USE_MULTIPLE_RESULT_CANDIDATES_FEATURE: ${CustomCrafterAPI.getUseMultipleResultCandidateFeature()}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("use-auto-crafting-feature")
            .executes { ctx ->
                ctx.msg("USE_AUTO_CRAFTING_FEATURE: ${CustomCrafterAPI.getUseAutoCraftingFeature()}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("base-block-side")
            .executes { ctx ->
                ctx.msg("BASE_BLOCK_SIDE: ${CustomCrafterAPI.getBaseBlockSideSize()}")
                return@executes SINGLE_SUCCESS
            }
        ).then(Commands.literal("auto-crafting-base-block-side")
            .executes { ctx ->
                ctx.msg("AUTO_CRAFTING_BASE_BLOCK_SIDE: ${InternalAPI.AUTO_CRAFTING_BASE_BLOCK_SIDE}")
                return@executes SINGLE_SUCCESS
            }
        )

    //     * - Set Part
    //     *   - RESULT_GIVE_CANCEL
    //     *   - BASE_BLOCK
    //     *   - USE_MULTIPLE_RESULT_CANDIDATE_FEATURE
    //     *   - USE_AUTO_CRAFTING_FEATURE
    //     *   - BASE_BLOCK_SIDE

    private val Set: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("set")
        .then(Commands.literal("result-give-cancel")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes { ctx ->
                    val v: Boolean = ctx.getArgument("value", Boolean::class.java)
                    CustomCrafterAPI.setResultGiveCancel(v)
                    ctx.msg("<green>result-give-cancel toggle successful. (${v})")
                    return@executes SINGLE_SUCCESS
                }
            )
        ).then(Commands.literal("base-block")
            .then(Commands.argument("block", ArgumentTypes.itemStack())
                .executes { ctx ->
                    val material: Material = ctx.getArgument("block", ItemStack::class.java).type
                    try {
                        CustomCrafterAPI.setBaseBlock(material)
                        ctx.msg("<green>base-block change successful. (${material.name})")
                    } catch (_: Exception) {
                        ctx.msg("<red>Failed to set base-block. 'block' must be a block.")
                    }
                    return@executes SINGLE_SUCCESS
                }
            )
        ).then(Commands.literal("use-multiple-result-candidate-feature")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes { ctx ->
                    val v: Boolean = ctx.getArgument("value", Boolean::class.java)
                    CustomCrafterAPI.setUseMultipleResultCandidateFeature(v)
                    ctx.msg("<green>use-multiple-result-candidate-feature toggle successful. (${v})")
                    return@executes SINGLE_SUCCESS
                }
            )
        ).then(Commands.literal("use-auto-crafting-feature")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes { ctx ->
                    val v: Boolean = ctx.getArgument("value", Boolean::class.java)
                    if (v) {
                        object: BukkitRunnable() {
                            override fun run() {
                                CustomCrafterAPI.setUseAutoCraftingFeature(true)
                                InternalAPI.setup()
                                if (v != CustomCrafterAPI.getUseAutoCraftingFeature()) {
                                    ctx.msg("<red>Failed to toggle use-auto-crafting-feature. See console logs.")
                                } else {
                                    ctx.msg("<green>use-auto-crafting-feature toggle successful. (${v})")
                                }
                            }
                        }.runTaskAsynchronously(CustomCrafter.getInstance())
                    } else {
                        CustomCrafterAPI.setUseAutoCraftingFeature(false)
                        ctx.msg("<green>use-auto-crafting-feature toggle successful. (${v})")
                    }
                    return@executes SINGLE_SUCCESS
                }
            )
        ).then(Commands.literal("base-block-side")
            .then(Commands.argument("size", IntegerArgumentType.integer(3))
                .executes { ctx ->
                    val size: Int = ctx.getArgument("size", Int::class.java)
                    if (CustomCrafterAPI.setBaseBlockSideSize(size)) {
                        ctx.msg("<green>base-block-side change successful. (${size}")
                    } else {
                        ctx.msg("<red>Failed to change base-block-side.")
                    }
                    return@executes SINGLE_SUCCESS
                }
            )
        )

    val command: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("cc")
        .then(Get)
        .then(Set)
}