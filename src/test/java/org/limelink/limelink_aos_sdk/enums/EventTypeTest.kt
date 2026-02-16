package org.limelink.limelink_aos_sdk.enums

import org.junit.Assert.assertEquals
import org.junit.Test

class EventTypeTest {

    @Test
    fun `FIRST_RUN value is first_run`() {
        assertEquals("first_run", EventType.FIRST_RUN.value)
    }

    @Test
    fun `RERUN value is rerun`() {
        assertEquals("rerun", EventType.RERUN.value)
    }

    @Test
    fun `enum has exactly 2 entries`() {
        assertEquals(2, EventType.values().size)
    }
}
