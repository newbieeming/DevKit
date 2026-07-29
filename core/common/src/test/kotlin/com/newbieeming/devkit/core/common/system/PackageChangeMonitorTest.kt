package com.newbieeming.devkit.core.common.system

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PackageChangeMonitorTest {
    @Test
    fun packageAddedEmitsInstalled() {
        assertEquals(
            PackageChangeEvent("com.example.new", PackageChangeType.INSTALLED),
            packageChangeEvent(
                action = Intent.ACTION_PACKAGE_ADDED,
                packageName = "com.example.new",
                isReplacing = false,
            ),
        )
    }

    @Test
    fun packageRemovedEmitsUninstalled() {
        assertEquals(
            PackageChangeEvent("com.example.old", PackageChangeType.UNINSTALLED),
            packageChangeEvent(
                action = Intent.ACTION_PACKAGE_REMOVED,
                packageName = "com.example.old",
                isReplacing = false,
            ),
        )
    }

    @Test
    fun replacingIntermediateBroadcastsAreIgnored() {
        assertNull(
            packageChangeEvent(
                action = Intent.ACTION_PACKAGE_REMOVED,
                packageName = "com.example.updated",
                isReplacing = true,
            ),
        )
        assertNull(
            packageChangeEvent(
                action = Intent.ACTION_PACKAGE_ADDED,
                packageName = "com.example.updated",
                isReplacing = true,
            ),
        )
    }

    @Test
    fun packageReplacedEmitsUpdated() {
        assertEquals(
            PackageChangeEvent("com.example.updated", PackageChangeType.UPDATED),
            packageChangeEvent(
                action = Intent.ACTION_PACKAGE_REPLACED,
                packageName = "com.example.updated",
                isReplacing = false,
            ),
        )
    }
}
