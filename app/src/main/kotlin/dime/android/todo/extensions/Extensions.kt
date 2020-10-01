package dime.android.todo.extensions

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dime.android.todo.App

/**
 * Extensions useful for the whole project
 *
 * Created by dime on 06/02/16.
 */

/**
 * Adds the inflate fun to the ViewGroup class
 */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false) =
    LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)

/**
 * Add the app in the context class
 */
val Context.app: App
    get() = applicationContext as App

/**
 * Runs the given lambda and returns true
 */
inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

/**
 * Executes the lambda expression if the boolean is true
 */
inline fun <R> Boolean.doIfTrue(f: (Boolean) -> R): R? = if (this) f(this) else null


/**
 * Adds the snack() extension function to the View, that displays a snack on that view
 */
inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, length).show()
}

/**
 * Adds the snack() extension function to the View, that displays a snack on that view
 */
inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, callback: Snackbar.Callback? = null, f: Snackbar.() -> Unit): Snackbar {
    val snack = Snackbar.make(this, message, length)
    snack.setCallback(callback)
    snack.f()
    snack.show()
    return snack
}

/**
 * Adds the action() function to the Snackbar, that adds an action to that snackbar
 */
fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}


/**
 * Moves the element from the 'fromPosition' to the 'toPosition'
 */
fun <T> MutableList<T>.move(fromPosition: Int, toPosition: Int) {
    val removed = removeAt(fromPosition);
    add(toPosition, removed)
}