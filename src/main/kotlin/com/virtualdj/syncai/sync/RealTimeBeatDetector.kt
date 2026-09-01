package com.virtualdj.syncai.sync

import kotlin.math.abs
import kotlin.math.sqrt

class RealTimeBeatDetector(private val sampleRate: Int = 44100) {
    
    private var beatGrid = mutableListOf<Long>()
    private var detectedBPM = 120f
    private var downbeatIndex = 0
    private val energyBands = FloatArray(12)
    
    fun detectBeats(audioBuffer: FloatArray, currentTimeMs: Long): BeatDetectionResult {
        val bands = analyzeFrequencyBands(audioBuffer)
        val onsets = detectOnsets(bands)
        val beatFound = detectBeatFromOnsets(onsets)
        
        if (beatFound) {
            updateBPM(currentTimeMs)
            beatGrid.add(currentTimeMs)
            
            if (beatGrid.size > 440) {
                beatGrid.removeAt(0)
            }
        }
        
        return BeatDetectionResult(
            beatDetected = beatFound,
            bpm = detectedBPM,
            beatGrid = beatGrid.toList(),
            energyBands = bands,
            downbeatIndex = downbeatIndex
        )
    }
    
    private fun analyzeFrequencyBands(audioBuffer: FloatArray): FloatArray {
        val bandWidth = audioBuffer.size / 12
        val bands = FloatArray(12)
        
        for (i in 0 until 12) {
            val start = i * bandWidth
            val end = minOf((i + 1) * bandWidth, audioBuffer.size)
            
            var energy = 0f
            for (j in start until end) {
                energy += audioBuffer[j] * audioBuffer[j]
            }
            
            bands[i] = sqrt(energy / (end - start))
        }
        
        return bands
    }
    
    private fun detectOnsets(currentBands: FloatArray): BooleanArray {
        val onsets = BooleanArray(12)
        val onsetThreshold = 1.5f
        
        for (i in currentBands.indices) {
            val previousEnergy = energyBands[i]
            val currentEnergy = currentBands[i]
            
            if (currentEnergy > previousEnergy * onsetThreshold) {
                onsets[i] = true
            }
        }
        
        for (i in currentBands.indices) {
            energyBands[i] = currentBands[i] * 0.95f
        }
        
        return onsets
    }
    
    private fun detectBeatFromOnsets(onsets: BooleanArray): Boolean {
        val kickBands = onsets.slice(0..3)
        val snarePredicateBands = onsets.slice(4..6)
        
        val kickOnsetCount = kickBands.count { it }
        val snareOnsetCount = snarePredicateBands.count { it }
        
        return kickOnsetCount >= 2 || (kickOnsetCount >= 1 && snareOnsetCount >= 1)
    }
    
    private fun updateBPM(currentTimeMs: Long) {
        if (beatGrid.size < 2) return
        
        val lastBeat = beatGrid[beatGrid.size - 1]
        val previousBeat = beatGrid[beatGrid.size - 2]
        
        val interval = lastBeat - previousBeat
        if (interval > 0) {
            val newBPM = (60000f / interval).coerceIn(80f, 200f)
            detectedBPM = detectedBPM * 0.8f + newBPM * 0.2f
        }
    }
}

data class BeatDetectionResult(
    val beatDetected: Boolean,
    val bpm: Float,
    val beatGrid: List<Long>,
    val energyBands: FloatArray,
    val downbeatIndex: Int
)
