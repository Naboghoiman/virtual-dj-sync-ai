package com.virtualdj.syncai.sync

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

class AIPredictor {
    
    private val beatHistory = mutableListOf<BeatEvent>()
    private val tempoHistory = mutableListOf<TempoEvent>()
    private val maxHistorySize = 1000
    
    private var modelWeights = FloatArray(8) { 0.5f }
    private var learningRate = 0.01f
    
    fun recordBeat(beat: BeatEvent) {
        beatHistory.add(beat)
        if (beatHistory.size > maxHistorySize) {
            beatHistory.removeAt(0)
        }
    }
    
    fun recordTempo(tempo: TempoEvent) {
        tempoHistory.add(tempo)
        if (tempoHistory.size > maxHistorySize) {
            tempoHistory.removeAt(0)
        }
    }
    
    fun predictNextBeat(currentTime: Long): Long {
        if (beatHistory.size < 2) return currentTime + 500
        
        val recentBeats = beatHistory.takeLast(4)
        val intervals = mutableListOf<Long>()
        
        for (i in 1 until recentBeats.size) {
            intervals.add(recentBeats[i].timestamp - recentBeats[i - 1].timestamp)
        }
        
        val averageInterval = intervals.average().toLong()
        val lastBeat = recentBeats.last()
        
        return lastBeat.timestamp + averageInterval
    }
    
    fun getBeatStability(): Float {
        if (beatHistory.size < 2) return 0f
        
        val intervals = mutableListOf<Long>()
        for (i in 1 until beatHistory.size) {
            intervals.add(beatHistory[i].timestamp - beatHistory[i - 1].timestamp)
        }
        
        if (intervals.isEmpty()) return 0f
        
        val mean = intervals.average()
        val variance = intervals.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        val coefficientOfVariation = stdDev / mean
        
        return (1f - minOf(1f, coefficientOfVariation.toFloat())).coerceIn(0f, 1f)
    }
}
