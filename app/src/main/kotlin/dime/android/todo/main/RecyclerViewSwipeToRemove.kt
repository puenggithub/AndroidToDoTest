package dime.android.todo.main

import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View

/** 
 * Created by dime on 14/02/16.
 */
class RecyclerViewSwipeToRemove(private val swipeListener: SwipeListener) : RecyclerView.OnItemTouchListener {

    companion object {
        private val LOCK_MIN_DISTANCE = 20
        private val DEFAULT_MIN_DISTANCE = 200
    }

    private var downX = 0.0f
    private var downY = 0.0f
    private var eventLocked = false
    private var scrollOrientation: ScrollOrientation? = null
    private var childView: View? = null

    /* The min distance need for the swipe. It should be half of the whole width of the recycler view */
    private var minDistance = -1

    private enum class ScrollOrientation { HORIZONTAL, VERTICAL }

    init { minDistance = DEFAULT_MIN_DISTANCE }

    fun recalculateMinDistance(recyclerViewWidth: Int) {
        minDistance = recyclerViewWidth / 4
    }

    override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                eventLocked = false
                childView = recyclerView.findChildViewUnder(event.x, event.y)

                /* Allow other events like Click to be processed */
                return false
            }
            MotionEvent.ACTION_MOVE -> {

                if (eventLocked && scrollOrientation == ScrollOrientation.HORIZONTAL) {
                    return true
                } else if (eventLocked && scrollOrientation == ScrollOrientation.VERTICAL) {
                    return false
                }

                /* Get the deltas */
                val deltaX = downX - event.x
                val deltaY = downY - event.y

                /* If we have horizontal scroll - lock the event */
                if (Math.abs(deltaX) > LOCK_MIN_DISTANCE) {
                    eventLocked = true
                    scrollOrientation = ScrollOrientation.HORIZONTAL
                    return true
                }

                if (Math.abs(deltaY) > LOCK_MIN_DISTANCE) {
                    eventLocked = true
                    scrollOrientation = ScrollOrientation.VERTICAL
                    return false
                }
                if (eventLocked && scrollOrientation == ScrollOrientation.HORIZONTAL) {
                    return true
                } else if (eventLocked && scrollOrientation == ScrollOrientation.VERTICAL) {
                    return false
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (eventLocked && scrollOrientation == ScrollOrientation.HORIZONTAL) {
                return true
            } else if (eventLocked && scrollOrientation == ScrollOrientation.VERTICAL) {
                return false
            }
        }
        return false
    }

    override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
        childView ?: return

        val deltaX = downX - event.x
        when (event.action) {
            MotionEvent.ACTION_MOVE -> swipeListener.swipeInProgress(childView!!, deltaX)
            MotionEvent.ACTION_CANCEL -> swipeListener.swipeCanceled(childView!!, deltaX)
            MotionEvent.ACTION_UP ->
                if (Math.abs(deltaX) > minDistance) {
                    swipeListener.swipeDone(childView!!, deltaX)
                } else {
                    swipeListener.swipeCanceled(childView!!, deltaX)
                }
        }
    }
}

/**
 * The Swipe Listener
 */
interface SwipeListener {
    /**
     * Called when the swipe has been canceled (deltaX < MIN_DISTANCE)
     *
     * @param v The view on which the swipe has been happening
     */
    fun swipeCanceled(v: View, deltaX: Float)

    /**
     * Called when the swipe has been done.
     *
     * @param v The view on which the swipe has been happening
     */
    fun swipeDone(v: View, deltaX: Float)

    /**
     * Called for every move motion action on the given view.
     *
     * @param v
     * @param deltaX
     */
    fun swipeInProgress(v: View, deltaX: Float)
}

