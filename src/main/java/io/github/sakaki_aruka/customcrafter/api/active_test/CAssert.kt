package io.github.sakaki_aruka.customcrafter.api.active_test

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
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
        } ?: kotlin.run {
            console.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Failed to get caller."))
        }
    }

    fun assertThrow(exception: KClass<out Exception>, e: () -> Unit) {
        val stackTrace = Throwable().stackTrace
        stackTrace.getOrNull(1)?.let { trace ->
            val methodName = trace.methodName
            val fileName = trace.fileName
            val lineNum = trace.lineNumber
            val resultComponent = try {
                e()
                "<yellow>Failed (No Exception caught)</yellow>"
            } catch (e: Exception) {
                if (e::class == exception) {
                    "<aqua>Successful</aqua>"
                } else {
                    "<yellow>Failed (Unexpected exception caught / ${e::class.simpleName})</yellow>"
                }
            }
            console.sendMessage(MiniMessage.miniMessage().deserialize("(Test) $resultComponent: $fileName#$methodName ($lineNum)"))
        }
    }
}