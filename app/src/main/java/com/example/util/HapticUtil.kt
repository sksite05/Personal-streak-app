package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtil {
    fun performHaptic(context: Context, type: HapticType, isEnabled: Boolean = true) {
        if (!isEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (type) {
                    HapticType.LIGHT -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    HapticType.MEDIUM -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    HapticType.HEAVY -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    HapticType.CELEBRATION -> {
                        val pattern = longArrayOf(0, 40, 60, 80, 60, 140)
                        val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            try {
                                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                            } catch (e: Exception) {
                                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                            }
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.LIGHT -> vibrator.vibrate(20)
                    HapticType.MEDIUM -> vibrator.vibrate(45)
                    HapticType.HEAVY -> vibrator.vibrate(80)
                    HapticType.CELEBRATION -> vibrator.vibrate(longArrayOf(0, 50, 70, 120), -1)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibrator unavailable
        }
    }
}

enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    CELEBRATION
}
