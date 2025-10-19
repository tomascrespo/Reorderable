package sh.calvin.reorderable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple mode selector for stacking behavior.
 */
enum class StackingMode { Disabled, Enabled }

/**
 * Build a canDragOver function that blocks displacement under high overlap and
 * requires a minimum hover duration before allowing displacement under low overlap.
 */
@Composable
fun rememberStackingCanDragOver(
    overlapThreshold: Float,
    hoverDelayMs: Long,
    stackingMode: StackingMode,
): (dragKey: Any, overKey: Any, overlapRatio: Float) -> Boolean {
    val hoverAccumByTarget = remember { mutableStateMapOf<Any, Long>() }
    var lastHoverTarget by remember { mutableStateOf<Any?>(null) }
    var lastDragKey by remember { mutableStateOf<Any?>(null) }
    var lastUptime by remember { mutableStateOf(android.os.SystemClock.uptimeMillis()) }

    return remember(overlapThreshold, hoverDelayMs, stackingMode) {
        { dragKey: Any, overKey: Any, ratio: Float ->
            // If stacking disabled, always allow displacement (pure reorder)
            if (stackingMode == StackingMode.Disabled) return@remember true

            val now = android.os.SystemClock.uptimeMillis()
            val elapsed = now - lastUptime
            lastUptime = now

            // New drag session => reset timers
            if (dragKey != lastDragKey) {
                hoverAccumByTarget.clear()
                lastHoverTarget = null
                lastDragKey = dragKey
            }

            // Accumulate hover time only while staying over the same target
            val current = if (lastHoverTarget == overKey) {
                (hoverAccumByTarget[overKey] ?: 0L) + elapsed
            } else 0L
            hoverAccumByTarget[overKey] = current
            lastHoverTarget = overKey

            when {
                // High overlap blocks displacement (so we can stack on drop)
                ratio >= overlapThreshold -> false
                // Low overlap: require dwell time before allowing displacement
                current < hoverDelayMs -> false
                else -> true
            }
        }
    }
}

/**
 * Remember a ReorderableLazyGridState configured for either reorder-only or reorder+stack behavior.
 * Maps the library's onMove callback to (fromIndex, toIndex) for convenience at call sites.
 */
@Composable
fun rememberReorderableLazyGridStateConfigured(
    lazyGridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    stackingMode: StackingMode,
    overlapThreshold: Float,
    hoverDelayMs: Long,
    scrollMoveMode: ScrollMoveMode = ScrollMoveMode.INSERT,
    onMoveIndices: (fromIndex: Int, toIndex: Int) -> Unit,
    onDropOver: ((dragKey: Any, overKey: Any) -> Unit)? = null,
): ReorderableLazyGridState {
    val canDragOver = rememberStackingCanDragOver(
        overlapThreshold = overlapThreshold,
        hoverDelayMs = hoverDelayMs,
        stackingMode = stackingMode,
    )

    val dropThreshold = when (stackingMode) {
        StackingMode.Disabled -> null
        StackingMode.Enabled -> overlapThreshold
    }

    return rememberReorderableLazyGridState(
        lazyGridState = lazyGridState,
        scrollMoveMode = scrollMoveMode,
        onMove = { from: LazyGridItemInfo, to: LazyGridItemInfo ->
            onMoveIndices(from.index, to.index)
        },
        canDragOver = canDragOver,
        onDropOver = onDropOver,
        dropOverlapThreshold = dropThreshold,
    )
}

/**
 * High-level wrapper to build a LazyVerticalGrid with reorder and optional stacking.
 *
 * The [content] lambda is invoked within [ReorderableCollectionItemScope] so the caller
 * can use [ReorderableCollectionItemScope.draggableHandle] directly for the drag handle.
 */
@Composable
fun <T> ReorderableLazyVerticalGrid(
    items: List<T>,
    key: (T) -> Any,
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: androidx.compose.foundation.lazy.grid.LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    verticalArrangement: androidx.compose.foundation.layout.Arrangement.Vertical = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    horizontalArrangement: androidx.compose.foundation.layout.Arrangement.Horizontal = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    stackingMode: StackingMode = StackingMode.Enabled,
    overlapThreshold: Float = 0.7f,
    hoverDelayMs: Long = 500L,
    scrollMoveMode: ScrollMoveMode = ScrollMoveMode.INSERT,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onDropOver: ((dragKey: Any, overKey: Any) -> Unit)? = null,
    content: @Composable ReorderableCollectionItemScope.(item: T, isDragging: Boolean) -> Unit,
) {
    val reorderableState = rememberReorderableLazyGridStateConfigured(
        lazyGridState = state,
        stackingMode = stackingMode,
        overlapThreshold = overlapThreshold,
        hoverDelayMs = hoverDelayMs,
        scrollMoveMode = scrollMoveMode,
        onMoveIndices = onMove,
        onDropOver = onDropOver,
    )

    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
    ) {
        itemsIndexed(items, key = { _, item -> key(item) }) { _, item ->
            ReorderableItem(reorderableState, key(item)) { isDragging ->
                this.content(item, isDragging)
            }
        }
    }
}

