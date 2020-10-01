package dime.android.todo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import dime.android.todo.R
import dime.android.todo.main.MainActivity

class ToDoWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update all of the widgets
        for (widgetId in appWidgetIds) {

            // The intent that starts the service and add the extra data
            val intent = Intent(context, ToDoWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            // Init the remote views
            val remoteViews = RemoteViews(context.packageName, R.layout.todo_widget)
            remoteViews.setRemoteAdapter(R.id.widget_tasklist, intent)

            // Click
            val mainActivityIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0)
            remoteViews.setPendingIntentTemplate(R.id.widget_tasklist, pendingIntent)
            remoteViews.setOnClickPendingIntent(R.id.empty_view, pendingIntent)

            // The empty view
            remoteViews.setEmptyView(R.id.widget_tasklist, R.id.empty_view)

            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}
