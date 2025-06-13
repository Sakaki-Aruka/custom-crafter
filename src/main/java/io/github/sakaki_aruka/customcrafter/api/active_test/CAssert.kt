package io.github.sakaki_aruka.customcrafter.api.active_test

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import kotlin.reflect.KClass

/**
 * Assertion utilities for Active-Test.
 */
object CAssert {
    val console = Bukkit.getConsoleSender()

    /**
     * Shows test (returns only Boolean)'s result, file name, method name and line number.
     * @param[result] A result of a test.
     * @since 5.0.10
     */
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

    /**
     * Shows test (throws an exception)'s result, file name, method name and line number.
     *
     * If a lambda expression does not throw no exception or unexpected exception, shows those info.
     *
     * ```kotlin
     * // example (APITest.kt at v5.0.10)
     * CAssert.assertThrow(IllegalArgumentException::class) {
     *     CustomCrafterAPI.getRandomNCoordinates(0)
     * }
     * // -> (Test) Successful: APITest.kt#randomCoordinatesTest (22)
     * ```
     * @param[exception] Expected (kotlin)exception class. (KClass)
     * @param[e] Exception throw lambda expression.
     * @since 5.0.10
     */
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