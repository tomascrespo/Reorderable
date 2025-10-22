package sh.calvin.reorderable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Remember a ReorderableLazyListState configured for either reorder-only or reorder+stack behavior.
 * Maps the library's onMove callback to (fromIndex, toIndex) for convenience at call sites.
 */
@Composable
fun rememberReorderableLazyListStateConfigured(
    lazyListState: LazyListState,
    stackingMode: StackingMode,
    overlapThreshold: Float,
    hoverDelayMs: Long,
    scrollMoveMode: ScrollMoveMode = ScrollMoveMode.INSERT,
    onMoveIndices: (fromIndex: Int, toIndex: Int) -> Unit,
    onDropOver: ((dragKey: Any, overKey: Any) -> Unit)? = null,
): ReorderableLazyListState {
    val canDragOver = rememberStackingCanDragOver(
        overlapThreshold = overlapThreshold,
        hoverDelayMs = hoverDelayMs,
        stackingMode = stackingMode,
    )

    val dropThreshold = when (stackingMode) {
        StackingMode.Disabled -> null
        StackingMode.Enabled -> overlapThreshold
    }

    return rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from: LazyListItemInfo, to: LazyListItemInfo ->
            onMoveIndices(from.index, to.index)
        },
        scrollMoveMode = scrollMoveMode,
        canDragOver = canDragOver,
        onDropOver = onDropOver,
        dropOverlapThreshold = dropThreshold,
    )
}

/**
 * High-level wrapper to build a LazyRow with reorder and optional stacking.
 *
 * The [content] lambda is invoked within [ReorderableCollectionItemScope] so the caller
 * can use [ReorderableCollectionItemScope.draggableHandle] directly for the drag handle.
 */
@Composable
fun <T> ReorderableLazyRow(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    horizontalArrangement: androidx.compose.foundation.layout.Arrangement.Horizontal = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    stackingMode: StackingMode = StackingMode.Enabled,
    overlapThreshold: Float = 0.7f,
    hoverDelayMs: Long = 500L,
    scrollMoveMode: ScrollMoveMode = ScrollMoveMode.INSERT,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onDropOver: ((dragKey: Any, overKey: Any) -> Unit)? = null,
    content: @Composable ReorderableCollectionItemScope.(item: T, isDragging: Boolean) -> Unit,
) {
    val reorderableState = rememberReorderableLazyListStateConfigured(
        lazyListState = state,
        stackingMode = stackingMode,
        overlapThreshold = overlapThreshold,
        hoverDelayMs = hoverDelayMs,
        scrollMoveMode = scrollMoveMode,
        onMoveIndices = onMove,
        onDropOver = onDropOver,
    )

    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        itemsIndexed(items, key = { _, item -> key(item) }) { _, item ->
            ReorderableItem(reorderableState, key(item)) { isDragging ->
                this.content(item, isDragging)
            }
        }
    }
}
