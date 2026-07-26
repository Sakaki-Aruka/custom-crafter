package io.github.sakaki_aruka.customcrafter.search

/**
 * A minimal residual-graph max-flow solver (Ford-Fulkerson with DFS augmenting paths).
 *
 * Sized for tiny graphs (crafting-grid scale, well under a few hundred nodes/edges), so a
 * plain DFS augmenting-path search is fast enough; no need for Dinic's or scaling variants.
 *
 * @param[nodeCount] Total number of nodes in the graph.
 * @since 5.3.0
 */
internal class FlowGraph(nodeCount: Int) {

    /**
     * A directed residual edge. [isReverse] marks the automatically-created companion edge of
     * some other forward edge; it is never subject to [maxFlow]'s `blocked` gate.
     */
    class Edge(
        val to: Int,
        var residual: Int,
        internal val reverseIndex: Int,
        val isReverse: Boolean
    )

    private val adjacency: Array<MutableList<Edge>> = Array(nodeCount) { mutableListOf() }

    /**
     * Adds a directed edge `from -> to` with the given capacity, plus its zero-capacity reverse
     * companion used internally to undo/reroute flow.
     *
     * @return The forward [Edge]. Its [Edge.residual] can be inspected after [maxFlow] runs:
     *   `capacity - residual` is the flow actually pushed through it.
     */
    fun addEdge(from: Int, to: Int, capacity: Int): Edge {
        val forward = Edge(to, capacity, adjacency[to].size, isReverse = false)
        val backward = Edge(from, 0, adjacency[from].size, isReverse = true)
        adjacency[from].add(forward)
        adjacency[to].add(backward)
        return forward
    }

    /** Edges outgoing from [node], forward and reverse alike. */
    fun edgesFrom(node: Int): List<Edge> = adjacency[node]

    /**
     * Computes the maximum flow from [source] to [sink].
     *
     * [blocked] lazily gates forward edges only (reverse/residual edges used to undo a previous
     * assignment are always traversable): return `true` to forbid traversing `from -> to` in this
     * attempt. This lets callers defer expensive checks (e.g. user predicates) until the search
     * actually needs that specific edge, and memoize results externally.
     *
     * @return The total flow value pushed from [source] to [sink].
     */
    fun maxFlow(source: Int, sink: Int, blocked: (from: Int, to: Int) -> Boolean = { _, _ -> false }): Int {
        var total = 0
        while (true) {
            val visited = BooleanArray(adjacency.size)
            val pushed = augment(source, sink, Int.MAX_VALUE, visited, blocked)
            if (pushed == 0) {
                break
            }
            total += pushed
        }
        return total
    }

    private fun augment(
        node: Int,
        sink: Int,
        incoming: Int,
        visited: BooleanArray,
        blocked: (from: Int, to: Int) -> Boolean
    ): Int {
        if (node == sink) {
            return incoming
        }
        visited[node] = true
        for (edge in adjacency[node]) {
            if (edge.residual <= 0 || visited[edge.to]) {
                continue
            }
            if (!edge.isReverse && blocked(node, edge.to)) {
                continue
            }
            val pushed = augment(edge.to, sink, minOf(incoming, edge.residual), visited, blocked)
            if (pushed > 0) {
                edge.residual -= pushed
                adjacency[edge.to][edge.reverseIndex].residual += pushed
                return pushed
            }
        }
        return 0
    }
}
