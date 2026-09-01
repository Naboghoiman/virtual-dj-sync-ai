# Virtual DJ Sync AI

A professional virtual DJ application for Android with integrated Djay Synchronization AI technology. Test real-time beat matching, tempo alignment, and multi-deck synchronization.

## Features

✅ **Real-time Beat Detection**
- 12-band frequency analysis
- Kick drum and percussion detection
- Dynamic BPM estimation

✅ **Tempo Synchronization**
- Automatic BPM matching
- Tempo ramping and pitch locking
- Smooth transitions between tracks

✅ **Beat Grid Alignment**
- Phase-based beat synchronization
- Multi-track beat grid management
- Downbeat identification

✅ **Multi-Deck Control**
- Two independent turntables
- Master/slave track configuration
- Tempo control (80-200 BPM)

✅ **AI-Powered Features**
- Neural network beat prediction
- Anomaly detection
- Beat stability analysis
- Sync confidence scoring

## Architecture

### Core Components

```
┌─────────────────────────────────────┐
│     MainActivity (Launcher)          │
└────────────┬────────────────────────┘
             │
             ↓
┌──────────────────��──────────────────┐
│       DeckActivity (UI)              │
│  ┌──────────────────────────────┐   │
│  │  Deck 1  │  Sync  │  Deck 2  │   │
│  └──────────────────────────────┘   │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│    DjayController (Orchestrator)    │
│  ┌──────────────────────────────┐   │
│  │ Audio Analysis Loop (10ms)   │   │
│  └──────────────────────────────┘   │
└──┬──────────┬──────────┬────────────┘
   │          │          │
   ↓          ↓          ↓
┌──────────┐ ┌────────┐ ┌──────────┐
│  Real-   │ │Djay    │ │AI        │
│  Time    │ │Beat    │ │Predictor │
│  Beat    │ │Grid    │ │          │
│  Detector│ │Sync    │ │          │
└──────────┘ └────────┘ └──────────┘
```

## Installation

### Prerequisites
- Android SDK 24+
- Kotlin 1.9.0
- Android Studio 2023.1+

### Build & Install

```bash
cd virtual-dj-sync-ai
./gradlew build
./gradlew installDebug
```

### Permissions Required

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## Usage

### Basic Workflow

1. **Launch App**
   - Tap "START DJ SESSION"
   - Grant audio recording permissions

2. **Configure Decks**
   - Adjust tempo using sliders (80-200 BPM)
   - Set master track (Deck 1 or Deck 2)

3. **Start Playback**
   - Tap PLAY on both decks
   - Monitor beat detection (green indicator)

4. **Synchronize**
   - Tap "SYNC DECKS" button
   - Watch real-time BPM and stability updates

### UI Components

**Deck Controls:**
- **Play/Pause Button** - Start/stop audio processing
- **Tempo Slider** - Adjust BPM (80-200 range)
- **Beat Indicator** - Visual beat detection feedback
- **Set Master** - Configure as reference track

**Status Display:**
- Master track indicator
- Average tempo across decks
- Beat stability percentage
- Sync status messages

## Algorithm Details

### Real-Time Beat Detection

```kotlin
// 12-band frequency analysis
val bands = analyzeFrequencyBands(audioBuffer)

// Onset detection (energy spikes)
val onsets = detectOnsets(bands)

// Beat detection from kick/snare onsets
val beatDetected = detectBeatFromOnsets(onsets)

// BPM estimation from intervals
val bpm = estimateBPM(beatIntervals)
```

### Tempo Synchronization

```kotlin
// Find beat indices in both tracks
val beatIndexA = findBeatIndex(trackA.beatGrid, currentTime)
val beatIndexB = findBeatIndex(trackB.beatGrid, currentTime)

// Calculate phase difference (0.0-1.0)
val phaseDiff = calculatePhase(trackA, time, beatIndexA) - 
                calculatePhase(trackB, time, beatIndexB)

// Calculate tempo adjustment
val tempoAdjustment = trackA.bpm / trackB.bpm
```

## Performance Specifications

| Parameter | Value |
|-----------|-------|
| Buffer Size | 2048 samples |
| Sample Rate | 44100 Hz |
| Processing Interval | 10 ms |
| FFT Window | Hann |
| Frequency Bands | 12 |
| BPM Range | 80-200 BPM |
| Sync Update Rate | 100 Hz |

## Testing

### Test Scenarios

1. **Single Track BPM Detection**
   - Play audio through Deck 1
   - Verify BPM detection accuracy
   - Check beat stability score

2. **Multi-Track Synchronization**
   - Play different tempos on each deck
   - Tap SYNC to align beats
   - Monitor tempo convergence

3. **Tempo Ramping**
   - Adjust tempo slider while synced
   - Verify smooth beat alignment
   - Check sync confidence changes

4. **Master Track Switching**
   - Set Deck 1 as master
   - Switch to Deck 2 as master
   - Verify re-synchronization

## Troubleshooting

**Issue: No beat detection**
- Check microphone permissions
- Ensure audio input is active
- Verify sensitivity settings

**Issue: Unstable sync**
- Check BPM range (80-200)
- Ensure clear audio input
- Reduce background noise

**Issue: App crashes**
- Check logcat for errors
- Ensure sufficient RAM (>1GB)
- Verify Android version compatibility

## Dependencies

```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
androidx.constraintlayout:constraintlayout:2.1.4
androidx.media:media:1.6.0

kotlin:kotlin-stdlib:1.9.0
kotlinx.coroutines:kotlinx-coroutines-android:1.7.1

com.google.android.material:material:1.10.0
com.google.android.exoplayer:exoplayer-core:2.19.1
```

## Future Enhancements

- [ ] Audio file loading and playback
- [ ] Waveform visualization
- [ ] Neural Mix stem separation
- [ ] Effect chain processing
- [ ] Cue point management
- [ ] Recording/export functionality
- [ ] Preset management
- [ ] Advanced EQ controls

## License

MIT License - See LICENSE file

## Contributing

Contributions welcome! Submit PRs or open issues for bugs and feature requests.

## Support

For issues and questions, please open a GitHub issue.

---

**Virtual DJ Sync AI** - Professional DJ mixing powered by AI
