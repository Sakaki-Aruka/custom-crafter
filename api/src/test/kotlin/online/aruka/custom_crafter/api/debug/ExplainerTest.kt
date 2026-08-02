package online.aruka.custom_crafter.api.debug

import io.github.sakaki_aruka.customcrafter.debug.Explainer
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object ExplainerTest {

    @Test
    fun writeLogRejectsMoreVerboseThanConfigured() {
        // configured verbosity = INFO -> DEBUG (more verbose) must be rejected
        val explainer = Explainer(Explainer.Loglevel.INFO, "t")
        explainer.writeLog(Explainer.Loglevel.DEBUG, "debug message")
        assertTrue(explainer.getLogs().isEmpty())
    }

    @Test
    fun writeLogAcceptsSameOrLessVerboseThanConfigured() {
        // configured verbosity = INFO -> INFO (same) and WARN (less verbose) must be accepted
        val explainer = Explainer(Explainer.Loglevel.INFO, "t")
        explainer.writeLog(Explainer.Loglevel.INFO, "info message")
        explainer.writeLog(Explainer.Loglevel.WARN, "warn message")

        val logs = explainer.getLogs()
        assertEquals(2, logs.size)
        assertEquals(Explainer.Loglevel.INFO, logs[0].level)
        assertEquals(Explainer.Loglevel.WARN, logs[1].level)
    }

    @Test
    fun omittedVerbosityDefaultsToInfo() {
        // no verbosity passed -> behaves as if INFO was configured
        val explainer = Explainer(name = "t")
        explainer.writeLog(Explainer.Loglevel.DEBUG, "debug message")
        explainer.writeLog(Explainer.Loglevel.INFO, "info message")
        explainer.writeLog(Explainer.Loglevel.WARN, "warn message")

        val logs = explainer.getLogs()
        assertEquals(2, logs.size)
        assertEquals(Explainer.Loglevel.INFO, logs[0].level)
        assertEquals(Explainer.Loglevel.WARN, logs[1].level)
    }

    @Test
    fun omittedNameDefaultsToRandomUuid() {
        // instances must be distinguishable without the caller naming them
        assertTrue(Explainer().name.isNotEmpty())
        assertTrue(Explainer().name != Explainer().name)
    }

    @Test
    fun getLogsPreservesInsertionOrder() {
        // configured verbosity = DEBUG (most verbose) -> every level passes through
        val explainer = Explainer(Explainer.Loglevel.DEBUG, "t")
        explainer.writeLog(Explainer.Loglevel.INFO, "first")
        explainer.writeLog(Explainer.Loglevel.DEBUG, "second")
        explainer.writeLog(Explainer.Loglevel.WARN, "third")
        explainer.writeLog(Explainer.Loglevel.INFO, "fourth")

        val logs = explainer.getLogs()
        assertEquals(4, logs.size)
        assertEquals("first" to Explainer.Loglevel.INFO, logs[0].line to logs[0].level)
        assertEquals("second" to Explainer.Loglevel.DEBUG, logs[1].line to logs[1].level)
        assertEquals("third" to Explainer.Loglevel.WARN, logs[2].line to logs[2].level)
        assertEquals("fourth" to Explainer.Loglevel.INFO, logs[3].line to logs[3].level)
    }

    @Test
    fun getStringListRendersEveryLevelWhenNoTargetGiven() {
        val explainer = Explainer(Explainer.Loglevel.DEBUG, "t")
        explainer.writeLog(Explainer.Loglevel.INFO, "first")
        explainer.writeLog(Explainer.Loglevel.DEBUG, "second")

        assertEquals(listOf("[INFO] first", "[DEBUG] second"), explainer.getStringList())
    }

    @Test
    fun getStringListFiltersByExactLevel() {
        // unlike the write-time threshold, this keeps only the levels asked for
        val explainer = Explainer(Explainer.Loglevel.DEBUG, "t")
        explainer.writeLog(Explainer.Loglevel.INFO, "info message")
        explainer.writeLog(Explainer.Loglevel.DEBUG, "debug message")
        explainer.writeLog(Explainer.Loglevel.WARN, "warn message")

        assertEquals(listOf("[WARN] warn message"), explainer.getStringList(Explainer.Loglevel.WARN))
        assertEquals(
            listOf("[INFO] info message", "[DEBUG] debug message"),
            explainer.getStringList(Explainer.Loglevel.INFO, Explainer.Loglevel.DEBUG)
        )
    }

    @Test
    fun concurrentWritesLoseNoLines() {
        // asyncSearch writes to one instance from every worker thread
        val explainer = Explainer(Explainer.Loglevel.DEBUG, "t")
        val threads = 8
        val perThread = 2000
        val tasks = (0..<threads).map { t ->
            CompletableFuture.runAsync {
                repeat(perThread) { i -> explainer.writeLog(Explainer.Loglevel.INFO, "t$t-$i") }
            }
        }
        CompletableFuture.allOf(*tasks.toTypedArray()).join()

        assertEquals(threads * perThread, explainer.getLogs().size)
    }
}
