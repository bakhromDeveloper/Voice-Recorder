package uz.bakhrom.app.voicerecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var voiceRecorder: VoiceRecorder
    private lateinit var nVoiceRecorder: NoiseSuppressingVoiceRecorder
    private lateinit var btnRecord: Button
    private lateinit var btnStop: Button

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startRecording()
            } else {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        voiceRecorder = VoiceRecorder(this)
        nVoiceRecorder = NoiseSuppressingVoiceRecorder()

        btnRecord = findViewById(R.id.btnRecord)
        btnStop = findViewById(R.id.btnStop)

        btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                startRecording()
            }
        }

        btnStop.setOnClickListener {
//            val filePath = voiceRecorder.stopRecording()
            val filePath = nVoiceRecorder.stopRecording()
            Toast.makeText(this, "Saved: $filePath", Toast.LENGTH_SHORT).show()
            btnRecord.isEnabled = true
            btnStop.isEnabled = false

            val pcmFile = File(getExternalFilesDir(null), "recorded_audio.pcm")
            val wavFile = File(getExternalFilesDir(null), "recorded_audio.wav")

            val converter = PcmToWavConverter()
            converter.convert(pcmFile, wavFile)
            Toast.makeText(this, "Converted to: ${wavFile.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecording() {
//        voiceRecorder.startRecording()
        nVoiceRecorder.startRecording(this)
        btnRecord.isEnabled = false
        btnStop.isEnabled = true
    }
}