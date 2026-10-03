package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundUtil {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playSipSound(isEnabled: Boolean = true) {
        if (!isEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun playDoneSound(isEnabled: Boolean = true) {
        if (!isEnabled) return
        try {
            CoroutineScope(Dispatchers.Default).launch {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun playMilestoneCelebration(isEnabled: Boolean = true) {
        if (!isEnabled) return
        try {
            CoroutineScope(Dispatchers.Default).launch {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
