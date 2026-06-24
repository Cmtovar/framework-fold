package com.profold.framework

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PairBackground = Color(0xFF141418)
private val PairSurface = Color(0xFF1E1E24)
private val PairSurfaceActive = Color(0xFF26262E)
private val PairSlot = Color(0xFF1A1A20)
private val PairSlotFilled = Color(0xFF8A8A96)
private val PairSlotSpotlight = Color(0xFFC0C0CC)
private val PairText = Color(0xFFE8E8F0)
private val PairTextSecondary = Color(0xFFA8A8B4)
private val PairBorder = Color(0xFF4A4A58)

private data class PairDisplayData(
    val leftName: String,
    val rightName: String,
    val fills: List<Int>,
    val labels: List<String>
)

private fun pairDisplayData(pair: FrameworkData.ChutePair): PairDisplayData {
    val leftOrder = FrameworkData.orders.getValue(pair.left).first.take(4)
    val rightOrder = FrameworkData.orders.getValue(pair.right).first.take(4)
    val fills = leftOrder + rightOrder
    return PairDisplayData(
        leftName = FrameworkData.cellNames.getValue(pair.left),
        rightName = FrameworkData.cellNames.getValue(pair.right),
        fills = fills,
        labels = fills.map { FrameworkData.slotWords.getValue(it) }
    )
}

@Composable
fun ChutePairScreen(
    widthSizeClass: WindowWidthSizeClass,
    onBackToGrid: () -> Unit
) {
    var activePairIndex by remember { mutableIntStateOf(0) }
    var spotlightIndex by remember(activePairIndex) { mutableIntStateOf(0) }
    var isFlat by remember { mutableStateOf(false) }
    val activeData = pairDisplayData(FrameworkData.chutePairs[activePairIndex])

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PairBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PairHeader(
            isFlat = isFlat,
            onBackToGrid = onBackToGrid,
            onToggleFlat = { isFlat = !isFlat }
        )

        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            PairSelectorRow(
                activePairIndex = activePairIndex,
                onPairSelected = { activePairIndex = it }
            )
            PairModule(
                data = activeData,
                spotlightIndex = spotlightIndex,
                isFlat = isFlat,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
        } else {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PairSelectorColumn(
                    activePairIndex = activePairIndex,
                    onPairSelected = { activePairIndex = it },
                    modifier = Modifier
                        .width(112.dp)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    PairModule(
                        data = activeData,
                        spotlightIndex = spotlightIndex,
                        isFlat = isFlat
                    )
                }
            }
        }

        PairControls(
            canAdvance = spotlightIndex < 7,
            onReset = { spotlightIndex = 0 },
            onAdvance = { if (spotlightIndex < 7) spotlightIndex++ }
        )
    }
}

@Composable
private fun PairHeader(
    isFlat: Boolean,
    onBackToGrid: () -> Unit,
    onToggleFlat: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onBackToGrid) { Text("Grid") }
        Text("Chute Pairs", color = PairText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = onToggleFlat) { Text(if (isFlat) "Grouped" else "Flat") }
    }
}

@Composable
private fun PairSelectorRow(
    activePairIndex: Int,
    onPairSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FrameworkData.chutePairs.forEachIndexed { index, pair ->
            PairPreview(index, pair, index == activePairIndex, onPairSelected)
        }
    }
}

@Composable
private fun PairSelectorColumn(
    activePairIndex: Int,
    onPairSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FrameworkData.chutePairs.forEachIndexed { index, pair ->
            PairPreview(index, pair, index == activePairIndex, onPairSelected)
        }
    }
}

@Composable
private fun PairPreview(
    index: Int,
    pair: FrameworkData.ChutePair,
    isActive: Boolean,
    onPairSelected: (Int) -> Unit
) {
    val data = pairDisplayData(pair)
    val shape = RoundedCornerShape(5.dp)
    Row(
        modifier = Modifier
            .background(if (isActive) PairSurfaceActive else PairSurface, shape)
            .then(if (index < 6) Modifier.border(2.dp, Color(0xFF2A2A34), shape) else Modifier)
            .then(if (isActive) Modifier.border(1.dp, PairBorder, shape) else Modifier)
            .semantics {
                contentDescription = "${data.leftName} and ${data.rightName}"
                selected = isActive
            }
            .clickable(
                role = Role.Button,
                onClickLabel = "Show ${data.leftName} and ${data.rightName}"
            ) { onPairSelected(index) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        data.fills.forEachIndexed { columnIndex, fill ->
            if (columnIndex == 4) Spacer(Modifier.width(2.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                for (value in 8 downTo 1) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .background(
                                if (value == fill) PairSlotFilled else PairSlot,
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PairModule(
    data: PairDisplayData,
    spotlightIndex: Int,
    isFlat: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val squareSize = if (maxWidth < 520.dp) 24.dp else 30.dp
        val columnGap = if (maxWidth < 520.dp) 8.dp else 14.dp
        val groupGap = if (isFlat) 0.dp else 18.dp

        Row(
            horizontalArrangement = Arrangement.spacedBy(groupGap),
            verticalAlignment = Alignment.Top
        ) {
            PairGroup(
                title = data.leftName,
                columnIndices = 0..3,
                data = data,
                spotlightIndex = spotlightIndex,
                showTitle = !isFlat,
                squareSize = squareSize,
                columnGap = columnGap
            )
            PairGroup(
                title = data.rightName,
                columnIndices = 4..7,
                data = data,
                spotlightIndex = spotlightIndex,
                showTitle = !isFlat,
                squareSize = squareSize,
                columnGap = columnGap
            )
        }
    }
}

@Composable
private fun PairGroup(
    title: String,
    columnIndices: IntRange,
    data: PairDisplayData,
    spotlightIndex: Int,
    showTitle: Boolean,
    squareSize: Dp,
    columnGap: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (showTitle) title else "",
            color = PairText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.height(18.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(columnGap)) {
            columnIndices.forEach { columnIndex ->
                PairColumn(
                    label = data.labels[columnIndex],
                    fill = data.fills[columnIndex],
                    isSpotlighted = columnIndex == spotlightIndex,
                    squareSize = squareSize
                )
            }
        }
    }
}

@Composable
private fun PairColumn(
    label: String,
    fill: Int,
    isSpotlighted: Boolean,
    squareSize: Dp
) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier = Modifier
            .then(if (isSpotlighted) Modifier.background(PairSurfaceActive, shape).padding(4.dp) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = PairTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(squareSize)
        )
        for (value in 8 downTo 1) {
            Box(
                modifier = Modifier
                    .size(squareSize)
                    .background(
                        when {
                            value != fill -> PairSlot
                            isSpotlighted -> PairSlotSpotlight
                            else -> PairSlotFilled
                        },
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun PairControls(
    canAdvance: Boolean,
    onReset: () -> Unit,
    onAdvance: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onReset) { Text("Reset") }
        Button(
            onClick = onAdvance,
            enabled = canAdvance,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (canAdvance) "Next column" else "Complete")
        }
    }
}
