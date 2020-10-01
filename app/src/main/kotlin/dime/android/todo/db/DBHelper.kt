package dime.android.todo.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

/**
 * Created by dime on 06/02/16.
 */

class DBHelper(context: Context): ManagedSQLiteOpenHelper(context, "todo.db", null, 2) {

    // The table columns
    private val table = "todo"
    private val id = "id"
    private val name = "name"
    private val priority = "priority"
    private val completed = "completed"
    
    // Our custom parser
    private val parser = rowParser { id: Int, name: String, priority: Int, completed: Int -> Task(id, name, priority, completed) }

    /**
     * Singleton pattern
     */
    companion object {
        // The single instance
        private var instance: DBHelper? = null

        // The lock object
        private val lock = Object()

        // Returns the single instance of this object
        fun getInstance(ctx: Context) = synchronized(lock) {
            if (instance == null) instance = DBHelper(ctx.applicationContext)
            instance!!
        }
     }

    /**
     * Creates the table with the given table name
     */
    private fun createTable(db: SQLiteDatabase, tableName: String) {
        db.createTable(tableName, true,
                id to INTEGER + PRIMARY_KEY,
                name to TEXT,
                priority to INTEGER,
                completed to INTEGER)
    }

    /**
     * Called on database creating. Here the tables are created
     */
    override fun onCreate(db: SQLiteDatabase) {
        createTable(db, table)
    }

    /**
     * Called on database upgrade.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // If we are upgrading from 1 to 2 we need to create the new table,
        // copy all the data and drop the old table
        if (oldVersion != 1 || newVersion != 2) return

        // The temp constants
        val newTableName = "${table}_temp"
        val columns = "$id, $name, $priority, $completed"
        // Create the new table with a temp table name
        createTable(db, newTableName)
        // Copy all the data
        db.execSQL("INSERT INTO $newTableName($columns) SELECT $columns FROM $table")
        // Drop the old table
        db.dropTable(table)
        // Rename the new table
        db.execSQL("ALTER TABLE $newTableName RENAME TO $table")
    }

    //
    // region Helper functions
    //

    /**
     * Adds the given task to the database.
     *
     * @param   task    -> The task that needs to be added to the database
     * @return  Task?   -> The added task if successful, null otherwise
     */
    fun addTask(task: Task) =
            use {
                insert(table, name to task.name, priority to task.priorityInt, completed to task.completedInt)
            }.let {
                return@let if (it == -1L) null else Task(it.toInt(), task.name, task.priorityInt, task.completedInt)
            }

    /**
     * Deletes the given task.
     *
     * @param task      -> The task that need to be removed from the database
     * @return Boolean  -> True if the removal was successful
     */
    fun deleteTask(task: Task) = if (task.id != null) use { delete(table, "$id = {id}", "id" to task.id!!) } > -1 else false

    /**
     * Deletes all the completed tasks
     *
     * @return  -> The number of deleted tasks
     */
    fun deleteCompleted() = use { delete(table, "$completed = 1") }

    /**
     * Returns the Task? with the given taskId
     *
     * @param   taskId  -> The id of the task we want to get
     * @return  The Task?
     */
    fun findTaskById(taskId: Int) = use { select(table).where("$id = {id}", "id" to taskId).exec { parseOpt(parser) } }

    /**
     * Updates the given task.
     *
     * @param task      -> The task that needs to be updated
     * @return Boolean  -> True if the task was successfully updated
     */
    fun updateTask(task: Task) =
            if (task.id != null)
                use {
                    update(table, name to task.name, priority to task.priorityInt, completed to task.completedInt)
                            .where("$id = {id}", "id" to task.id!!).exec()
                } == 1
            else false

    /**
     * Returns a list of all tasks
     */
    fun allTasks() = use { select(table).orderBy(completed).orderBy(priority, SqlOrderDirection.DESC).exec { parseList(parser) } }

    /**
     * Returns a list of all uncompleted tasks
     */
    fun uncompletedTasks() = use { select(table).where("$completed = 0").orderBy(priority, SqlOrderDirection.DESC).exec { parseList(parser) } }

    //
    // endregion Helper functions
    //
}

// Access property for Context
val Context.database: DBHelper
    get() = DBHelper.getInstance(applicationContext)
