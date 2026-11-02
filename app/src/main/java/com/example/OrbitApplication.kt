package com.example

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class required as the Hilt component root.
 * Registered via `android:name=".OrbitApplication"` in AndroidManifest.xml.
 */
@HiltAndroidApp
class OrbitApplication : Application()
