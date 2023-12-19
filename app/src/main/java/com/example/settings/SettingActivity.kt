package com.example.settings

import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.settings.databinding.ActivitySettingBinding
import com.example.settings.model.SettingModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

//Patron Singleton para instanciar una vez a la base de datos
val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class SettingActivity : AppCompatActivity() {
    companion object {
        const val VOLUME_LVL = "volume_lvl"
        const val KEY_BUETOOTH = "key_bluetooth"
        const val KEY_VIBRATION = "key_vibration"
        const val KEY_DARK_MODE = "key_dark_mode"
    }

    private lateinit var binding: ActivitySettingBinding
    private var firstTime: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firstTime }.collect { settingModel ->
                if (settingModel != null) {
                    runOnUiThread {
                        binding.switchVibration.isChecked = settingModel.vibration
                        binding.switchBluetooth.isChecked = settingModel.bluetooth
                        binding.switchDarkMode.isChecked = settingModel.darkMode
                        binding.rsVolumen.setValues(settingModel.volume.toFloat())
                        firstTime = !firstTime
                    }
                }
            }
        }
        initUI()
    }

    private fun initUI() {
        binding.rsVolumen.addOnChangeListener { _, value, _ ->
            Log.i("Javicia", "El valor es $value")
            //lanzamos corrutina
            CoroutineScope(Dispatchers.IO).launch {
                saveVol(value.toInt())
            }
        }
        binding.switchBluetooth.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_BUETOOTH, value)
            }
        }
        binding.switchVibration.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_VIBRATION, value)
            }
            binding.switchDarkMode.setOnCheckedChangeListener { _, value ->
                if(value){
                    enableDarkMode()
                }else{
                    disenableDarkMode()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    saveOptions(KEY_DARK_MODE, value)
                }
            }
        }
    }

    private suspend fun saveVol(value: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(VOLUME_LVL)] = value
        }
    }

    private suspend fun saveOptions(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    private fun getSettings(): Flow<SettingModel> {
        return dataStore.data.map { preferences ->
            SettingModel(
                volume = preferences[intPreferencesKey(VOLUME_LVL)] ?: 50,
                bluetooth = preferences[booleanPreferencesKey(KEY_BUETOOTH)] ?: true,
                vibration = preferences[booleanPreferencesKey(KEY_VIBRATION)] ?: true,
                darkMode = preferences[booleanPreferencesKey(KEY_DARK_MODE)] ?: false
            )
        }
    }

    private fun enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    private fun disenableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()

    }
}