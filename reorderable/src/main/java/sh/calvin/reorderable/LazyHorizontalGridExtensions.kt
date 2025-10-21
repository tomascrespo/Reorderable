package sh.calvin.reorderable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * High-level wrapper to build a LazyHorizontalGrid with reorder and optional stacking.
 *
 * The [content] lambda is invoked within [ReorderableCollectionItemScope] so the caller
 * can use [ReorderableCollectionItemScope.draggableHandle] directly for the drag handle.
 */
@Composable
fun <T> ReorderableLazyHorizontalGrid(
    items: List<T>,
    key: (T) -> Any,
    rows: GridCells,
    modifier: Modifier = Modifier,
    state: androidx.compose.foundation.lazy.grid.LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    horizontalArrangement: androidx.compose.foundation.layout.Arrangement.Horizontal = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    verticalArrangement: androidx.compose.foundation.layout.Arrangement.Vertical = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
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

    LazyHorizontalGrid(
        rows = rows,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
    ) {
        itemsIndexed(items, key = { _, item -> key(item) }) { _, item ->
            ReorderableItem(reorderableState, key(item)) { isDragging ->
                this.content(item, isDragging)
            }
        }
    }
}
