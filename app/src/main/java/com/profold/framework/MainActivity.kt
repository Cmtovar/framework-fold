package com.profold.framework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profold.framework.ui.theme.FrameworkFoldTheme

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
        // Main 4x4 grid
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Column titles
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

            // 4x4 grid
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

        // Detail panel
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

        // Compact 4x4 grid (smaller cells)
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

    Box(
        modifier = modifier
            .aspectRatio(1f)
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
            // Cell name
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
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Coordinate label
            Text(
                text = "${cell.row},${cell.col}",
                color = Color(0xFF555555),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
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

        // Slot breakdown
        Text(
            text = "SLOTS",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show each active slot with its word and color
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

        // Traversal path
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
