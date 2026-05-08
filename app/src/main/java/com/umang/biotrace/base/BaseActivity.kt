package com.umang.biotrace.base

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

abstract class BaseActivity : FragmentActivity() {

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            onPermissionResult?.invoke(permissions.values.all { it })
        }

    protected fun requestRuntimePermissions(onResult: (Boolean) -> Unit) {
        onPermissionResult = onResult
        permissionLauncher.launch(requiredPermissions())
    }

    protected fun replaceFragment(containerId: Int, fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
        if (addToBackStack) transaction.addToBackStack(fragment::class.java.simpleName)
        transaction.commit()
    }

    private fun requiredPermissions(): Array<String> {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return permissions.toTypedArray()
    }
}
