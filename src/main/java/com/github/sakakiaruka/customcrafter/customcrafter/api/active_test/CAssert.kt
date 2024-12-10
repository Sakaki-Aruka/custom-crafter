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
            val methodName = caller.methodName
            val fileName = caller.fileName
            val lineNum = caller.lineNumber
            val resultString = if (result) "Successful" else "Failed"
            CustomCrafter.getInstance().logger.info("(Test) $fileName#$methodName ($lineNum) $resultString.")
        } ?: kotlin.run {
            CustomCrafter.getInstance().logger.warning("Failed to get caller.")
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
            val resultString = if (result) "Successful" else "Failed"
            CustomCrafter.getInstance().logger.info("(Exception Test) $fileName#$methodName ($lineNum) $resultString.")
        } ?: kotlin.run {
            CustomCrafter.getInstance().logger.warning("Failed to get caller.")
        }
    }
}