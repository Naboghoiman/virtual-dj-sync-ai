package com.virtualdj.syncai.sync

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*

class DjayController(private val context: Context) {
    
    private val audioAnalyzer = RealTimeBeatDetector()
    private val syncEngine = DjayBeatGridSync()
    private val aiPredictor = AIPredictor()
    
    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    
    private val syncListeners = mutableListOf<SyncListener>()
    private val trackMap = mutableMapOf<String, TrackBeatInfo>()
    private var masterTrackId = ""
    
    fun initialize() {
        initializeAudioRecord()
    }
    
    private fun initializeAudioRecord() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 2
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun startSync() {
        if (isRunning) return
        
        audioRecord?.startRecording()
        isRunning = true
        
        scope.launch {
            audioProcessingLoop()
        }
    }
    
    fun stopSync() {
        isRunning = false
        audioRecord?.stop()
    }
    
    private suspend fun audioProcessingLoop() = withContext(Dispatchers.Default) {
        val bufferSize = 2048
        val audioBuffer = FloatArray(bufferSize)
        val shortBuffer = ShortArray(bufferSize)
        
        while (isRunning) {
            val audioRecord = audioRecord ?: break
            
            if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = audioRecord.read(shortBuffer, 0, bufferSize)
                
                if (read > 0) {
                    for (i in 0 until read) {
                        audioBuffer[i] = shortBuffer[i] / 32768f
                    }
                    
                    val currentTime = System.currentTimeMillis()
                    val analysis = audioAnalyzer.detectBeats(audioBuffer, currentTime)
                    
                    if (analysis.beatDetected) {
                        aiPredictor.recordBeat(
                            BeatEvent(
                                timestamp = currentTime,
                                confidence = 0.8f,
                                energy = analysis.energyBands.average().toFloat()
                            )
                        )
                    }
                    
                    aiPredictor.recordTempo(
                        TempoEvent(
                            timestamp = currentTime,
                            tempo = analysis.bpm,
                            confidence = 0.7f
                        )
                    )
                    
                    val syncAnalysisResult = AudioAnalysisResult(
                        spectrum = analysis.energyBands,
                        energy = analysis.energyBands.average().toFloat(),
                        isBeat = analysis.beatDetected,
                        tempo = analysis.bpm,
                        timestamp = currentTime
                    )
                    
                    notifySyncUpdate(syncAnalysisResult)
                }
            }
            
            delay(10)
        }
    }
    
    fun addTrack(trackId: String, initialTempo: Float) {
        trackMap[trackId] = TrackBeatInfo(
            trackId = trackId,
            bpm = initialTempo,
            beatGrid = listOf(),
            downbeatPositions = listOf(),
            keyDetected = "C"
        )
    }
    
    fun setMasterTrack(trackId: String) {
        masterTrackId = trackId
    }
    
    fun getSyncStatus(): SyncStatus {
        return SyncStatus(
            masterTrackId = masterTrackId,
            trackCount = trackMap.size,
            averageTempo = trackMap.values.map { it.bpm }.average().toFloat(),
            isSynchronized = true
        )
    }
    
    fun addSyncListener(listener: SyncListener) {
        syncListeners.add(listener)
    }
    
    private fun notifySyncUpdate(analysis: AudioAnalysisResult) {
        val status = getSyncStatus()
        syncListeners.forEach { listener ->
            listener.onSyncUpdate(SyncUpdateEvent(
                analysis = analysis,
                adjustments = mapOf(),
                syncStatus = status,
                beatStability = aiPredictor.getBeatStability()
            ))
        }
    }
    
    fun destroy() {
        stopSync()
        audioRecord?.release()
        scope.cancel()
    }
}

data class AudioAnalysisResult(
    val spectrum: FloatArray,
    val energy: Float,
    val isBeat: Boolean,
    val tempo: Float,
    val timestamp: Long
)

data class SyncUpdateEvent(
    val analysis: AudioAnalysisResult,
    val adjustments: Map<String, Any>,
    val syncStatus: SyncStatus,
    val beatStability: Float
)

data class SyncStatus(
    val masterTrackId: String,
    val trackCount: Int,
    val averageTempo: Float,
    val isSynchronized: Boolean
)

interface SyncListener {
    fun onSyncUpdate(event: SyncUpdateEvent)
}

data class BeatEvent(
    val timestamp: Long,
    val confidence: Float,
    val energy: Float
)

data class TempoEvent(
    val timestamp: Long,
    val tempo: Float,
    val confidence: Float
)

data class TrackBeatInfo(
    val trackId: String,
    val bpm: Float,
    val beatGrid: List<Long>,
    val downbeatPositions: List<Int>,
    val keyDetected: String
)
