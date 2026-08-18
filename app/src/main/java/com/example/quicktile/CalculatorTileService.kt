package com.example.quicktile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * A custom Quick Settings tile that shows a calculator icon and, when tapped,
 * launches the device's default calculator app.
 *
 * The tile has no on/off toggle behavior of its own (it's a launcher, not a
 * setting), so it is always kept in [Tile.STATE_INACTIVE].
 */
class CalculatorTileService : TileService() {

    /** Called whenever the tile becomes visible in the Quick Settings panel. */
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    /** Called when the user taps the tile. */
    override fun onClick() {
        super.onClick()
        // If the device is locked, this prompts for the credential (PIN/pattern/password)
        // and then runs the block automatically once unlocked, instead of just dismissing
        // the keyguard and dropping the tap (which is the default behavior otherwise).
        unlockAndRun {
            // Starting the activity in the same instant the keyguard-dismiss animation
            // finishes can race with it on some devices, briefly showing a black frame.
            // Deferring by one frame lets that transition settle first.
            Handler(Looper.getMainLooper()).postDelayed({ launchCalculator() }, 300)
        }
    }

    /** Refreshes the tile's icon, label and state so it always reads "Calculator". */
    private fun updateTile() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.updateTile()
    }

    /** Resolves the device's calculator app and starts it, collapsing the Quick Settings panel. */
    private fun launchCalculator() {
        val launchIntent = resolveCalculatorIntent() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ (API 34) requires activities to be started from a tile via a
            // PendingIntent; the plain-Intent overload is disallowed and throws at runtime.
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @SuppressLint("StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(launchIntent)
        }
    }

    /**
     * Builds an intent that opens the device's default calculator app.
     *
     * Most devices advertise their calculator via [Intent.CATEGORY_APP_CALCULATOR]; for the
     * OEM skins that don't, we fall back to launching a few well-known calculator packages.
     */
    private fun resolveCalculatorIntent(): Intent? {
        val categoryIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_APP_CALCULATOR)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (categoryIntent.resolveActivity(packageManager) != null) {
            return categoryIntent
        }

        for (packageName in FALLBACK_CALCULATOR_PACKAGES) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return launchIntent
            }
        }

        return null
    }

    private companion object {
        // Known calculator app package names for OEMs that don't declare CATEGORY_APP_CALCULATOR.
        val FALLBACK_CALCULATOR_PACKAGES = listOf(
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator",
            "com.miui.calculator",
            "com.oneplus.calculator",
            "com.coloros.calculator",
            "com.huawei.calculator"
        )
    }
}
