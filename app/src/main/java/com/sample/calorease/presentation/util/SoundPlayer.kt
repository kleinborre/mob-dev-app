package com.sample.calorease.presentation.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * SoundPlayer — Lightweight utility for playing short UI sound effects.
 *
 * Sound files placed at:
 *   app/src/main/res/raw/sound_success.wav  (rising two-tone: A5 → C6)
 *   app/src/main/res/raw/sound_error.wav    (descending tone: A4 → A3)
 *
 * Degrades gracefully if files are missing (all play calls become no-ops).
 *
 * Usage:
 *   val soundPlayer = remember { SoundPlayer(context) }
 *   soundPlayer.playSuccess()
 *   soundPlayer.playError()
 *   // In DisposableEffect: soundPlayer.release()
 */
class SoundPlayer(private val context: Context) {

    var enabled: Boolean = true

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var successSoundId: Int = 0
    private var errorSoundId: Int   = 0
    private var loaded = false

    init {
        tryLoadSound()
    }

    private fun tryLoadSound() {
        try {
            val packageName = context.packageName
            val successRes  = context.resources.getIdentifier("sound_success", "raw", packageName)
            val errorRes    = context.resources.getIdentifier("sound_error",   "raw", packageName)

            if (successRes != 0) successSoundId = soundPool.load(context, successRes, 1)
            if (errorRes   != 0) errorSoundId   = soundPool.load(context, errorRes,   1)

            soundPool.setOnLoadCompleteListener { _, _, _ -> loaded = true }
        } catch (e: Exception) {
            android.util.Log.w("SoundPlayer", "Sounds not found, disabled: ${e.message}")
        }
    }

    /** Play a short pleasant ding for successful operations. */
    fun playSuccess() {
        if (enabled && loaded && successSoundId != 0) {
            soundPool.play(successSoundId, 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    /** Play a short soft buzz for error/failure operations. */
    fun playError() {
        if (enabled && loaded && errorSoundId != 0) {
            soundPool.play(errorSoundId, 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    /** Release the SoundPool. Call from DisposableEffect onDispose. */
    fun release() {
        soundPool.release()
    }
}
