package io.github.sakaki_aruka.customcrafter.debug

import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A diagnostic recorder for a single search run.
 *
 * An instance collects why a search reached its result: which recipes became candidates, which
 * check rejected a recipe, and which predicate failed. Pass one to
 * [io.github.sakaki_aruka.customcrafter.search.Search.search] (or its async / partial counterparts)
 * and read the collected lines back with [getLogs] or [getStringList].
 *
 * Lines are recorded only when their level is not more verbose than [verbosity]:
 * [Loglevel.WARN] alone keeps errors, [Loglevel.INFO] adds what happened, and [Loglevel.DEBUG]
 * adds the concrete values behind each decision.
 *
 * This class is safe to share across threads.
 * [io.github.sakaki_aruka.customcrafter.search.Search.asyncSearch] evaluates candidate recipes in
 * parallel and writes to one instance from every worker, so lines from different recipes interleave.
 * Every line carries the recipe it belongs to, and the recorded order is a faithful trace of the
 * actual execution order.
 *
 * @param[verbosity] The most verbose level to keep. (default = [Loglevel.INFO])
 * @param[name] Label for this instance. Only used to tell instances apart. (default = a random UUID)
 * @since 5.3.0
 */
class Explainer @JvmOverloads constructor(
    val verbosity: Loglevel = Loglevel.INFO,
    val name: String = UUID.randomUUID().toString()
) {
    private val logs: ConcurrentLinkedQueue<Log> = ConcurrentLinkedQueue()

    /**
     * A single recorded line.
     *
     * @param[level] Level this line was written at
     * @param[line] Message body
     * @since 5.3.0
     */
    class Log(val level: Loglevel, val line: String) {
        override fun toString(): String = "[$level] $line"
    }

    /**
     * Severity of a recorded line.
     *
     * [Loglevel.verbosity] grows as the level gets more detailed, so a level is kept when its
     * [Loglevel.verbosity] is less than or equal to the [Explainer.verbosity] threshold. Note this
     * is the inverse of the "higher is more severe" convention used by most logging frameworks.
     *
     * @param[verbosity] How detailed this level is. Larger means more detailed.
     * @since 5.3.0
     */
    enum class Loglevel(val verbosity: Int) {
        /** Errors that no other code catches. Rarely used. */
        WARN(1),

        /** What happened as a result of a step. */
        INFO(2),

        /** The concrete values a decision was made from. */
        DEBUG(3)
    }

    /**
     * Records a line when [level] is not more verbose than [verbosity].
     *
     * Callable from a [io.github.sakaki_aruka.customcrafter.matter.CMatterPredicate] or a
     * [io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicate] through the explainer exposed
     * on their context, so a predicate can record why it rejected an input.
     *
     * @param[level] Level of this line
     * @param[line] Message body. Keep it on one line.
     * @since 5.3.0
     */
    fun writeLog(level: Loglevel, line: String) {
        if (level.verbosity > this.verbosity.verbosity) {
            return
        }
        this.logs.add(Log(level, line))
    }

    /**
     * Returns every recorded line in the order it was written.
     *
     * @return[List] Recorded lines
     * @since 5.3.0
     */
    fun getLogs(): List<Log> = this.logs.toList()

    /**
     * Returns recorded lines rendered as `"[LEVEL] message"`, keeping only [targetLevels].
     *
     * Unlike the [verbosity] threshold applied when writing, this filters by exact level, so
     * `getStringList(Loglevel.WARN)` returns warnings alone. Passing no level returns everything.
     *
     * @param[targetLevels] Levels to keep. (default = every level)
     * @return[List] Rendered lines
     * @since 5.3.0
     */
    fun getStringList(vararg targetLevels: Loglevel): List<String> {
        val targets: Set<Loglevel> = targetLevels.toSet()
            .takeIf { it.isNotEmpty() }
            ?: Loglevel.entries.toSet()
        return this.logs
            .filter { log -> log.level in targets }
            .map { log -> log.toString() }
    }

    override fun toString(): String = "Explainer(name=$name, verbosity=$verbosity, logs=${logs.size})"
}
