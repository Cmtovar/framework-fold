package com.profold.framework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profold.framework.ui.theme.FrameworkFoldTheme
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
import kotlin.math.max

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            FrameworkFoldTheme {
                FrameworkApp(widthSizeClass = windowSizeClass.widthSizeClass)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameworkApp(widthSizeClass: WindowWidthSizeClass) {
    val cells = remember { FrameworkData.getCells() }
    var selectedCell by remember { mutableStateOf<FrameworkCell?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Framework") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFF1A1A1A)
        ) {
            when (widthSizeClass) {
                WindowWidthSizeClass.Compact -> CompactLayout(cells, selectedCell) { selectedCell = it }
                WindowWidthSizeClass.Medium,
                WindowWidthSizeClass.Expanded -> ExpandedLayout(cells, selectedCell) { selectedCell = it }
            }
        }
    }
}

@Composable
fun ExpandedLayout(
    cells: List<FrameworkCell>,
    selectedCell: FrameworkCell?,
    onCellSelected: (FrameworkCell?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FrameworkData.columnTitles.forEach { title ->
                    Text(
                        text = title.uppercase(),
                        color = Color(0xFF666666),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            for (r in 0..3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    for (c in 0..3) {
                        val cell = cells[r * 4 + c]
                        GridCell(
                            cell = cell,
                            isSelected = selectedCell == cell,
                            modifier = Modifier.weight(1f),
                            onTap = {
                                onCellSelected(if (selectedCell == cell) null else cell)
                            }
                        )
                    }
                }
                if (r < 3) Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (selectedCell != null) {
            Spacer(modifier = Modifier.width(24.dp))
            CellDetail(
                cell = selectedCell,
                modifier = Modifier.weight(0.4f)
            )
        }
    }
}

@Composable
fun CompactLayout(
    cells: List<FrameworkCell>,
    selectedCell: FrameworkCell?,
    onCellSelected: (FrameworkCell?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedCell != null) {
            CellDetail(cell = selectedCell, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }

        for (r in 0..3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                for (c in 0..3) {
                    val cell = cells[r * 4 + c]
                    GridCell(
                        cell = cell,
                        isSelected = selectedCell == cell,
                        modifier = Modifier.weight(1f),
                        onTap = {
                            onCellSelected(if (selectedCell == cell) null else cell)
                        }
                    )
                }
            }
            if (r < 3) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GridCell(
    cell: FrameworkCell,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val borderMod = if (isSelected) {
        Modifier.border(2.dp, Color(0xFF666666), shape)
    } else {
        Modifier
    }

    // Track slot positions relative to the outer Box for canvas drawing
    val slotCenters = remember { mutableStateMapOf<Int, Offset>() }
    var outerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onGloballyPositioned { outerCoords = it }
    ) {
        // Clipped background + content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(if (isSelected) Color(0xFF444444) else Color(0xFF333333), shape)
                .then(borderMod)
                .clickable { onTap() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cell.name,
                    color = if (isSelected) Color(0xFFAAAAAA) else Color(0xFF666666),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Slot row — 9 mini squares
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (s in 1..9) {
                        val isShaded = s in cell.slots
                        val color = if (isShaded) {
                            FrameworkData.slotColors[s] ?: Color(0xFF666666)
                        } else {
                            Color(0xFF2A2A2A)
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, RoundedCornerShape(1.dp))
                                .onGloballyPositioned { coords ->
                                    val oc = outerCoords ?: return@onGloballyPositioned
                                    val posInOuter = oc.localPositionOf(coords, Offset.Zero)
                                    slotCenters[s] = Offset(
                                        posInOuter.x + coords.size.width / 2f,
                                        posInOuter.y + coords.size.height / 2f
                                    )
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${cell.row},${cell.col}",
                    color = Color(0xFF555555),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Canvas overlay — unclipped so arcs can extend beyond cell bounds
        if (slotCenters.size == 9 && cell.path.size > 1) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { clip = false }
            ) {
                val lineColor = Color(0xFF888888)
                val dotColor = Color(0xFF888888)
                val strokeStyle = Stroke(width = 1.5f, cap = StrokeCap.Round)
                val dotRadius = 3f

                val path = cell.path
                val arcs = cell.arcs

                for (i in 0 until path.size - 1) {
                    val fromSlot = path[i]
                    val toSlot = path[i + 1]
                    val from = slotCenters[fromSlot] ?: continue
                    val to = slotCenters[toSlot] ?: continue

                    if (fromSlot == toSlot) {
                        // Self-loop: teardrop below the node
                        val loopPath = Path().apply {
                            moveTo(from.x, from.y)
                            cubicTo(
                                from.x - 12f, from.y + 24f,
                                from.x + 12f, from.y + 24f,
                                from.x, from.y
                            )
                        }
                        drawPath(loopPath, lineColor, style = strokeStyle)
                    } else if (i in arcs) {
                        // Arc: quadratic bezier
                        val dx = abs(to.x - from.x)
                        val arcHeight = max(dx * 0.5f, 20f)
                        val midX = (from.x + to.x) / 2f
                        val isLast = (i == path.size - 2)
                        val midY = if (isLast) from.y - arcHeight else from.y + arcHeight

                        val arcPath = Path().apply {
                            moveTo(from.x, from.y)
                            quadraticTo(midX, midY, to.x, to.y)
                        }
                        drawPath(arcPath, lineColor, style = strokeStyle)
                    } else {
                        // Straight line
                        drawLine(lineColor, from, to, strokeWidth = 1.5f, cap = StrokeCap.Round)
                    }
                }

                // Dots at each stop
                for (slotNum in path) {
                    val center = slotCenters[slotNum] ?: continue
                    drawCircle(dotColor, dotRadius, center)
                }
            }
        }
    }
}

@Composable
fun CellDetail(cell: FrameworkCell, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF262626))
            .padding(20.dp)
    ) {
        Text(
            text = cell.name,
            color = Color(0xFFCCCCCC),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${cell.row},${cell.col}",
            color = Color(0xFF888888),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SLOTS",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        cell.slots.forEach { slotNum ->
            val word = FrameworkData.slotWords[slotNum] ?: ""
            val color = FrameworkData.slotColors[slotNum] ?: Color.Gray
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = word,
                    color = Color(0xFFAAAAAA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($slotNum)",
                    color = Color(0xFF666666),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PATH",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            cell.path.forEachIndexed { i, slotNum ->
                val color = FrameworkData.slotColors[slotNum] ?: Color.Gray
                val isArc = i in cell.arcs
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(color, RoundedCornerShape(3.dp))
                        .then(
                            if (isArc) Modifier.border(
                                1.dp,
                                Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(3.dp)
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (FrameworkData.slotWords[slotNum] ?: "").take(2),
                        color = Color(0xFF000000).copy(alpha = 0.5f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
