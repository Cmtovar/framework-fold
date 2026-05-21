package com.profold.framework

object FrameworkData {
    const val ROWS = 4
    const val COLS = 4
    const val SLOTS_PER_ROW = 9

    val slotWords = mapOf(
        1 to "at", 2 to "to", 3 to "by", 4 to "in", 5 to "away",
        6 to "of", 7 to "out", 8 to "on", 9 to "up"
    )

    val cellNames = mapOf(
        "0,0" to "Adaptation",  "0,1" to "Rest",        "0,2" to "Diagnose",    "0,3" to "Itemize",
        "1,0" to "Attempt",     "1,1" to "Realization",  "1,2" to "Work",        "1,3" to "Mobilization",
        "2,0" to "Coverage",    "2,1" to "Showing Up",   "2,2" to "Friction",    "2,3" to "Travelling",
        "3,0" to "Maintain",    "3,1" to "Utilization",  "3,2" to "Rearrange",   "3,3" to "Switch Out"
    )

    val columnTitles = listOf("Exposure", "Abundance", "Investment", "Occlusion")

    val shades = mapOf(
        "0,0" to listOf(1,4,5,9),     "0,1" to listOf(1,4,8,9),
        "0,2" to listOf(1,6,7,8,9),   "0,3" to listOf(4,5,6,7,9),
        "1,0" to listOf(3,5,6,8,9),   "1,1" to listOf(2,5,7,8,9),
        "1,2" to listOf(2,3,7,9),     "1,3" to listOf(2,3,6,9),
        "2,0" to listOf(2,3,7,9),     "2,1" to listOf(2,3,6,9),
        "2,2" to listOf(3,5,6,8,9),   "2,3" to listOf(2,5,7,8,9),
        "3,0" to listOf(1,6,7,8,9),   "3,1" to listOf(4,5,6,7,9),
        "3,2" to listOf(1,4,5,9),     "3,3" to listOf(1,4,8,9)
    )

    val orders = mapOf(
        "0,0" to (listOf(4,1,5,4,9) to listOf(1,3)),
        "0,1" to (listOf(8,1,1,4,9) to listOf(0,3)),
        "0,2" to (listOf(8,7,1,6,9) to listOf(1,3)),
        "0,3" to (listOf(4,7,5,6,9) to listOf(0,1,3)),
        "1,0" to (listOf(3,8,6,5,9) to listOf(0,3)),
        "1,1" to (listOf(7,8,2,5,9) to listOf(1,3)),
        "1,2" to (listOf(7,3,2,2,9) to listOf(3)),
        "1,3" to (listOf(3,3,6,2,9) to listOf(2,3)),
        "2,0" to (listOf(2,2,7,3,9) to listOf(1,3)),
        "2,1" to (listOf(6,2,3,3,9) to listOf(0,3)),
        "2,2" to (listOf(6,5,3,8,9) to listOf(2)),
        "2,3" to (listOf(2,5,7,8,9) to listOf<Int>()),
        "3,0" to (listOf(1,6,8,7,9) to listOf(1,3)),
        "3,1" to (listOf(5,6,4,7,9) to listOf(1,2,3)),
        "3,2" to (listOf(5,4,4,1,9) to listOf(3)),
        "3,3" to (listOf(1,4,8,1,9) to listOf(2,3))
    )

    // Horizontal navigation: one-way cycle per column 0→2→3→1→0
    data class HorizontalMove(val target: Int, val isJump: Boolean)
    val horizontalNext = mapOf(
        0 to HorizontalMove(2, true),   // 0→2 jump
        2 to HorizontalMove(3, false),  // 2→3 step
        3 to HorizontalMove(1, true),   // 3→1 jump
        1 to HorizontalMove(0, false)   // 1→0 step
    )

    // Chute/ladder connections (bidirectional)
    val chuteDefinitions = listOf(
        intArrayOf(0,0, 3,2), intArrayOf(0,1, 3,3),
        intArrayOf(0,2, 3,0), intArrayOf(0,3, 3,1),
        intArrayOf(1,0, 2,2), intArrayOf(1,1, 2,3),
        intArrayOf(2,0, 1,2), intArrayOf(2,1, 1,3)
    )

    // Build chute lookup: cell index → partner index
    val chuteLookup: Map<Int, Int> by lazy {
        val map = mutableMapOf<Int, Int>()
        chuteDefinitions.forEach { c ->
            val from = c[0] * COLS + c[1]
            val to = c[2] * COLS + c[3]
            map[from] = to
            map[to] = from
        }
        map
    }

    // Connection types between cells
    enum class ConnectionType { STEP, JUMP, CHUTE }
    data class Connection(val from: Int, val to: Int, val type: ConnectionType, val directed: Boolean)

    val connections: List<Connection> by lazy {
        val list = mutableListOf<Connection>()
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                val idx = r * COLS + c
                // Vertical step (bidirectional)
                if (r < ROWS - 1) {
                    list.add(Connection(idx, idx + COLS, ConnectionType.STEP, false))
                }
                // Horizontal (directed, one-way)
                val next = horizontalNext[c]!!
                val toIdx = r * COLS + next.target
                list.add(Connection(idx, toIdx,
                    if (next.isJump) ConnectionType.JUMP else ConnectionType.STEP, true))
            }
        }
        // Chutes (bidirectional)
        chuteDefinitions.forEach { c ->
            val from = c[0] * COLS + c[1]
            val to = c[2] * COLS + c[3]
            list.add(Connection(from, to, ConnectionType.CHUTE, false))
        }
        list
    }

    // Edge type lookup for tree rendering
    val edgeTypes: Map<String, ConnectionType> by lazy {
        val map = mutableMapOf<String, ConnectionType>()
        connections.forEach { c ->
            map["${c.from}-${c.to}"] = c.type
            map["${c.to}-${c.from}"] = c.type
        }
        map
    }

    // BFS from source
    data class BfsResult(val dist: IntArray, val parents: Array<MutableList<Int>>)

    fun bfs(source: Int): BfsResult {
        val n = ROWS * COLS
        val dist = IntArray(n) { -1 }
        val parents = Array(n) { mutableListOf<Int>() }
        dist[source] = 0
        val queue = ArrayDeque<Int>()
        queue.add(source)
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            for (nb in getNeighbors(cur)) {
                if (dist[nb] == -1) {
                    dist[nb] = dist[cur] + 1
                    parents[nb].add(cur)
                    queue.add(nb)
                } else if (dist[nb] == dist[cur] + 1) {
                    parents[nb].add(cur)
                }
            }
        }
        return BfsResult(dist, parents)
    }

    private fun getNeighbors(idx: Int): List<Int> {
        val r = idx / COLS
        val c = idx % COLS
        val neighbors = mutableListOf<Int>()
        if (r > 0) neighbors.add(idx - COLS)
        if (r < ROWS - 1) neighbors.add(idx + COLS)
        val next = horizontalNext[c]!!
        neighbors.add(r * COLS + next.target)
        chuteLookup[idx]?.let { neighbors.add(it) }
        return neighbors
    }
}
