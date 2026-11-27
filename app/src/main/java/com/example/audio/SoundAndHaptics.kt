package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface SoundEffectsController {
    fun playTap()
    fun playTooHigh()
    fun playTooLow()
    fun playHint()
    fun playError()
    fun playWin()
    fun playLose()
}

interface BackgroundMusicController {
    fun setEnabled(enabled: Boolean)
    fun onAppForeground()
    fun onAppBackground()
    fun release()
}

interface HapticController {
    fun tap()
    fun hint()
    fun win()
    fun lose()
}

object NoOpSoundEffectsController : SoundEffectsController {
    override fun playTap() {}
    override fun playTooHigh() {}
    override fun playTooLow() {}
    override fun playHint() {}
    override fun playError() {}
    override fun playWin() {}
    override fun playLose() {}
}

object NoOpBackgroundMusicController : BackgroundMusicController {
    override fun setEnabled(enabled: Boolean) {}
    override fun onAppForeground() {}
    override fun onAppBackground() {}
    override fun release() {}
}

object NoOpHapticController : HapticController {
    override fun tap() {}
    override fun hint() {}
    override fun win() {}
    override fun lose() {}
}

class SynthesizedSoundEffectsController : SoundEffectsController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 22050

    override fun playTap() {
        playNotes(listOf(660.0 to 45), amplitude = 0.28)
    }

    override fun playTooHigh() {
        playNotes(listOf(523.25 to 75, 392.0 to 110), amplitude = 0.35)
    }

    override fun playTooLow() {
        playNotes(listOf(392.0 to 75, 523.25 to 110), amplitude = 0.35)
    }

    override fun playHint() {
        playNotes(listOf(587.33 to 65, 739.99 to 65, 880.0 to 120), amplitude = 0.32)
    }

    override fun playError() {
        playNotes(listOf(220.0 to 90, 185.0 to 120), amplitude = 0.32)
    }

    override fun playWin() {
        playNotes(
            listOf(523.25 to 90, 659.25 to 90, 783.99 to 90, 1046.50 to 240),
            amplitude = 0.42
        )
    }

    override fun playLose() {
        playNotes(
            listOf(392.0 to 130, 349.23 to 130, 311.13 to 140, 261.63 to 260),
            amplitude = 0.35
        )
    }

    private fun playNotes(notes: List<Pair<Double, Int>>, amplitude: Double) {
        scope.launch {
            runCatching {
                val totalSamples = notes.sumOf { (_, ms) -> (sampleRate * ms) / 1000 }
                if (totalSamples <= 0) return@runCatching
                val pcm = ShortArray(totalSamples)
                var offset = 0
                for ((freq, durationMs) in notes) {
                    val count = (sampleRate * durationMs) / 1000
                    for (i in 0 until count) {
                        val t = i.toDouble() / sampleRate
                        val progress = i.toDouble() / count.coerceAtLeast(1)
                        val envelope = exp(-3.8 * progress) * (1.0 - progress * 0.2)
                        val sample = (sin(2.0 * PI * freq * t) * amplitude * envelope * Short.MAX_VALUE)
                        if (offset + i < pcm.size) {
                            pcm[offset + i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                    }
                    offset += count
                }
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                if (minBuf <= 0) return@runCatching
                val byteCount = pcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBuf, byteCount))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(pcm, 0, pcm.size)
                track.play()
                val totalMs = notes.sumOf { it.second }.toLong()
                delay(totalMs + 60L)
                track.stop()
                track.release()
            }
        }
    }
}

class SynthesizedMusicController : BackgroundMusicController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var loopJob: Job? = null
    private var isEnabled = false
    private var isForeground = true
    private val sampleRate = 22050

    // Gentle upbeat puzzle arpeggio notes (Hz)
    private val melody = listOf(
        261.63, 329.63, 392.00, 523.25,
        293.66, 349.23, 440.00, 587.33,
        261.63, 329.63, 392.00, 493.88,
        246.94, 293.66, 392.00, 440.00
    )

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        updatePlayback()
    }

    override fun onAppForeground() {
        isForeground = true
        updatePlayback()
    }

    override fun onAppBackground() {
        isForeground = false
        updatePlayback()
    }

    override fun release() {
        isEnabled = false
        loopJob?.cancel()
        loopJob = null
    }

    private fun updatePlayback() {
        if (isEnabled && isForeground) {
            if (loopJob?.isActive == true) return
            loopJob = scope.launch {
                runCatching {
                    val noteMs = 340
                    val samplesPerNote = (sampleRate * noteMs) / 1000
                    val totalSamples = samplesPerNote * melody.size
                    val pcm = ShortArray(totalSamples)
                    melody.forEachIndexed { idx, freq ->
                        val base = idx * samplesPerNote
                        for (i in 0 until samplesPerNote) {
                            val t = i.toDouble() / sampleRate
                            val progress = i.toDouble() / samplesPerNote
                            val attack = (progress * 10.0).coerceAtMost(1.0)
                            val decay = exp(-2.8 * progress)
                            val wave = 0.7 * sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * freq * 0.5 * t)
                            pcm[base + i] = (wave * 0.08 * attack * decay * Short.MAX_VALUE)
                                .toInt()
                                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                                .toShort()
                        }
                    }

                    val minBuf = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    if (minBuf <= 0) return@runCatching
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setSampleRate(sampleRate)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    track.play()
                    try {
                        while (isActive && isEnabled && isForeground) {
                            track.write(pcm, 0, pcm.size)
                        }
                    } finally {
                        runCatching {
                            track.stop()
                            track.release()
                        }
                    }
                }
            }
        } else {
            loopJob?.cancel()
            loopJob = null
        }
    }
}

class AndroidHapticController(context: Context) : HapticController {
    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    override fun tap() {
        vibrateOneShot(22L, 90)
    }

    override fun hint() {
        vibrateOneShot(40L, 140)
    }

    override fun win() {
        runCatching {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 70, 110), intArrayOf(0, 180, 0, 255), -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 60, 70, 110), -1)
            }
        }
    }

    override fun lose() {
        vibrateOneShot(180L, 170)
    }

    private fun vibrateOneShot(durationMs: Long, amplitude: Int) {
        runCatching {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        }
    }
}
