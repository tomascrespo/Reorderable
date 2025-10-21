# Reorderable and Stackable

This is a fork of the great job done by Calvin Liang. I use his library in a project but I also need to drag items over other items. I tried to accomplish this without modifiying its library, using wrapped modifiers, but I did not success, so I have edited the library inserting some hooks.


<table width="100%" align="center">
  <tbody>
    <tr>
      <td width="50%">
        <img
          src="reorderandstack.webp"
          width="320"
          alt="A video showing some items being reordered and some being stacked"
        />
      </td>
    </tr>
  </tbody>
</table>



## Features

- Same cababilities as Calvin-LL/Reorderable
- You can drag and drop items over other items
- Adjustable behaviour through two variables, necessary overlap % to stack an element and ms time to move an element

## Usage

You have a complete example in MainActivity.kt (the one in the gif)

```kotlin
val overlapThreshold = 0.8f      // When 80% of overlap the target item will not move
val hoverDelayMs = 800L          // If overlapping < 80% the target item will move after 800ms (this delay allows easy movements)

val reorderableLazyGridState = rememberReorderableLazyGridState(
        lazyGridState,
        onMove = { from, to ->
            list = list.toMutableList().apply {
                this[to.index] = this[from.index].also {
                    this[from.index] = this[to.index]
                }
            }
        },
        canDragOver = { dragKey, overKey, ratioFromLib ->
            val dragId = (dragKey as? Int) ?: return@rememberReorderableLazyGridState true
            val overId = (overKey as? Int) ?: return@rememberReorderableLazyGridState true

            val now = android.os.SystemClock.uptimeMillis()
            val elapsed = now - lastUptime
            lastUptime = now

            val current = if (lastHoverTarget == overId) {
                (hoverAccumByTarget[overId] ?: 0L) + elapsed
            } else 0L
            hoverAccumByTarget[overId] = current
            lastHoverTarget = overId

            when {
                ratioFromLib >= overlapThreshold -> false
                current < hoverDelayMs -> false
                else -> true
            }
        },
        onDropOver = { dragKey, overKey ->
            val dragId = (dragKey as? Int) ?: return@rememberReorderableLazyGridState
            val overId = (overKey as? Int) ?: return@rememberReorderableLazyGridState
            val draggedItem = list.firstOrNull { it.id == dragId }
            val overItem = list.firstOrNull { it.id == overId }
            coroutineScope.launch {
                val message = if (draggedItem != null && overItem != null) {
                    overItem.text += "," + draggedItem.text
                    list = list.toMutableList().apply {
                        remove(draggedItem) }.toList()
                    "${draggedItem.text} soltado sobre ${overItem.text}"

                } else {
                    "item $dragId soltado sobre item $overId"
                }
                //snackbarHostState.showSnackbar(message)
                // Reset hover/timing state after handling a drop to avoid stale 800ms carry-over
                hoverAccumByTarget.clear()
                lastHoverTarget = null
                lastUptime = android.os.SystemClock.uptimeMillis()
            }
        }
    )

```
 
If you do not want stackable behaviour simply do not set canDragOver and onDropOver. This way you can have different grids with the same library, one with stackable behaviour and other without it.

