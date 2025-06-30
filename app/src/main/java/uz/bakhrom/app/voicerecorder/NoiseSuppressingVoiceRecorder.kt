package uz.bakhrom.app.voicerecorder

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import androidx.annotation.RequiresPermission
import java.io.File
import java.io.FileOutputStream

class NoiseSuppressingVoiceRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var outputFile: File? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(context: Context) {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        // 🔇 Noise suppressor qo‘shamiz
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(audioRecord!!.audioSessionId)
        }

        outputFile = File(context.getExternalFilesDir(null), "recorded_audio.pcm")

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread {
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(bufferSize)
            while (isRecording) {
                val read = audioRecord!!.read(buffer, 0, buffer.size)
                if (read > 0) {
                    outputStream.write(buffer, 0, read)
                }
            }
            outputStream.close()
        }
        recordingThread?.start()
    }

    fun stopRecording(): String? {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
        return outputFile?.absolutePath
    }
}