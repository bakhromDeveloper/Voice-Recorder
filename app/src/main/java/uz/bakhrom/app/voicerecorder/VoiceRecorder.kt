package uz.bakhrom.app.voicerecorder

import android.content.Context
import android.media.MediaRecorder
import java.util.Timer
import java.util.TimerTask
import kotlin.math.log10

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: String? = null
    private var isRecording: Boolean = false

    private var amplitudeCheckTimer: Timer? = null
    var onAmplitudeChanged: ((db: Int) -> Unit)? = null

    fun startRecording() {
        outputFile = "${context.getExternalFilesDir(null)?.absolutePath}/recorded_audio.3gp"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile)

            prepare()
            start()
        }

        isRecording = true
        startAmplitudeMonitor()
    }

    fun stopRecording(): String? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        stopAmplitudeMonitor()
        return outputFile
    }

    private fun startAmplitudeMonitor() {
        amplitudeCheckTimer = Timer()
        amplitudeCheckTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                recorder?.maxAmplitude?.let { amp ->
                    // Amplitude 1..32767, convert to decibels (dB)
                    val db = if (amp > 0) (20 * log10(amp.toDouble())).toInt() else 0
                    onAmplitudeChanged?.invoke(db)
                }
            }
        }, 0, 200) // Har 200ms da yangilanish
    }

    private fun stopAmplitudeMonitor() {
        amplitudeCheckTimer?.cancel()
        amplitudeCheckTimer = null
    }
}