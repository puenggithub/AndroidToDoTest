package dime.android.todo.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBar.LayoutParams
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import dime.android.todo.R
import dime.android.todo.db.Task
import dime.android.todo.db.database
import dime.android.todo.extensions.doIfTrue
import dime.android.todo.extensions.snack
import dime.android.todo.main.TaskListAdapter
import org.jetbrains.anko.appcompat.v7.linearLayoutCompat
import org.jetbrains.anko.find

/**
 * Created by dime on 06/02/16.
 */

class EditActivity: AppCompatActivity() {
    //
    // region Companion object
    //

    companion object {
        val REQUEST_CODE = 23
        val EXTRA_TASK_ID = "taskID"
    }

    //
    // endregion Companion object
    //


    //
    // region Properties
    //

    private val rootLayout by lazy { find<ViewGroup>(R.id.root_layout) }
    private val txtName by lazy { find<EditText>(R.id.txt_name) }
    private val saveButton by lazy { find<ImageButton>(R.id.save) }
    private val cancelButton by lazy { find<ImageButton>(R.id.cancel) }
    private val priorityButtons by lazy {
        arrayOf(find<ImageButton>(R.id.low_priority), find<ImageButton>(R.id.normal_priority), find<ImageButton>(R.id.high_priority))
    }
    private var selectedPriority: Task.Priority? = null

    // The task that is currently edited
    private var task: Task? = null

    //
    // endregion Properties
    //


    //
    // region Activity's lifecycle
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_todo)

        // Setup the action bar
        supportActionBar?.setHomeButtonEnabled(false)

        val actionBarView = layoutInflater.inflate(R.layout.new_edit_action_bar, null)
        supportActionBar?.setCustomView(actionBarView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM

        // Setup the UI components
        saveButton.setOnClickListener { save() }
        cancelButton.setOnClickListener { cancel() }
        priorityButtons.forEachIndexed { i, imageButton ->
            val priority = Task.Priority.values().first { it.integer == i }
            imageButton.setOnClickListener { selectPriority(priority) }
            imageButton.setColorFilter(resources.getColor(TaskListAdapter.priorityColors[priority]!!));
        }
        txtName.setOnEditorActionListener { textView, actionId, keyEvent ->
            (actionId == EditorInfo.IME_ACTION_DONE).doIfTrue { save(); true } ?: false
        }

        // Load an existing task?
        intent.extras?.getInt(EXTRA_TASK_ID)?.let { database.findTaskById(it) }?.let {
            txtName.setText(it.name)
            txtName.setSelection(it.name.length)
            selectPriority(it.priority)
            task = it
        } ?: selectPriority(Task.Priority.NORMAL)
    }
    
    //
    // endregion Activity's lifecycle
    //

    //
    // region Private functions
    //

    /**
     * Finishes the current activity and sets the result to cancel
     */
    private fun selectPriority(priority: Task.Priority) {
        if (selectedPriority != priority) {
            // Clear the previous selection
            selectedPriority?.let { priorityButtons[it.integer].setBackgroundResource(android.R.color.transparent) }

            // Set the new priority
            priorityButtons[priority.integer].setBackgroundResource(R.drawable.priority_button_bg)
            selectedPriority = priority
        }
    }

    private fun cancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun save() {
        // Data validation
        if (txtName.text.isNullOrBlank()) {
            rootLayout.snack(getString(R.string.name_cannot_be_empty))
            return
        }

        // Finishes this activity and sends an OK result
        fun finishActivity(task: Task) {
            // Send back the id of the updated/added task
            val intent = Intent()
            intent.putExtra(EXTRA_TASK_ID, task.id)
            setResult(RESULT_OK, intent)
            finish()
        }

        // Are we editing or this is a new task?
        val unwrappedTask = task ?: run {
            // We need to add the task as a new task in the database
            database.addTask(Task(null, txtName.text.toString(), selectedPriority?.integer ?: Task.Priority.NORMAL.integer, 0))?.let {
                finishActivity(it)
            } ?: rootLayout.snack(getString(R.string.error_while_saving))

            // We need to return from the save() function
            return
        }

        // The unwrappedTask is the existing task, and we just need to update it
        unwrappedTask.name = txtName.text.toString()
        unwrappedTask.priority = selectedPriority ?: Task.Priority.NORMAL
        database.updateTask(unwrappedTask).doIfTrue { finishActivity(unwrappedTask) } ?: rootLayout.snack(getString(R.string.error_while_saving))
    }

    //
    // endregion Private functions
    //
}