package com.profold.framework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profold.framework.ui.theme.FrameworkFoldTheme
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
                FrameworkScreen(widthSizeClass = windowSizeClass.widthSizeClass)
            }
        }
    }
}

// Colors matching the web CSS
private val BG = Color(0xFF1A1A1A)
private val CELL_BG = Color(0xFF333333)
private val CELL_BG_ACTIVE = Color(0xFF444444)
private val SLOT_BG = Color(0xFF2A2A2A)
private val SLOT_SHADED = Color(0xFF666666)
private val TEXT_LABEL = Color(0xFF555555)
private val TEXT_LABEL_ACTIVE = Color(0xFF999999)
private val TEXT_WORDS = Color(0xFF4A4A4A)
private val TEXT_WORDS_ACTIVE = Color(0xFF999999)
private val TEXT_DISTANCE = Color(0xFF3A3A3A)
private val TEXT_DISTANCE_ACTIVE = Color(0xFF666666)
private val TEXT_BOTTOM = Color(0xFF4A4A4A)
private val TEXT_BOTTOM_ACTIVE = Color(0xFF999999)
private val LINE_COLOR = Color(0xFF888888)
private val LINE_COLOR_ACTIVE = Color(0xFFAAAAAA)
private val DOT_COLOR = Color(0xFF888888)
private val DOT_COLOR_ACTIVE = Color(0xFFAAAAAA)
private val CONN_COLOR = Color(0xFF2A2A2A)
private val CONN_ACTIVE = Color(0xFF555555)
private val CHUTE_COLOR = Color(0xFF5A3A2A)
private val CHUTE_ACTIVE = Color(0xFFC47A4A)
private val COLUMN_TITLE_COLOR = Color(0xFF404040)
private val NAV_BTN_BG = Color(0xFF2A2A2A)
private val NAV_BTN_ACTIVE = Color(0xFF444444)
private val NAV_BTN_TEXT = Color(0xFF888888)

@Composable
fun FrameworkScreen(widthSizeClass: WindowWidthSizeClass) {
    var activeIndex by remember { mutableIntStateOf(0) }
    val bfsResult = remember(activeIndex) { FrameworkData.bfs(activeIndex) }

    Surface(modifier = Modifier.fillMaxSize(), color = BG) {
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> CompactLayout(activeIndex, bfsResult) { activeIndex = it }
            else -> ExpandedLayout(activeIndex, bfsResult) { activeIndex = it }
        }
    }
}

// Navigation helpers
private fun moveUp(activeIndex: Int): Int {
    val row = activeIndex / FrameworkData.COLS
    return if (row > 0) activeIndex - FrameworkData.COLS else activeIndex
}

private fun moveDown(activeIndex: Int): Int {
    val row = activeIndex / FrameworkData.COLS
    return if (row < FrameworkData.ROWS - 1) activeIndex + FrameworkData.COLS else activeIndex
}

// Horizontal right: only works from columns 0,2 (ArrowRight)
private fun moveRight(activeIndex: Int): Int {
    val row = activeIndex / FrameworkData.COLS
    val col = activeIndex % FrameworkData.COLS
    return when (col) {
        0 -> row * FrameworkData.COLS + 2  // 0→2
        2 -> row * FrameworkData.COLS + 3  // 2→3
        else -> activeIndex
    }
}

// Horizontal left: only works from columns 3,1 (ArrowLeft)
private fun moveLeft(activeIndex: Int): Int {
    val row = activeIndex / FrameworkData.COLS
    val col = activeIndex % FrameworkData.COLS
    return when (col) {
        3 -> row * FrameworkData.COLS + 1  // 3→1
        1 -> row * FrameworkData.COLS + 0  // 1→0
        else -> activeIndex
    }
}

private fun moveChute(activeIndex: Int): Int {
    return FrameworkData.chuteLookup[activeIndex] ?: activeIndex
}

// Expanded layout: grid above tree, both stacked vertically
@Composable
fun ExpandedLayout(
    activeIndex: Int,
    bfsResult: FrameworkData.BfsResult,
    onCellTap: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val availableWidth = maxWidth
        val gap = 16.dp
        val cellWidth = (availableWidth - gap * 3) / 4
        val cellHeight = cellWidth * 0.56f
        val slotSize = (cellWidth.value * 0.069f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FrameworkGrid(
                activeIndex = activeIndex,
                bfsResult = bfsResult,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                slotSize = slotSize,
                gap = gap,
                onCellTap = onCellTap
            )

            Spacer(modifier = Modifier.height(16.dp))

            TreePanel(
                activeIndex = activeIndex,
                bfsResult = bfsResult,
                onNodeTap = onCellTap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            NavigationControls(
                activeIndex = activeIndex,
                onNavigate = onCellTap
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Compact layout: scrollable, auto-sized to fit width
@Composable
fun CompactLayout(
    activeIndex: Int,
    bfsResult: FrameworkData.BfsResult,
    onCellTap: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 8.dp)
    ) {
        val availableWidth = maxWidth
        val gap = 6.dp
        val cellWidth = (availableWidth - gap * 3) / 4
        val cellHeight = cellWidth * 0.65f
        val slotSize = (cellWidth.value * 0.069f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            FrameworkGrid(
                activeIndex = activeIndex,
                bfsResult = bfsResult,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                slotSize = slotSize,
                gap = gap,
                onCellTap = onCellTap
            )

            Spacer(modifier = Modifier.height(12.dp))

            NavigationControls(
                activeIndex = activeIndex,
                onNavigate = onCellTap
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// 4x4 Grid with connection lines BEHIND cells
@Composable
fun FrameworkGrid(
    activeIndex: Int,
    bfsResult: FrameworkData.BfsResult,
    cellWidth: Dp,
    cellHeight: Dp,
    slotSize: Dp,
    gap: Dp,
    onCellTap: (Int) -> Unit
) {
    val cellCenters = remember { mutableStateMapOf<Int, Offset>() }
    var gridCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier.onGloballyPositioned { gridCoords = it }
    ) {
        // Connection lines BEHIND cells (z-index 0 in web CSS)
        if (cellCenters.size == 16) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { clip = false }
            ) {
                drawConnectionLines(cellCenters, activeIndex)
            }
        }

        // Grid content ON TOP (z-index 1 in web CSS)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (r in 0 until FrameworkData.ROWS) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    for (c in 0 until FrameworkData.COLS) {
                        val idx = r * FrameworkData.COLS + c
                        val isActive = idx == activeIndex
                        val dist = bfsResult.dist[idx]

                        FrameworkCell(
                            row = r,
                            col = c,
                            isActive = isActive,
                            distance = if (isActive) null else if (dist == -1) null else dist,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            slotSize = slotSize,
                            onTap = { onCellTap(idx) },
                            onPositioned = { center -> cellCenters[idx] = center },
                            gridCoords = gridCoords
                        )
                    }
                }
                if (r < FrameworkData.ROWS - 1) Spacer(modifier = Modifier.height(gap))
            }

            Spacer(modifier = Modifier.height(gap / 2))

            // Column titles at bottom
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                FrameworkData.columnTitles.forEach { title ->
                    Text(
                        text = title,
                        color = COLUMN_TITLE_COLOR,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(cellWidth),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ConnectionLegend()
        }
    }
}

// Single cell
@Composable
fun FrameworkCell(
    row: Int,
    col: Int,
    isActive: Boolean,
    distance: Int?,
    cellWidth: Dp,
    cellHeight: Dp,
    slotSize: Dp,
    onTap: () -> Unit,
    onPositioned: (Offset) -> Unit,
    gridCoords: LayoutCoordinates?
) {
    val key = "$row,$col"
    val shadedSlots = FrameworkData.shades[key] ?: emptyList()
    val orderData = FrameworkData.orders[key]
    val path = orderData?.first ?: emptyList()
    val arcs = orderData?.second ?: emptyList()
    val cellName = FrameworkData.cellNames[key] ?: ""
    val words = path.map { FrameworkData.slotWords[it] ?: "" }

    val shape = RoundedCornerShape(8.dp)
    val slotCenters = remember { mutableStateMapOf<Int, Offset>() }
    var cellBoxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val isCompact = cellWidth < 100.dp

    Box(
        modifier = Modifier
            .size(cellWidth, cellHeight)
            .onGloballyPositioned { coords ->
                cellBoxCoords = coords
                val gc = gridCoords ?: return@onGloballyPositioned
                val pos = gc.localPositionOf(coords, Offset.Zero)
                onPositioned(Offset(
                    pos.x + coords.size.width / 2f,
                    pos.y + coords.size.height / 2f
                ))
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(if (isActive) CELL_BG_ACTIVE else CELL_BG)
                .clickable { onTap() }
        ) {
            // Words row at top
            if (!isCompact) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    words.forEach { word ->
                        Text(
                            text = word,
                            color = if (isActive) TEXT_WORDS_ACTIVE else TEXT_WORDS,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Distance number top-right
            if (distance != null) {
                Text(
                    text = "$distance",
                    color = if (isActive) TEXT_DISTANCE_ACTIVE else TEXT_DISTANCE,
                    fontSize = if (isCompact) 12.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 6.dp)
                )
            }

            // Center: coordinate + slots
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = key,
                    color = if (isActive) TEXT_LABEL_ACTIVE else TEXT_LABEL,
                    fontSize = if (isCompact) 8.sp else 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(if (isCompact) 1.dp else 3.dp)) {
                    for (s in 1..FrameworkData.SLOTS_PER_ROW) {
                        val isShaded = s in shadedSlots
                        Box(
                            modifier = Modifier
                                .size(slotSize)
                                .background(
                                    if (isShaded) SLOT_SHADED else SLOT_BG,
                                    RoundedCornerShape(2.dp)
                                )
                                .onGloballyPositioned { coords ->
                                    val cbc = cellBoxCoords ?: return@onGloballyPositioned
                                    val posInCell = cbc.localPositionOf(coords, Offset.Zero)
                                    slotCenters[s] = Offset(
                                        posInCell.x + coords.size.width / 2f,
                                        posInCell.y + coords.size.height / 2f
                                    )
                                }
                        )
                    }
                }
            }

            // Cell name at bottom
            Text(
                text = cellName,
                color = if (isActive) TEXT_BOTTOM_ACTIVE else TEXT_BOTTOM,
                fontSize = if (isCompact) 7.sp else 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isCompact) 2.dp else 5.dp)
            )
        }

        // Traversal path overlay (unclipped, on top of cell)
        if (slotCenters.size == FrameworkData.SLOTS_PER_ROW && path.size > 1) {
            val lineCol = if (isActive) LINE_COLOR_ACTIVE else LINE_COLOR
            val dotCol = if (isActive) DOT_COLOR_ACTIVE else DOT_COLOR

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { clip = false }
            ) {
                drawTraversalPath(path, arcs, slotCenters, lineCol, dotCol)
            }
        }
    }
}

// Navigation controls: Up, Down, Next (horizontal cycle), Chute
@Composable
fun NavigationControls(
    activeIndex: Int,
    onNavigate: (Int) -> Unit
) {
    val hasChute = FrameworkData.chuteLookup.containsKey(activeIndex)
    val row = activeIndex / FrameworkData.COLS
    val col = activeIndex % FrameworkData.COLS

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Up button
        NavButton(
            label = "W",
            enabled = row > 0,
            onClick = { onNavigate(moveUp(activeIndex)) }
        )

        // Middle row: Left, Chute, Right
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: only works from columns 3,1
            NavButton(
                label = "A",
                enabled = col == 3 || col == 1,
                onClick = { onNavigate(moveLeft(activeIndex)) }
            )

            // Chute button
            NavButton(
                label = "/",
                enabled = hasChute,
                onClick = { if (hasChute) onNavigate(moveChute(activeIndex)) }
            )

            // Right: only works from columns 0,2
            NavButton(
                label = "D",
                enabled = col == 0 || col == 2,
                onClick = { onNavigate(moveRight(activeIndex)) }
            )
        }

        // Down button
        NavButton(
            label = "S",
            enabled = row < FrameworkData.ROWS - 1,
            onClick = { onNavigate(moveDown(activeIndex)) }
        )
    }
}


@Composable
fun NavButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled) NAV_BTN_BG else NAV_BTN_BG.copy(alpha = 0.3f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) NAV_BTN_TEXT else NAV_BTN_TEXT.copy(alpha = 0.3f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Draw traversal paths inside a cell
private fun DrawScope.drawTraversalPath(
    path: List<Int>,
    arcs: List<Int>,
    slotCenters: Map<Int, Offset>,
    lineColor: Color,
    dotColor: Color
) {
    val strokeStyle = Stroke(width = 1.5f, cap = StrokeCap.Round)

    for (i in 0 until path.size - 1) {
        val fromSlot = path[i]
        val toSlot = path[i + 1]
        val from = slotCenters[fromSlot] ?: continue
        val to = slotCenters[toSlot] ?: continue

        if (fromSlot == toSlot) {
            val loopPath = Path().apply {
                moveTo(from.x, from.y)
                cubicTo(from.x - 12f, from.y + 24f, from.x + 12f, from.y + 24f, from.x, from.y)
            }
            drawPath(loopPath, lineColor, style = strokeStyle)
        } else if (i in arcs) {
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
            drawLine(lineColor, from, to, strokeWidth = 1.5f, cap = StrokeCap.Round)
        }
    }

    for (slotNum in path) {
        val center = slotCenters[slotNum] ?: continue
        drawCircle(dotColor, 3f, center)
    }
}

// Draw connection lines between cells
private fun DrawScope.drawConnectionLines(
    cellCenters: Map<Int, Offset>,
    activeIndex: Int
) {
    val jumpDash = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    val chuteDash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))

    FrameworkData.connections.forEach { conn ->
        val from = cellCenters[conn.from] ?: return@forEach
        val to = cellCenters[conn.to] ?: return@forEach

        val isActive = if (conn.directed) {
            conn.from == activeIndex
        } else {
            conn.from == activeIndex || conn.to == activeIndex
        }

        val color: Color
        val width: Float
        val dash: PathEffect?

        when (conn.type) {
            FrameworkData.ConnectionType.CHUTE -> {
                color = if (isActive) CHUTE_ACTIVE else CHUTE_COLOR
                width = if (isActive) 2.5f else 1.5f
                dash = chuteDash
            }
            FrameworkData.ConnectionType.JUMP -> {
                color = if (isActive) CONN_ACTIVE else CONN_COLOR
                width = if (isActive) 2.5f else 2f
                dash = jumpDash
            }
            FrameworkData.ConnectionType.STEP -> {
                color = if (isActive) CONN_ACTIVE else CONN_COLOR
                width = if (isActive) 2.5f else 2f
                dash = null
            }
        }

        drawLine(
            color = color,
            start = from,
            end = to,
            strokeWidth = width,
            cap = StrokeCap.Round,
            pathEffect = dash
        )
    }
}

// Connection legend
@Composable
fun ConnectionLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                drawLine(CONN_COLOR, Offset.Zero, Offset(size.width, 0f), strokeWidth = 2f)
            }
            Text("Step (1)", color = TEXT_LABEL, fontSize = 10.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                drawLine(CONN_COLOR, Offset.Zero, Offset(size.width, 0f), strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
            }
            Text("Jump (2)", color = TEXT_LABEL, fontSize = 10.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                drawLine(CHUTE_COLOR, Offset.Zero, Offset(size.width, 0f), strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)))
            }
            Text("Chute / Ladder", color = TEXT_LABEL, fontSize = 10.sp)
        }
    }
}

// Tree panel with labels and tap detection
@Composable
fun TreePanel(
    activeIndex: Int,
    bfsResult: FrameworkData.BfsResult,
    onNodeTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dist = bfsResult.dist
    val parents = bfsResult.parents
    val n = FrameworkData.ROWS * FrameworkData.COLS
    val nodeR = 20f
    val levelGap = 80f
    val padding = 30f

    val levels = mutableMapOf<Int, MutableList<Int>>()
    var maxDist = 0
    for (i in 0 until n) {
        val d = dist[i]
        if (d == -1) continue
        levels.getOrPut(d) { mutableListOf() }.add(i)
        if (d > maxDist) maxDist = d
    }

    val svgW = 400f
    val svgH = (maxDist + 1) * levelGap + padding * 2

    val pos = mutableMapOf<Int, Offset>()
    for (d in 0..maxDist) {
        val nodesAtLevel = levels[d] ?: continue
        val count = nodesAtLevel.size
        val spacing = svgW / (count + 1)
        nodesAtLevel.forEachIndexed { i, idx ->
            pos[idx] = Offset(spacing * (i + 1), padding + d * levelGap + nodeR)
        }
    }

    // Store scaled positions for tap detection
    var scaledPositions by remember { mutableStateOf(mapOf<Int, Offset>()) }
    var scaledRadius by remember { mutableStateOf(0f) }

    val labelPaint = remember {
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }

    Box(modifier = modifier
        .pointerInput(activeIndex) {
            detectTapGestures { tapOffset ->
                val r = scaledRadius
                for ((idx, center) in scaledPositions) {
                    val dx = tapOffset.x - center.x
                    val dy = tapOffset.y - center.y
                    if (dx * dx + dy * dy <= r * r) {
                        onNodeTap(idx)
                        break
                    }
                }
            }
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / svgW
            val scaleY = size.height / svgH
            val scale = minOf(scaleX, scaleY)
            val offsetX = (size.width - svgW * scale) / 2f

            fun scaled(p: Offset) = Offset(p.x * scale + offsetX, p.y * scale)

            val newPositions = mutableMapOf<Int, Offset>()
            val r = nodeR * scale
            scaledRadius = r

            // Draw edges
            for (i in 0 until n) {
                if (dist[i] <= 0) continue
                for (p in parents[i]) {
                    val pPos = pos[p] ?: continue
                    val iPos = pos[i] ?: continue
                    val type = FrameworkData.edgeTypes["$p-$i"] ?: FrameworkData.ConnectionType.STEP

                    val color: Color
                    val dash: PathEffect?
                    when (type) {
                        FrameworkData.ConnectionType.JUMP -> {
                            color = CONN_COLOR; dash = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                        }
                        FrameworkData.ConnectionType.CHUTE -> {
                            color = CHUTE_COLOR; dash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
                        }
                        FrameworkData.ConnectionType.STEP -> {
                            color = CONN_COLOR; dash = null
                        }
                    }
                    drawLine(color, scaled(pPos), scaled(iPos), strokeWidth = 1.5f * scale, pathEffect = dash)
                }
            }

            // Draw nodes with labels
            labelPaint.textSize = 11f * scale
            for (i in 0 until n) {
                val p = pos[i] ?: continue
                val sp = scaled(p)
                newPositions[i] = sp
                val isSource = i == activeIndex

                drawCircle(
                    color = if (isSource) CELL_BG_ACTIVE else CELL_BG,
                    radius = r,
                    center = sp
                )
                if (isSource) {
                    drawCircle(
                        color = Color(0xFF666666),
                        radius = r,
                        center = sp,
                        style = Stroke(width = 2f * scale)
                    )
                }

                // Coordinate label inside node
                val row = i / FrameworkData.COLS
                val col = i % FrameworkData.COLS
                labelPaint.color = if (isSource) 0xFF999999.toInt() else 0xFF555555.toInt()
                drawContext.canvas.nativeCanvas.drawText(
                    "$row,$col",
                    sp.x,
                    sp.y + labelPaint.textSize / 3f,
                    labelPaint
                )

                // Distance below node (non-source only)
                if (!isSource && dist[i] > 0) {
                    labelPaint.textSize = 10f * scale
                    labelPaint.color = 0xFF555555.toInt()
                    drawContext.canvas.nativeCanvas.drawText(
                        "${dist[i]}",
                        sp.x,
                        sp.y + r + 12f * scale,
                        labelPaint
                    )
                    labelPaint.textSize = 11f * scale
                }
            }

            scaledPositions = newPositions
        }
    }
}
