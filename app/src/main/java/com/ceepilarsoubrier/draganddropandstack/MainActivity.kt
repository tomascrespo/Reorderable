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
import androidx.compose.foundation.layout.fillMaxSize
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        var lazyGridItems = (1..20).map { "Grid Item $it" }

        setContent {
            DragAndDropAndStackTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )

                    SimpleReorderableLazyVerticalGridScreen(snackbarHostState)
                }
            }
        }
    }
}

data class Item(val id: Int, val text: String, val size: Int)

@Composable
fun SimpleReorderableLazyVerticalGridScreen(
    snackbarHostState: SnackbarHostState,
) {

    val items = (0..200).map {
        Item(id = it, text = "Item #$it", size = if (it % 2 == 0) 70 else 100)
    }
    var list by remember { mutableStateOf(items) }
    val lazyGridState = rememberLazyGridState()
    val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        list = list.toMutableList().apply {
            this[to.index] = this[from.index].also {
                this[from.index] = this[to.index]
            }
        }
    }

    // Track item bounds in root for overlap calculations
    val itemBoundsById = remember { mutableStateMapOf<Int, Rect>() }
    val coroutineScope = rememberCoroutineScope()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = Modifier.fillMaxSize(),
        state = lazyGridState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(list, key = { _, item -> item.id }) { index, item ->
            ReorderableItem(reorderableLazyGridState, item.id) {
                val interactionSource = remember { MutableInteractionSource() }

                Card(
                    onClick = {},
                    modifier = Modifier
                        .height(96.dp)
                        .onGloballyPositioned { coordinates ->
                            itemBoundsById[item.id] = coordinates.boundsInRoot()
                        }
                        .semantics {
                            customActions = listOf(
                                CustomAccessibilityAction(
                                    label = "Move Before",
                                    action = {
                                        if (index > 0) {
                                            list = list.toMutableList().apply {
                                                add(index - 1, removeAt(index))
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                ),
                                CustomAccessibilityAction(
                                    label = "Move After",
                                    action = {
                                        if (index < list.size - 1) {
                                            list = list.toMutableList().apply {
                                                add(index + 1, removeAt(index))
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                ),
                            )
                        },
                    interactionSource = interactionSource,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        IconButton(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .draggableHandle(
                                    onDragStarted = {
                                    },
                                    onDragStopped = {
                                    },
                                    interactionSource = interactionSource,
                                )
                                .clearAndSetSemantics { },
                            onClick = {},
                        ) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Reorder")
                        }
                        Text(
                            item.text,
                            Modifier.align(Alignment.Center).padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}



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
