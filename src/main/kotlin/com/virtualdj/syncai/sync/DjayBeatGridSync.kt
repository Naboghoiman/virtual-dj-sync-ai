package com.virtualdj.syncai.sync

import kotlin.math.abs

class DjayBeatGridSync {
    
    fun syncTracks(
        trackA: TrackBeatInfo,
        trackB: TrackBeatInfo,
        currentTimeMs: Long
    ): BeatGridSyncResult {
        val beatIndexA = findBeatIndex(trackA.beatGrid, currentTimeMs)
        val beatIndexB = findBeatIndex(trackB.beatGrid, currentTimeMs)
        
        val phaseA = calculatePhase(trackA, currentTimeMs, beatIndexA)
        val phaseB = calculatePhase(trackB, currentTimeMs, beatIndexB)
        
        val phaseDifference = phaseA - phaseB
        val tempoAdjustment = calculateTempoAdjustment(trackA, trackB)
        val seekPosition = calculateSeekPosition(trackB.beatGrid, beatIndexA, trackB.bpm)
        
        return BeatGridSyncResult(
            phaseDifference = phaseDifference,
            tempoAdjustment = tempoAdjustment,
            seekPosition = seekPosition,
            syncConfidence = calculateSyncConfidence(trackA, trackB),
            beatIndexAlignment = beatIndexA - beatIndexB
        )
    }
    
    private fun findBeatIndex(beatGrid: List<Long>, currentTimeMs: Long): Int {
        var closestIndex = 0
        var minDifference = Long.MAX_VALUE
        
        for ((index, beatTime) in beatGrid.withIndex()) {
            val difference = abs(beatTime - currentTimeMs)
            if (difference < minDifference) {
                minDifference = difference
                closestIndex = index
            }
        }
        
        return closestIndex
    }
    
    private fun calculatePhase(
        track: TrackBeatInfo,
        currentTimeMs: Long,
        beatIndex: Int
    ): Float {
        if (beatIndex >= track.beatGrid.size) return 0f
        
        val beatTime = track.beatGrid[beatIndex]
        val nextBeatTime = if (beatIndex + 1 < track.beatGrid.size) {
            track.beatGrid[beatIndex + 1]
        } else {
            beatTime + (60000 / track.bpm).toLong()
        }
        
        val beatInterval = nextBeatTime - beatTime
        val timeSinceBeat = currentTimeMs - beatTime
        
        return if (beatInterval > 0) {
            ((timeSinceBeat % beatInterval).toFloat() / beatInterval).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    private fun calculateTempoAdjustment(trackA: TrackBeatInfo, trackB: TrackBeatInfo): Float {
        val bpmRatio = trackA.bpm / trackB.bpm
        return bpmRatio.coerceIn(0.85f, 1.15f)
    }
    
    private fun calculateSeekPosition(
        beatGrid: List<Long>,
        targetBeatIndex: Int,
        trackBPM: Float
    ): Long {
        if (targetBeatIndex < beatGrid.size) {
            return beatGrid[targetBeatIndex]
        }
        
        if (beatGrid.isNotEmpty()) {
            val lastBeat = beatGrid.last()
            val beatInterval = (60000 / trackBPM).toLong()
            val extrapolatedBeats = targetBeatIndex - beatGrid.size + 1
            return lastBeat + (extrapolatedBeats * beatInterval)
        }
        
        return 0L
    }
    
    private fun calculateSyncConfidence(trackA: TrackBeatInfo, trackB: TrackBeatInfo): Float {
        val bpmDifference = abs(trackA.bpm - trackB.bpm)
        val maxDeviation = 20f
        return (1f - (bpmDifference / maxDeviation)).coerceIn(0f, 1f)
    }
}

data class BeatGridSyncResult(
    val phaseDifference: Float,
    val tempoAdjustment: Float,
    val seekPosition: Long,
    val syncConfidence: Float,
    val beatIndexAlignment: Int
)
