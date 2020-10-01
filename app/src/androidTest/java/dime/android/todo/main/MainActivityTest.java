package dime.android.todo.main;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.LargeTest;
import android.view.View;

import android.widget.TextView;
import dime.android.todo.R;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.*;
import android.support.v7.widget.RecyclerView;
import org.hamcrest.Matcher;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.*;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new  ActivityTestRule<>(MainActivity.class);

    @BeforeClass //Clear database before start testing
    public static void beforeClass() throws Exception {
        InstrumentationRegistry.getTargetContext().deleteDatabase("todo.db");
    }

    @Test //Check if user opens the app, user should see the task list and add button
    public void Test1_checkObject() throws Exception {
        //check if program name "To-Do List" displays
        onView(withText(R.string.app_name))
                .check(matches(isDisplayed()));
        //check if the list layout displays
        onView(withId(R.id.task_list_new))
                .check(matches(isDisplayed()));
        //check if add new task button displays
        onView(withId(R.id.new_todo))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));
    }

    @Test //Test add a new task and check form
    public void Test2_createNewTask() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        onView(withId(R.id.new_todo))
                .perform(click());
        //check if new task form displays
        onView(withId(R.id.decor_content_parent))
                .check(matches(isDisplayed()));
        //check if action bar, save, cancel, text box, priority icon button display
        onView(withId(R.id.action_bar_container))
                .check(matches(isDisplayed()));
        onView(withId(R.id.save))
                .check(matches(isDisplayed()));
        onView(withId(R.id.cancel))
                .check(matches(isDisplayed()));
        onView(withId(R.id.low_priority))
                .check(matches(isDisplayed()));
        onView(withId(R.id.normal_priority))
                .check(matches(isDisplayed()));
        onView(withId(R.id.high_priority))
                .check(matches(isDisplayed()));
        //fill in new task name
        onView(withId(R.id.txt_name))
                .check(matches(isDisplayed()))
                .perform(typeText("One"));
        //click save to create new task
        onView(withId(R.id.save))
                .perform(click());
        //check new task is created correctly
        onView(withId(R.id.list_item_layout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.task_name))
                .check(matches(isDisplayed()));
        onView(withId(R.id.priority_image))
                .check(matches(isDisplayed()));
        onView(withId(R.id.done_checkbox))
                .check(matches(isDisplayed()));
    }
    @Test //Test add a new task with empty task name
    public void Test3_createNewEmptyTask() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        onView(withId(R.id.new_todo))
                .perform(click());
        onView(withId(R.id.save))
                .check(matches(isDisplayed()))
                .perform(click());
        //change if error message text displays
        onView(withId(android.support.design.R.id.snackbar_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("The name of the task cannot be empty!")));
    }

    @Test //Verify if user clicks cancel button, new task will not be created
    public void Test4_createAndCancel() throws Exception {
        //clear list
        beforeClass();
        int countA = RecyclerViewCount();
        //add a new task
        onView(withId(R.id.new_todo))
                .perform(click());
        //click to cancel action
        onView(withId(R.id.cancel))
                .check(matches(isDisplayed()))
                .perform(click());

        //check if number of list is not changed
        int countB = RecyclerViewCount();
        if (countA != countB) {
            fail();
        }
    }

    @Test //Test edit the task
    public void Test5_editTask() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        AddNewTask("One");
        //click on task name to edit
        onView(withId(R.id.task_name))
                .check(matches(isDisplayed()))
                .perform(click());
        //change if task name exists
        onView(withId(R.id.txt_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("One")))
                .perform(replaceText("Two"));
        //change priority from normal to high
        onView(withId(R.id.high_priority))
                .check(matches(isDisplayed()))
                .perform(click());
        //click save to create new task
        onView(withId(R.id.save))
                .perform(click());
        //check new task is created correctly
        onView(withId(R.id.list_item_layout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.task_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("Two")));
        onView(withId(R.id.priority_image))
                .check(matches(isDisplayed()));
        onView(withId(R.id.done_checkbox))
                .check(matches(isDisplayed()));
    }

    @Test //Verify if user edit task name and click cancel button, task name will not be changed
    public void Test6_editAndCancel() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        AddNewTask("Sky");
        //click on task name to edit
        onView(withId(R.id.task_name))
                .check(matches(isDisplayed()))
                .perform(click());
        //change if task name exists
        onView(withId(R.id.txt_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sky")))
                .perform(replaceText("Sea"));
        //click to cancel action
        onView(withId(R.id.cancel))
                .check(matches(isDisplayed()))
                .perform(click());
        //change if task name is still not changed
        onView(withId(R.id.task_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sky")));
    }

    @Test //Test create and delete one existing task, check if list is empty after deleting
    public void Test7_deleteOneTask() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        AddNewTask("January");
        //swipe to delete task
        onView(withId(R.id.list_item_layout))
                .check(matches(isDisplayed()))
                .perform(ViewActions.swipeRight());
        Thread.sleep(3000);
        //change if undo text display
        onView(withId(android.support.design.R.id.snackbar_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("Deleted one task")));
        //check if undo button displays
        onView(withId(R.id.snackbar_action))
                .check(matches(isDisplayed()));
        //check if it is empty list
        onView(withId(R.id.list_item_layout))
                .check(doesNotExist());
        int count = RecyclerViewCount();
        if (count > 0) {
            fail();
        }
        onView(withId(R.id.empty_list))
                .check(matches(isDisplayed()))
                .check(matches(withText("Move along, nothing to see here!")));
    }

    @Test //Test create and delete two existing task, check if list is empty after deleting
    public void Test8_deleteTwoTask() throws Exception {
        //clear list
        beforeClass();
        //add first task
        AddNewTask("Red");
        //add two task
        AddNewTask("Blue");
        //swipe to delete all tasks
        for (int i = 0; i < 2; i++) {
            onView(allOf(withId(R.id.task_list_new), isDisplayed()))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.swipeRight()));
            Thread.sleep(1000);
        }

        Thread.sleep(1000);
        //change if undo text display
        onView(withId(android.support.design.R.id.snackbar_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("Deleted 2 tasks")));
        //check if undo button displays
        onView(withId(R.id.snackbar_action))
                .check(matches(isDisplayed()));
        //check if it is empty list
        onView(withId(R.id.list_item_layout))
                .check(doesNotExist());
        int count = RecyclerViewCount();
        if (count > 0) {
            fail();
        }
        onView(withId(R.id.empty_list))
                .check(matches(isDisplayed()))
                .check(matches(withText("Move along, nothing to see here!")));
    }


    @Test //Test undo delete
    public void Test9_undoDelete() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        AddNewTask("Hello");
        //swipe to delete task
        onView(withId(R.id.list_item_layout))
                .check(matches(isDisplayed()))
                .perform(ViewActions.swipeRight());
        Thread.sleep(3000);
        //change if undo text and button display
        onView(withId(android.support.design.R.id.snackbar_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("Deleted one task")));
        //click undo button
        onView(withId(R.id.snackbar_action))
                .check(matches(isDisplayed()))
                .perform(click());
        //check if task "Hello" comes back
        onView(withId(R.id.task_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("Hello")));
    }

    @Test //Test create multiple new tasks
    public void Test10_createMultiTasks() throws Exception {
        //clear list
        beforeClass();
        //add first task
        AddNewTask("Task01");
        //add second task
        AddNewTask("Task02");
        //add third task
        AddNewTask("Task03");
        //add fourth task
        AddNewTask("Task04");

        //check if there are 4 tasks in the list
        int count = RecyclerViewCount();
        if (count != 4) {
            fail();
        }
    }

    @Test //Test add duplicate task name
    public void Test11_addDuplicateTask() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        AddNewTask("Monday");
        //add a new task wth task name that already exists
        AddNewTask("Monday");
        //check if there are 5 tasks in the list
        int count = RecyclerViewCount();
        if (count != 2) {
            fail();
        }
    }

    @Test //Test click check box
    public void Test12_clickCheckbox() throws Exception {
        //clear list
        beforeClass();
        //add a new task
        onView(withId(R.id.new_todo))
                .perform(click());
        onView(withId(R.id.txt_name))
                .check(matches(isDisplayed()))
                .perform(typeText("Test"));
        onView(withId(R.id.save))
                .perform(click());
        //click on check box to mask task as completed task
        onView(withId(R.id.done_checkbox))
                .perform(click());
        //check if task is crossed out
        onView(withId(R.id.done_layer))
                .check(matches(isDisplayed()));
        //click on check box again to mask task as uncompleted task
        onView(withId(R.id.done_checkbox))
                .perform(click());
        //check if task is not crossed out
        onView(withId(R.id.done_layer))
                .check(matches(not(isDisplayed())));

    }

    //Verify priority order, change priority of task "Spring" from normal priority to high priority
    //task "Spring" is moved to top list
    @Test
    public void Test13_checkPriorityOrder() throws Exception {
        //clear list
        beforeClass();
        //add first task
        AddNewTask("Winter");
        //add second task
        AddNewTask("Spring");
        //check if "Spring" is second task
        assertTrue(CheckTaskPosition("Spring",1));
        //edit priority of task "Two" from normal priority to high priority
        onView(withId(R.id.task_list_new))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));  //click on second task
        //change task "Spring" priority from normal priority to high priority
        onView(withId(R.id.txt_name))
                .check(matches(withText("Spring")));
        onView(withId(R.id.high_priority))
                .perform(click());
        onView(withId(R.id.save))
                .perform(click());
        //check if "Spring" is changed to be first task
        assertTrue(CheckTaskPosition("Spring",0));
    }

    @Test //Verify if completed task is moved to the bottom of the list
    public void Test14_checkTaskOrder1() throws Exception {
        //clear list
        beforeClass();
        //add first task
        AddNewTask("Bangkok");
        //add second task
        AddNewTask("Chiang Mai");
        //add second task
        AddNewTask("Phuket");

        //check if "Bangkok" is first task
        assertTrue(CheckTaskPosition("Bangkok",0));
        //Thread.sleep(2000);

        //make "Bangkok" as completed task
        onView(withId(R.id.task_list_new)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0,
                        ChildViewAction.clickChildViewWithId(R.id.done_checkbox)))
                .perform(click());

        //check if "Bangkok" is at the bottom of the list
        int count = RecyclerViewCount()-1;
        assertTrue(CheckTaskPosition("Bangkok",count));
    }

    @Test //Verify if there are only completed task, when user adds a new task to the list, the new task should be on the top of the list
    public void Test15_checkTaskOrder2() throws Exception {
        //clear list
        beforeClass();
        //add first task
        AddNewTask("Daisy");
        //add second task
        AddNewTask("Rose");
        //add third task
        AddNewTask("Lily");

        //make all tasks as completed task
        for (int i = 0; i <3 ; i++) {
            onView(withId(R.id.task_list_new)).perform(
                    RecyclerViewActions.actionOnItemAtPosition(0,
                            ChildViewAction.clickChildViewWithId(R.id.done_checkbox)))
                    .perform(click());
        }
        //add Third task
        AddNewTask("Sakura");
        //check if "Sakura" is at the top of the list
        assertTrue(CheckTaskPosition("Sakura",0));
        Thread.sleep(2000);
    }


    public int RecyclerViewCount () {
        RecyclerView recyclerView = mActivityRule.getActivity().findViewById(R.id.task_list_new);
        int itemCount = recyclerView.getAdapter().getItemCount();
        return itemCount;
    }

    public void AddNewTask (String name)
    {
        onView(withId(R.id.new_todo))
                .perform(click());
        onView(withId(R.id.txt_name))
                .check(matches(isDisplayed()))
                .perform(typeText(name));
        onView(withId(R.id.save))
                .perform(click());
    }

    public boolean CheckTaskPosition (String expectedName, int position) {
        boolean result = true;
        RecyclerView recyclerView = mActivityRule.getActivity().findViewById(R.id.task_list_new);
        String taskName = ((TextView) recyclerView.findViewHolderForAdapterPosition(position)
                .itemView.findViewById(R.id.task_name)).getText().toString();
        if (!taskName.equals(expectedName))
        {
            result = false;
        }
        return result;
    }


    public static class ChildViewAction {

        public static ViewAction clickChildViewWithId(
                final int id) {
            return new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "Click on a child view with specified id.";
                }

                @Override
                public void perform(UiController uiController,
                                    View view) {
                    View v = view.findViewById(id);
                    v.performClick();
                }
            };
        }
    }

}

