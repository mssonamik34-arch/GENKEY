package com.genkey.keyboard

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast

class MicPermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 7)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val ok = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        val msg = if (ok) "অনুমতি পাওয়া গেছে, আবার 🎤 চাপো" else "অনুমতি ছাড়া ভয়েস টাইপিং হবে না"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        finish()
    }
}
