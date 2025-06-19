package io.github.sakaki_aruka.customcrafter.api.active_test

import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.writeLines
import kotlin.reflect.KClass

/**
 * Assertion utilities for Active-Test.
 */
object CAssert {
    private val console = Bukkit.getConsoleSender()
    private val LOGS: MutableList<String> = mutableListOf()

    /**
     * Flush stored logs to the specified path.
     *
     * (If the specified path is a regular file.)
     * @throws[IllegalArgumentException] Thrown if 'outputPath' is not a regular file.
     * @param[createIfNotExist] Creates a new log file if the specified path not exists. (Default = true)
     * @param[overrideIfExist] Override a log file if the specified path exists. (Default = false)
     * @param[forceClearLog] Force delete stored logs. (Even if thrown an exception or not flushed, will do.)
     * @since 5.0.10-1
     */
    fun flushStoredLog(
        outputPath: Path,
        createIfNotExist: Boolean = true,
        overrideIfExist: Boolean = false,
        forceClearLog: Boolean = false
    ) {
        var destination: Path = outputPath
        try {
            if (createIfNotExist && outputPath.notExists()) {
                destination = outputPath.createFile()
            } else if (overrideIfExist && outputPath.exists()) {
                outputPath.deleteIfExists()
                destination = outputPath.createFile()
            } else if (!overrideIfExist && outputPath.exists()) {
                InternalAPI.info("[CAssert LogFlush] Not flushed. ('outputPath' exists and 'overrideIfExist' is false.)")
                if (forceClearLog) {
                    LOGS.clear()
                }
                return
            }

            if (!destination.isRegularFile()) {
                throw IllegalArgumentException("'outputPath' must be a regular file. But '${outputPath.name}' is not.")
            }
        } catch (e: Exception) {
            InternalAPI.warn(e.message ?: "[CAssert LogFlush] Error")
            if (forceClearLog) {
                LOGS.clear()
            }
            return
        }

        destination.writeLines(LOGS, Charset.defaultCharset(), StandardOpenOption.APPEND)
        LOGS.clear()
    }

    /**
     * Shows test (returns only Boolean)'s result, file name, method name and line number.
     * ```kotlin
     * // example (APITest.kt at v5.0.10-1)
     * CAssert.assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(-1))
     * // -> (Test) Successful: APITest#baseBlockTest (file=APITest.kt/line=34)
     * ```
     * @param[result] A result of a test.
     * @param[logStoreInternal] Store logs to the internal log collector. (Default = false)
     * @since 5.0.10
     */
    fun assertTrue(
        result: Boolean,
        logStoreInternal: Boolean = InternalAPI.IS_GITHUB_ACTIONS
    ) {
        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk { stream ->
                stream.skip(2)
                    .findFirst()
                    .ifPresent { frame ->
                        val callerClassName = frame.declaringClass.simpleName
                        val callerMethodName = frame.methodName
                        val callerFileName = frame.fileName
                        val callerLineNumber = frame.lineNumber

                        val resultComponent = if (result) "<aqua>Successful</aqua>" else "<yellow>Failed    </yellow>"
                        val line = "(Test) $resultComponent: $callerClassName#$callerMethodName (file=$callerFileName/line=$callerLineNumber)"
                        val lineComponent = MiniMessage.miniMessage().deserialize(line)
                        console.sendMessage(lineComponent)

                        if (logStoreInternal) {
                            LOGS.add(line)
                        }
                    }
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
     * // example (APITest.kt at v5.0.10-1)
     * CAssert.assertThrow(IllegalArgumentException::class) {
     *     CustomCrafterAPI.getRandomNCoordinates(0)
     * }
     * // -> (Test) Successful: APITest#randomCoordinatesTest (file=APITest.kt/line=23)
     * ```
     * @param[exception] Expected (kotlin)exception class. (KClass)
     * @param[logStoreInternal] Store logs to the internal log collector. (Default = false)
     * @param[e] Exception throw lambda expression.
     * @since 5.0.10
     */
    fun assertThrow(
        exception: KClass<out Exception>,
        logStoreInternal: Boolean = InternalAPI.IS_GITHUB_ACTIONS,
        e: () -> Unit
    ) {

        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk { stream ->
                stream.skip(2)
                    .findFirst()
                    .ifPresent { frame ->
                        val callerClassName = frame.declaringClass.simpleName
                        val callerMethodName = frame.methodName
                        val callerFileName = frame.fileName
                        val callerLineNumber = frame.lineNumber

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
                        val line = "(Test) $resultComponent: $callerClassName#$callerMethodName (file=$callerFileName/line=$callerLineNumber)"
                        val lineComponent = MiniMessage.miniMessage().deserialize(line)
                        console.sendMessage(lineComponent)

                        if (logStoreInternal) {
                            LOGS.add(line)
                        }
                    }
            } ?: kotlin.run {
            console.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Failed to get caller."))
        }
    }
}