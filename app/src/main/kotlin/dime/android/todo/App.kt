package dime.android.todo

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.widget.RemoteViews
import com.crashlytics.android.Crashlytics
import dime.android.todo.widget.ToDoWidget
import dime.android.todo.widget.ToDoWidgetService
import io.fabric.sdk.android.Fabric

class App : Application() {

    private lateinit var appWidgetManager: AppWidgetManager

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())

        appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        updateWidget()
    }

    fun updateWidget() {
        val componentName = ComponentName(applicationContext, ToDoWidget::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_tasklist)
    }
}
