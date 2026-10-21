package com.dessalines.thumbkey.effects

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskerKeyBindingsTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val task = TaskerTask("My user task", "net.dinglisch.android.taskerm")

    @Before
    fun reset() {
        context
            .getSharedPreferences("keywi_tasker_keys", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun selectedNamesPersistPerCellAndGestureAcrossMeAndHk() {
        val primary = TaskerKeyBinding(task)
        val secondary = TaskerKeyBinding(task.copy(name = "Second task"), TaskerBindingMode.SECONDARY)
        TaskerKeyBindings.save(context, "key0_0", "TOP", primary)
        TaskerKeyBindings.save(context, "hk0_0", "CENTER", secondary)
        assertEquals(primary, TaskerKeyBindings.get(context, "key0_0", "TOP"))
        assertEquals(secondary, TaskerKeyBindings.get(context, "hk0_0", "CENTER"))
        assertNull(TaskerKeyBindings.get(context, "key0_0", "CENTER"))
        assertNull(TaskerKeyBindings.get(context, "hk0_0", "TOP"))
        assertTrue(TaskerKeyBindings.reservesSwipe(context, "key0_0"))
        assertFalse(TaskerKeyBindings.reservesSwipe(context, "hk0_0"))
        TaskerKeyBindings.save(context, "key0_0", "TOP", null)
        assertNull(TaskerKeyBindings.get(context, "key0_0", "TOP"))
    }

    @Test
    fun mockReceiverGetsExactlyOneNamedTaskAndSecondaryPreservesNormalAction() {
        var received = 0
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    received++
                    assertEquals(TaskerIntegration.ACTION_RUN, intent.action)
                    assertEquals(task.name, intent.getStringExtra("task_name"))
                    assertEquals(task.packageName, intent.`package`)
                    assertEquals("1.1", intent.getStringExtra("version_number"))
                    assertEquals("id", intent.data?.scheme)
                    assertEquals(setOf("version_number", "task_name"), intent.extras?.keySet())
                }
            }
        TaskerKeyBindings.save(context, "key1_2", "BOTTOM", TaskerKeyBinding(task))
        assertFalse(TaskerKeyBindings.dispatchBinding(context, "key1_2", "TOP") { error("Wrong gesture") })
        assertTrue(
            TaskerKeyBindings.dispatchBinding(context, "key1_2", "BOTTOM") {
                receiver.onReceive(context, TaskerIntegration.executionIntent(it.task))
            },
        )
        assertEquals(1, received)
        TaskerKeyBindings.save(context, "key1_2", "BOTTOM", TaskerKeyBinding(task, TaskerBindingMode.SECONDARY))
        assertFalse(
            TaskerKeyBindings.dispatchBinding(context, "key1_2", "BOTTOM") {
                receiver.onReceive(context, TaskerIntegration.executionIntent(it.task))
            },
        )
        assertEquals(2, received)
    }

    @Test
    fun replacementStillConsumesKeyWhenTaskerIsMissing() {
        TaskerKeyBindings.save(context, "hk2_1", "HOLD", TaskerKeyBinding(task))
        assertEquals(TaskerIntegration.Status.MISSING, TaskerIntegration.execute(context, task))
        assertTrue(TaskerKeyBindings.dispatch(context, "hk2_1", "HOLD"))
    }

    @Test
    fun namesAreDataNeverParsedAsIntentsAndUntrustedPackagesAreRejected() {
        val intentName = task.copy(name = "intent:#Intent;action=evil;end")
        val intent = TaskerIntegration.executionIntent(intentName)
        assertEquals(TaskerIntegration.ACTION_RUN, intent.action)
        assertEquals(intentName.name, intent.getStringExtra("task_name"))
        assertEquals("id", intent.data?.scheme)
        assertFalse(TaskerIntegration.valid(task.copy(packageName = "other.package")))
        assertFalse(TaskerIntegration.valid(task.copy(name = "\n")))
        assertFalse(TaskerIntegration.valid(task.copy(name = "x".repeat(257))))
        assertEquals(TaskerIntegration.ACTION_SELECT, TaskerIntegration.selectionIntent(task.packageName).action)
        assertEquals(task.packageName, TaskerIntegration.selectionIntent(task.packageName).`package`)
        assertEquals(3, TaskerIntegration.externalAccessIntent(task.packageName).getIntExtra("tno", -1))
    }
}
