package com.profold.framework

import androidx.compose.ui.graphics.Color

data class FrameworkCell(
    val row: Int,
    val col: Int,
    val name: String,
    val slots: List<Int>,
    val path: List<Int>,
    val arcs: List<Int>
)

object FrameworkData {
    val slotWords = mapOf(
        1 to "at", 2 to "to", 3 to "by", 4 to "in", 5 to "away",
        6 to "of", 7 to "out", 8 to "on", 9 to "up"
    )

    val slotColors = mapOf(
        1 to Color(0xFFFA8B8B),  // at - red
        2 to Color(0xFFFCA55A),  // to - orange
        3 to Color(0xFFFCCB38),  // by - yellow
        4 to Color(0xFF65E796),  // in - green
        5 to Color(0xFFF68DC5),  // away - pink
        6 to Color(0xFF939EF9),  // of - indigo
        7 to Color(0xFFB6A0FC),  // out - purple
        8 to Color(0xFF45DFCA),  // on - teal
        9 to Color(0xFF79B5FB),  // up - blue
    )

    val columnTitles = listOf("Exposure", "Abundance", "Investment", "Occlusion")

    val cellNames = mapOf(
        "0,0" to "Adaptation",
        "0,1" to "Rest",
        "0,2" to "Diagnose",
        "0,3" to "Itemize",
        "1,0" to "Attempt",
        "1,1" to "Realization",
        "1,2" to "Work",
        "1,3" to "Mobilization",
        "2,0" to "Coverage",
        "2,1" to "Showing Up",
        "2,2" to "Friction",
        "2,3" to "Travelling",
        "3,0" to "Maintain",
        "3,1" to "Utilization",
        "3,2" to "Rearrange",
        "3,3" to "Switch Out"
    )

    val shades = mapOf(
        "0,0" to listOf(1, 4, 5, 9),
        "0,1" to listOf(1, 4, 8, 9),
        "0,2" to listOf(1, 6, 7, 8, 9),
        "0,3" to listOf(4, 5, 6, 7, 9),
        "1,0" to listOf(3, 5, 6, 8, 9),
        "1,1" to listOf(2, 5, 7, 8, 9),
        "1,2" to listOf(2, 3, 7, 9),
        "1,3" to listOf(2, 3, 6, 9),
        "2,0" to listOf(2, 3, 7, 9),
        "2,1" to listOf(2, 3, 6, 9),
        "2,2" to listOf(3, 5, 6, 8, 9),
        "2,3" to listOf(2, 5, 7, 8, 9),
        "3,0" to listOf(1, 6, 7, 8, 9),
        "3,1" to listOf(4, 5, 6, 7, 9),
        "3,2" to listOf(1, 4, 5, 9),
        "3,3" to listOf(1, 4, 8, 9)
    )

    val orders = mapOf(
        "0,0" to (listOf(4, 1, 5, 4, 9) to listOf(1, 3)),
        "0,1" to (listOf(8, 1, 1, 4, 9) to listOf(0, 3)),
        "0,2" to (listOf(8, 7, 1, 6, 9) to listOf(1, 3)),
        "0,3" to (listOf(4, 7, 5, 6, 9) to listOf(0, 1, 3)),
        "1,0" to (listOf(3, 8, 6, 5, 9) to listOf(0, 3)),
        "1,1" to (listOf(7, 8, 2, 5, 9) to listOf(1, 3)),
        "1,2" to (listOf(7, 3, 2, 2, 9) to listOf(3)),
        "1,3" to (listOf(3, 3, 6, 2, 9) to listOf(2, 3)),
        "2,0" to (listOf(2, 2, 7, 3, 9) to listOf(1, 3)),
        "2,1" to (listOf(6, 2, 3, 3, 9) to listOf(0, 3)),
        "2,2" to (listOf(6, 5, 3, 8, 9) to listOf(2)),
        "2,3" to (listOf(2, 5, 7, 8, 9) to listOf<Int>()),
        "3,0" to (listOf(1, 6, 8, 7, 9) to listOf(1, 3)),
        "3,1" to (listOf(5, 6, 4, 7, 9) to listOf(1, 2, 3)),
        "3,2" to (listOf(5, 4, 4, 1, 9) to listOf(3)),
        "3,3" to (listOf(1, 4, 8, 1, 9) to listOf(2, 3))
    )

    fun getCells(): List<FrameworkCell> {
        val cells = mutableListOf<FrameworkCell>()
        for (r in 0..3) {
            for (c in 0..3) {
                val key = "$r,$c"
                val (path, arcs) = orders[key] ?: (emptyList<Int>() to emptyList())
                cells.add(
                    FrameworkCell(
                        row = r,
                        col = c,
                        name = cellNames[key] ?: "",
                        slots = shades[key] ?: emptyList(),
                        path = path,
                        arcs = arcs
                    )
                )
            }
        }
        return cells
    }
}
