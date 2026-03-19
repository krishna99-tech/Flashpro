package com.example.flash

import android.app.Application
import com.example.flash.data.db.AppDatabase

class FlashApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}
