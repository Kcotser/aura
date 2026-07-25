package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.PinRepository
import com.example.myapplication.data.ProfileRepository

class AuraApplication : Application() {
    val profileRepository: ProfileRepository by lazy { ProfileRepository(this) }
    val pinRepository: PinRepository by lazy { PinRepository(this) }
}
