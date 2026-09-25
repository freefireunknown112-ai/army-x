package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val pcmCache = ConcurrentHashMap<String, ShortArray>()
    var soundVolume: Float = 0.8f
    var musicVolume: Float = 0.6f

    init {
        // Pre-render procedural tactical audio waveform buffers
        preloadSound("shoot_ar", generateGunshotWave(sampleRate = 22050, durationMs = 120, baseFreq = 180f, decay = 18f, noiseMix = 0.7f))
        preloadSound("shoot_pistol", generateGunshotWave(sampleRate = 22050, durationMs = 90, baseFreq = 220f, decay = 25f, noiseMix = 0.6f))
        preloadSound("shoot_sniper", generateGunshotWave(sampleRate = 22050, durationMs = 280, baseFreq = 110f, decay = 8f, noiseMix = 0.85f))
        preloadSound("shoot_shotgun", generateGunshotWave(sampleRate = 22050, durationMs = 180, baseFreq = 130f, decay = 12f, noiseMix = 0.9f))
        preloadSound("hit", generateToneWave(sampleRate = 22050, durationMs = 60, freq = 920f))
        preloadSound("headshot", generateToneWave(sampleRate = 22050, durationMs = 110, freq = 1450f))
        preloadSound("reload", generateMechanicalClick(sampleRate = 22050, durationMs = 160))
        preloadSound("footstep", generateFootstep(sampleRate = 22050, durationMs = 70))
        preloadSound("explosion", generateExplosion(sampleRate = 22050, durationMs = 650))
        preloadSound("click", generateToneWave(sampleRate = 22050, durationMs = 35, freq = 800f))
        preloadSound("victory", generateVictoryFanfare(sampleRate = 22050))
    }

    private fun preloadSound(key: String, pcm: ShortArray) {
        pcmCache[key] = pcm
    }

    fun playSound(name: String) {
        if (soundVolume <= 0.05f) return
        val samples = pcmCache[name] ?: return
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(22050)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.setVolume(soundVolume)
                track.write(samples, 0, samples.size)
                track.play()
                // Auto cleanup after completion
                launch {
                    kotlinx.coroutines.delay((samples.size * 1000L / 22050L) + 80L)
                    try {
                        track.stop()
                        track.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    private fun generateGunshotWave(sampleRate: Int, durationMs: Int, baseFreq: Float, decay: Float, noiseMix: Float): ShortArray {
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val random = java.util.Random(42)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = kotlin.math.exp(-decay * t)
            val sine = sin(2.0 * PI * (baseFreq * (1f - t * 2f).coerceAtLeast(0.3f)) * t).toFloat()
            val noise = (random.nextFloat() * 2f - 1f)
            val sample = ((1f - noiseMix) * sine + noiseMix * noise) * envelope
            buffer[i] = (sample.coerceIn(-1f, 1f) * 32000).toInt().toShort()
        }
        return buffer
    }

    private fun generateToneWave(sampleRate: Int, durationMs: Int, freq: Float): ShortArray {
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = (1f - i.toFloat() / totalSamples)
            val sine = sin(2.0 * PI * freq * t).toFloat()
            buffer[i] = (sine * envelope * 24000).toInt().toShort()
        }
        return buffer
    }

    private fun generateMechanicalClick(sampleRate: Int, durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val random = java.util.Random(10)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val pop = if (i % 400 < 50) 0.8f else 0.1f
            val sample = (random.nextFloat() * 2f - 1f) * pop * (1f - t)
            buffer[i] = (sample * 25000).toInt().toShort()
        }
        return buffer
    }

    private fun generateFootstep(sampleRate: Int, durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val random = java.util.Random(99)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val envelope = sin(PI * t).toFloat()
            val noise = (random.nextFloat() * 2f - 1f) * envelope * 0.4f
            buffer[i] = (noise * 20000).toInt().toShort()
        }
        return buffer
    }

    private fun generateExplosion(sampleRate: Int, durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val random = java.util.Random(7)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = kotlin.math.exp(-4.5f * t)
            val rumble = sin(2.0 * PI * (65f * (1f - t * 0.5f)) * t).toFloat() * 0.5f
            val noise = (random.nextFloat() * 2f - 1f) * 0.7f
            val sample = (rumble + noise) * envelope
            buffer[i] = (sample.coerceIn(-1f, 1f) * 32000).toInt().toShort()
        }
        return buffer
    }

    private fun generateVictoryFanfare(sampleRate: Int): ShortArray {
        val durationMs = 1200
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val notes = floatArrayOf(440f, 554.37f, 659.25f, 880f) // A chord triumphant
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val noteIdx = ((t / 0.3f).toInt()).coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val envelope = kotlin.math.exp(-2.0f * (t % 0.3f))
            val sine = sin(2.0 * PI * freq * t).toFloat()
            buffer[i] = (sine * envelope * 22000).toInt().toShort()
        }
        return buffer
    }
}
