package uz.bakhrom.app.voicerecorder

import java.io.File
import java.io.FileOutputStream

class PcmToWavConverter {
    fun convert(pcmFile: File, wavFile: File, sampleRate: Int = 44100, channels: Int = 1, bitDepth: Int = 16) {
        val pcmData = pcmFile.readBytes()
        val wavOutput = FileOutputStream(wavFile)

        // Write WAV header
        val byteRate = sampleRate * channels * bitDepth / 8
        val blockAlign = channels * bitDepth / 8
        val dataSize = pcmData.size
        val chunkSize = 36 + dataSize

        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        writeInt(header, 4, chunkSize) // ChunkSize
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        writeInt(header, 16, 16) // Subchunk1Size for PCM
        writeShort(header, 20, 1) // AudioFormat = PCM
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, blockAlign.toShort())
        writeShort(header, 34, bitDepth.toShort())

        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        writeInt(header, 40, dataSize)

        wavOutput.write(header)
        wavOutput.write(pcmData)
        wavOutput.close()
    }

    private fun writeInt(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value and 0xff).toByte()
        data[offset + 1] = (value shr 8 and 0xff).toByte()
        data[offset + 2] = (value shr 16 and 0xff).toByte()
        data[offset + 3] = (value shr 24 and 0xff).toByte()
    }

    private fun writeShort(data: ByteArray, offset: Int, value: Short) {
        data[offset] = (value.toInt() and 0xff).toByte()
        data[offset + 1] = (value.toInt() shr 8 and 0xff).toByte()
    }
}