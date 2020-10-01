package dime.android.todo.main

import android.support.v7.widget.DefaultItemAnimator

/**
 * Created by dime on 05/11/14.
 */
class ToDoListAnimator : DefaultItemAnimator() {
    override fun getRemoveDuration() = 10000000L
}
