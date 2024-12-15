package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import kotlin.Exception
import kotlin.reflect.KClass

/**
 * @suppress
 */
internal object CAssert {
    val console = Bukkit.getConsoleSender()
    fun assertTrue(result: Boolean) {
        val stackTrace = Throwable().stackTrace
        stackTrace.getOrNull(1)?.let {
            val caller = stackTrace.getOrNull(1)!!
            val methodName = caller.methodName
            val fileName = caller.fileName
            val lineNum = caller.lineNumber
            val resultComponent = if (result) "<aqua>Successful</aqua>" else "<yellow>Failed    </yellow>"
            val line = MiniMessage.miniMessage().deserialize("(Test) $resultComponent: $fileName#$methodName ($lineNum)")
            console.sendMessage(line)
            //CustomCrafter.getInstance().logger.info()
        } ?: kotlin.run {
            console.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Failed to get caller."))
            //CustomCrafter.getInstance().logger.warning("Failed to get caller.")
        }
    }

    fun assertThrow(exception: Any, eClass: KClass<out Exception>) {
        val result = eClass.isInstance(exception)
        val stackTrace = Throwable().stackTrace
        stackTrace.getOrNull(1)?.let {
            val caller = stackTrace.getOrNull(1)!!
            val methodName = caller.methodName
            val fileName = caller.fileName
            val lineNum = caller.lineNumber
            val resultComponent = if (result) "<aqua>Successful</aqua>" else "<yellow>Failed    </yellow>"
            console.sendMessage(MiniMessage.miniMessage().deserialize("(Exception Test) $resultComponent: $fileName#$methodName ($lineNum)"))
            //CustomCrafter.getInstance().logger.info()
        } ?: kotlin.run {
            console.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Failed to get caller."))
            //CustomCrafter.getInstance().logger.warning("Failed to get caller.")
        }
    }
}