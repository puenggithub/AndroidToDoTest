package dime.android.todo.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import dime.android.todo.R
import dime.android.todo.db.Task
import dime.android.todo.db.database
import dime.android.todo.main.TaskListAdapter

class ToDoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?) = ToDoWidgetViewsFactory(applicationContext)
}

class ToDoWidgetViewsFactory(val context: Context): RemoteViewsService.RemoteViewsFactory {

    // The items
    private var tasks = listOf<Task>()

    override fun onCreate() { }

    override fun onDataSetChanged() {
        tasks = context.database.uncompletedTasks()
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val task = tasks[position]
        val view = RemoteViews(context.packageName, R.layout.todo_widget_item)
        view.setTextViewText(R.id.task_name, task.name)
        view.setInt(R.id.priority_color, "setBackgroundResource", TaskListAdapter.priorityColors[task.priority]!!)
        view.setOnClickFillInIntent(R.id.todo_widget_list_item, Intent())
        return view
    }

    override fun getCount() = tasks.size
    override fun getViewTypeCount() = 1
    override fun onDestroy() {}
    override fun hasStableIds() = true
    override fun getLoadingView() = null
    override fun getItemId(position: Int) = position.toLong()
}
