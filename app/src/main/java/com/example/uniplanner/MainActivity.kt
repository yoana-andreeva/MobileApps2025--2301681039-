package com.example.uniplanner

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.uniplanner.databinding.ActivityMainBinding
import com.example.uniplanner.ui.settings.SettingsBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)



        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.addEditTaskFragment) {
                // Скриваме контейнера с анимация или директно
                binding.bottomNavContainer.visibility = View.GONE
            } else {
                // Показваме го на основните 3 екрана
                binding.bottomNavContainer.visibility = View.VISIBLE
            }
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    SettingsBottomSheet().show(
                        supportFragmentManager,
                        SettingsBottomSheet.TAG
                    )
                    false
                }
                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }
}