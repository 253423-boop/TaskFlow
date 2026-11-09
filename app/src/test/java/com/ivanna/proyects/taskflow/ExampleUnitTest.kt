package com.ivanna.proyects.taskflow

import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun taskDomainEnumsMapLevelsAndApiValuesWithSafeDefaults() {
        assertEquals(TaskPriority.BAJA, TaskPriority.fromLevel(1))
        assertEquals(TaskPriority.MEDIA, TaskPriority.fromLevel(2))
        assertEquals(TaskPriority.ALTA, TaskPriority.fromLevel(3))
        assertEquals(TaskPriority.MEDIA, TaskPriority.fromLevel(99))
        assertEquals(TaskPriority.MEDIA, TaskPriority.fromLevel(null))

        assertEquals(TaskStatus.COMPLETADA, TaskStatus.fromApiValue(" COMPLETADA "))
        assertEquals(TaskStatus.PENDIENTE, TaskStatus.fromApiValue("pendiente"))
        assertEquals(TaskStatus.PENDIENTE, TaskStatus.fromApiValue(null))
    }
}
