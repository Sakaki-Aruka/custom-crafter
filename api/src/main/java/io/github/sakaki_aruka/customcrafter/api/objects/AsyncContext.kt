package io.github.sakaki_aruka.customcrafter.api.objects

import java.util.concurrent.atomic.AtomicBoolean

/**
 * AsyncContext provides states of async
 *
 * It is recommended that async processes check for interruptions periodically.
 *
 * @param[interrupted] Interruption status. (True: Interrupted, False: Not interrupted)
 * @since 5.0.20
 */
class AsyncContext(
    interrupted: Boolean
) {
    private val interrupted: AtomicBoolean = AtomicBoolean(interrupted)

    companion object {
        @JvmStatic
        fun ofTurnOff(): AsyncContext = AsyncContext(false)
    }

    /**
     * Returns interrupted or not.
     * Async operation implementors should poll this periodically and return early when true to support cooperative cancellation.
     * @return[Boolean] Interrupted or not
     * @since 5.0.20
     */
    fun isInterrupted(): Boolean = interrupted.get()

    internal fun interrupt() {
        interrupted.set(true)
    }
}