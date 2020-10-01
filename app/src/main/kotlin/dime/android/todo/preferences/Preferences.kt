package dime.android.todo.preferences

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceActivity

import dime.android.todo.R
import dime.android.todo.widget.ToDoWidgetService

class Preferences : PreferenceActivity(), OnSharedPreferenceChangeListener {

    companion object {
        val REQUEST_CODE = 22
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onResume() {
        super.onResume()

        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Update the widget
        applicationContext.startService(Intent(this.applicationContext, ToDoWidgetService::class.java))
    }
}
