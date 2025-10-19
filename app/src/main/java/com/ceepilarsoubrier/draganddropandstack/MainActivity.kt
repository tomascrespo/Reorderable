package com.ceepilarsoubrier.draganddropandstack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ceepilarsoubrier.draganddropandstack.ui.theme.DragAndDropAndStackTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf


import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.text.style.TextAlign


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.zIndex
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableLazyVerticalGrid
import sh.calvin.reorderable.StackingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DragAndDropAndStackTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Usa una Column para organizar los elementos verticalmente
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Greeting(
                            name = "Grid 1",
                            modifier = Modifier.padding(innerPadding)
                        )
                        val items = (0..25).map {
                            Item(id = it, text = "$it", size = if (it % 2 == 0) 70 else 100)
                        }
                        SimpleReorderableLazyVerticalGridScreen(items)

                        Greeting(
                            name = "Grid 2",
                            modifier = Modifier.padding(innerPadding)
                        )

                        val otherItems = (30..50).map {
                            Item(id = it, text = "$it", size = if (it % 2 == 0) 70 else 100)
                        }
                        SimpleReorderableLazyVerticalGridScreen(otherItems)
                    }
                }
            }
        }
    }
}

data class Item(val id: Int, var text: String, val size: Int)

@Composable
fun SimpleReorderableLazyVerticalGridScreen(
    items: List<Item>
) {
    val overlapThreshold = 0.70f
    val hoverDelayMs = 500L

    var list by remember { mutableStateOf(items) }
    val lazyGridState = rememberLazyGridState()

    ReorderableLazyVerticalGrid(
        items = list,
        key = { it.id },
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = Modifier.fillMaxWidth(),
        state = lazyGridState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        stackingMode = StackingMode.Enabled, // Cambiar a Disabled para sólo ordenar
        overlapThreshold = overlapThreshold,
        hoverDelayMs = hoverDelayMs,
        onMove = { fromIndex, toIndex ->
            list = list.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }.toList()
        },
        onDropOver = { dragKey, overKey ->
            val dragId = (dragKey as? Int) ?: return@ReorderableLazyVerticalGrid
            val overId = (overKey as? Int) ?: return@ReorderableLazyVerticalGrid
            val draggedItem = list.firstOrNull { it.id == dragId }
            val overItem = list.firstOrNull { it.id == overId }
            if (draggedItem != null && overItem != null) {
                overItem.text += "," + draggedItem.text
                list = list.toMutableList().apply { remove(draggedItem) }.toList()
            }
        }
    ) { item, _ ->
        val interactionSource = remember { MutableInteractionSource() }
        Card(
            onClick = {},
            modifier = Modifier
                .height(96.dp)
                .clearAndSetSemantics { },
            interactionSource = interactionSource,
        ) {
            Box(Modifier.fillMaxSize()) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .draggableHandle(
                            interactionSource = interactionSource,
                        )
                        .clearAndSetSemantics { },
                    onClick = {},
                ) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Reorder")
                }
                Text(
                    item.text,
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/*

private fun Rect.area(): Float = max(0f, width) * max(0f, height)

private fun intersectionArea(a: Rect, b: Rect): Float {
    val left = max(a.left, b.left)
    val top = max(a.top, b.top)
    val right = min(a.right, b.right)
    val bottom = min(a.bottom, b.bottom)
    val w = right - left
    val h = bottom - top
    return if (w > 0f && h > 0f) w * h else 0f
}

// Translate a Rect by an Offset (helper for drag projection)
private fun Rect.translateBy(offset: Offset): Rect =
    Rect(left + offset.x, top + offset.y, right + offset.x, bottom + offset.y)

// Simple drag session holder kept at file level (per composition instance it resets on recomposition of modifier)
private object dragSession {
    var currentDragId: Int? = null
    var currentOffset: Offset? = null
    var dropOverTargetId: Int? = null
    var lastHoverTargetId: Int? = null
    var hoverAccumulatedMs: Long? = null
    var lastEventUptimeMs: Long? = null
    var lastOverlapRatio: Float = 0f

    fun reset() {
        currentDragId = null
        currentOffset = null
        dropOverTargetId = null
        lastHoverTargetId = null
        hoverAccumulatedMs = null
        lastEventUptimeMs = null
        lastOverlapRatio = 0f
    }
}
*/

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DragAndDropAndStackTheme {
        Greeting("Android")
    }
}
