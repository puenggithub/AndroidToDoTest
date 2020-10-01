package dime.android.todo.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.TextView
import dime.android.todo.App
import dime.android.todo.R
import dime.android.todo.db.Task
import dime.android.todo.db.database
import dime.android.todo.edit.EditActivity
import dime.android.todo.extensions.action
import dime.android.todo.extensions.snack
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivityForResult


/**
 * Created by dime on 05/02/16.
 */
class MainActivity: AppCompatActivity(), SwipeListener {

    //
    // region Properties
    //

    private val app by lazy { application as App }
    private val rootView by lazy { find<ViewGroup>(R.id.root_view) }
    private val emptyList by lazy { find<View>(R.id.empty_list) }
    private val addButton by lazy { find<FloatingActionButton>(R.id.new_todo) }
    private val recyclerView by lazy { find<RecyclerView>(R.id.task_list_new) }

    private val adapter by lazy { TaskListAdapter(this) }

    // The UNDO snackbar
    private var undoSnack: Snackbar? = null
    // The SnackBar callback
    private val snackBarCallback = object: Snackbar.Callback() {
        override fun onDismissed(snackbar: Snackbar?, event: Int) {
            undoSnack = null
            undoStack.clear()
        }
    }
    // The list of tasks waiting to be permanently deleted
    private val undoStack = mutableListOf<Task>()

    //
    // endregion Properties
    //

    //
    // region Activity's lifecycle
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_list)

        // Enable the home button on the action bar
        supportActionBar?.setHomeButtonEnabled(true)

        // Setup the adapter
        adapter.errorDelegate = { rootView.snack(it) }
        adapter.taskClickListener = { startActivityForResult<EditActivity>(EditActivity.REQUEST_CODE, EditActivity.EXTRA_TASK_ID to it) }
        adapter.dataChangedListener = {
            emptyList.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.INVISIBLE
            app.updateWidget()
        }
        adapter.refreshDataFromDB()

        // Setup the Recycler view
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = ToDoListAnimator()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
        recyclerView.adapter = adapter

        // The swipe to remove
        val swipeToRemove = RecyclerViewSwipeToRemove(this)
        recyclerView.addOnItemTouchListener(swipeToRemove)
        find<ViewGroup>(android.R.id.content).viewTreeObserver.addOnGlobalLayoutListener {
            swipeToRemove.recalculateMinDistance(recyclerView.measuredWidth)
        }

        // Setup the other UI components
        addButton.setOnClickListener { startActivityForResult<EditActivity>(EditActivity.REQUEST_CODE) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EditActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the list
            adapter.refreshDataFromDB()
        }
    }

    //
    // endregion Activity's lifecycle
    //

    //
    // region Swipe listener
    //

    override fun swipeCanceled(v: View, deltaX: Float) {
        if (v == null || v.tag == null || deltaX > 0) return

        // Get the view holder
        val vh = v.tag as TaskListAdapter.ViewHolder

        vh.foregroundLayer.animate().x(0f).setInterpolator(BounceInterpolator()).withEndAction {
            vh.foregroundLayer.x = 0f
            vh.foregroundLayer.requestLayout()
        }.start()
    }

    override fun swipeDone(v: View, deltaX: Float) {
        if (v == null || v.tag == null || deltaX > 0) return

        // Get the view holder
        val vh = v.tag as TaskListAdapter.ViewHolder

        vh.foregroundLayer.animate().x(v.measuredWidth.toFloat()).setInterpolator(AccelerateInterpolator()).withEndAction {
            fadeOutAndRemoveTask(vh)
        }.start()
    }

    override fun swipeInProgress(v: View, deltaX: Float) {
        if (v == null || v.tag == null || deltaX > 0) return

        // Get the view holder
        val vh = v.tag as TaskListAdapter.ViewHolder
        vh.foregroundLayer.x = -deltaX
        vh.foregroundLayer.requestLayout()
    }

    //
    // endregion Swipe listener
    //

    //
    // region Private functions
    //

    private fun undoDelete() {
        // Add all tasks back to the database
        undoStack.forEach { database.addTask(it) }
        undoStack.clear()
        adapter.refreshDataFromDB()
    }

    private fun fadeOutAndRemoveTask(viewHolder: TaskListAdapter.ViewHolder) = viewHolder.itemView.animate().withEndAction {
        // Delete the task from the DB
        adapter.removeTaskAtPosition(viewHolder.adapterPosition)?.let {
            // Add the task to the stack, so we can UNDO what we've done here
            undoStack.add(it)

            // Create the snack if it doesn't exist
            if (undoSnack == null) {
                undoSnack = rootView.snack(getString(R.string.deleted_one_task), callback = snackBarCallback) {
                    action(getString(R.string.undo), color = Color.YELLOW) {
                        undoDelete()
                    }
                }
            } else {
                val textView = undoSnack!!.view.find<TextView>(android.support.design.R.id.snackbar_text)
                textView.text = getString(R.string.deleted_more_tasks).format(undoStack.size)
                undoSnack!!.show()
            }
        } ?: rootView.snack(getString(R.string.error_while_updating))

        // Move everything back in it's place
        viewHolder.foregroundLayer.x = 0f
        viewHolder.foregroundLayer.requestLayout()
        viewHolder.itemView.alpha = 1f
    }.alpha(0f)

    //
    // endregion Private functions
    //
}