package com.aieq.app

import android.app.Application
import com.aieq.app.ui.MainViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test

class ViewModelCreationTest {

    @Test
    fun verifyAndroidViewModelApplicationConstructorExistsForReflection() {
        val constructor = MainViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull("MainViewModel must expose (Application) constructor for ViewModelProvider reflection", constructor)
    }
}
