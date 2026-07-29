package com.newbieeming.devkit.feature.systemquery.data

import com.newbieeming.devkit.feature.systemquery.model.SystemValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SystemValueDataSourceTest {

    @Test
    fun `parses getprop output`() {
        assertEquals(
            SystemValue("persist.sys.locale", "zh-CN"),
            parseGetpropLine("[persist.sys.locale]: [zh-CN]"),
        )
    }

    @Test
    fun `ignores malformed getprop output`() {
        assertNull(parseGetpropLine("persist.sys.locale=zh-CN"))
    }

    @Test
    fun `filters keys with regex alternatives and sorts them`() {
        val values = listOf(
            SystemValue("system_locales", "zh-CN"),
            SystemValue("ro.product.model", "Device"),
            SystemValue("persist.sys.locale", "zh-CN"),
        )

        assertEquals(
            listOf(
                SystemValue("persist.sys.locale", "zh-CN"),
                SystemValue("ro.product.model", "Device"),
            ),
            values.filterByKey(Regex("persist|ro\\.")),
        )
    }
}
