package com.example.m3ewidgets

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.PermissionController

/** Setup screen: grant location + Health Connect access, then sync. */
class MainActivity : ComponentActivity() {

    private val locationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Weather.cacheNow(this)
            SyncWorker.syncNow(this)
        }

    private val healthLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) {
            SyncWorker.syncNow(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }
        root.addView(TextView(this).apply {
            text = "M3E Widgets"
            textSize = 28f
        })
        root.addView(TextView(this).apply {
            text = "1. Allow access below\n2. Long-press your home screen → Widgets → M3E Widgets"
            setPadding(0, pad / 2, 0, pad / 2)
        })

        fun button(label: String, onClick: () -> Unit) {
            root.addView(Button(this).apply {
                text = label
                setOnClickListener { onClick() }
            })
        }

        button("Allow location (weather)") {
            locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        button("Allow Health Connect (steps, heart, sleep)") {
            if (Health.isAvailable(this)) {
                healthLauncher.launch(Health.PERMISSIONS)
            } else {
                Toast.makeText(this, "Health Connect isn't available on this device", Toast.LENGTH_LONG).show()
            }
        }
        button("Sync now") { SyncWorker.syncNow(this) }

        setContentView(root)
        SyncWorker.schedule(this)
    }

    override fun onResume() {
        super.onResume()
        Weather.cacheNow(this)
    }
}
