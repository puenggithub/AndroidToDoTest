package dime.android.todo.db

/**
 * Represents a single Task
 *
 * Created by dime on 06/02/16.
 */
class Task(var id: Int?, var name: String, var priorityInt: Int, var completedInt: Int) {

    // The enum representing the priority
    enum class Priority(val integer: Int) { LOW(0), NORMAL(1), HIGH(2) }

    // The priority of the task
    var priority: Priority
        get() = Priority.values().first { it.integer == priorityInt }
        set(value) { priorityInt = value.integer }

    // The completed flag of the task
    var completed: Boolean
        get() = completedInt == 1
        set(value) { completedInt = if (value) 1 else 0 }

    /**
     * Equals
     */
    override fun equals(other: Any?) = if (other is Task) other.id == id else false
}
