package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter
import kotlin.Exception
import kotlin.reflect.KClass

/**
 * @suppress
 */
internal object CAssert {
    fun assertTrue(result: Boolean) {
        val stackTrace = Throwable().stackTrace
        stackTrace.getOrNull(1)?.let {
            val caller = stackTrace.getOrNull(1)!!
            val fileName = caller.fileName
            val lineNum = caller.lineNumber
            CustomCrafter.getInstance().logger.info("Test from $fileName-$lineNum ${if (result) "Successful" else "Failed"}.")
        } ?: kotlin.run {
            CustomCrafter.getInstance().logger.warning("Failed to get caller.")
        }
    }

    fun assertThrow(exception: Any, eClass: KClass<out Exception>) {
        val result = eClass.isInstance(exception)
        val stackTrace = Throwable().stackTrace
        stackTrace.getOrNull(1)?.let {
            val caller = stackTrace.getOrNull(1)!!
            val fileName = caller.fileName
            val lineNum = caller.lineNumber
            CustomCrafter.getInstance().logger.info("Exception Test from $fileName-$lineNum ${if (result) "Successful" else "Failed"}.")
        } ?: kotlin.run {
            CustomCrafter.getInstance().logger.warning("Failed to get caller.")
        }
    }
}