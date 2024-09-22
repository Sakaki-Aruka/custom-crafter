package com.github.sakakiaruka.customcrafter.customcrafter.command

import com.github.sakakiaruka.customcrafter.customcrafter.util.HistoryUtil
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.UUID

object HistoryDatabase: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        /*
         *
         * > /history_database all ['name' or 'uuid']
         * > /history_database unique ['name' or 'uuid']
         */

        if (!sender.hasPermission("cc.op")) return false
        if (args == null || args.size != 2) {
            sender.sendMessage("/history_database (all|unique) ['name' or 'uuid']")
            return false
        }

        val player: OfflinePlayer = try {
            val id: UUID = UUID.fromString(args[1])
            Bukkit.getOfflinePlayer(id)
        } catch (e: Exception) {
            Bukkit.getOfflinePlayer(args[1])
        }.takeIf { it.firstPlayed > 0 } ?: run {
            sender.sendMessage("The specified player has not yet joined this server.")
            return false
        }

        sender.sendMessage(HistoryUtil.convertToJsonList(
            when (args[0]) {
                "all" -> HistoryUtil.getPlayerCraftedHistoryAll(player.uniqueId)
                "unique" -> HistoryUtil.getPlayerCreatedHistoryUnique(player.uniqueId)
                else -> run {
                    sender.sendMessage("'${args[0]}' is an illegal option.")
                    return false
                }
            }
        ).joinToString(System.lineSeparator()))

        return true
    }
}