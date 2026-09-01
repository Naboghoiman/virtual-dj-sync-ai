package com.virtualdj.syncai

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.virtualdj.syncai.databinding.ActivityDeckBinding
import com.virtualdj.syncai.sync.DjayController
import com.virtualdj.syncai.sync.SyncListener
import com.virtualdj.syncai.sync.SyncUpdateEvent
import kotlinx.coroutines.launch

class DeckActivity : AppCompatActivity(), SyncListener {
    
    private lateinit var binding: ActivityDeckBinding
    private lateinit var djayController: DjayController
    private val mediaPlayers = mutableMapOf<String, MediaPlayer>()
    private var isPlaying = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityDeckBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeDJController()
        setupUI()
    }
    
    private fun initializeDJController() {
        djayController = DjayController(this)
        djayController.initialize()
        djayController.addSyncListener(this)
        
        // Add two decks
        djayController.addTrack("deck1", 120f)
        djayController.addTrack("deck2", 120f)
        djayController.setMasterTrack("deck1")
    }
    
    private fun setupUI() {
        // Deck 1 Controls
        binding.btnPlayDeck1.setOnClickListener {
            togglePlayback("deck1")
        }
        
        binding.seekBarDeck1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvTempolDeck1.text = "Tempo: ${80 + progress}% (${120 + (progress - 50)})"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Deck 2 Controls
        binding.btnPlayDeck2.setOnClickListener {
            togglePlayback("deck2")
        }
        
        binding.seekBarDeck2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvTempolDeck2.text = "Tempo: ${80 + progress}% (${120 + (progress - 50)})"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Sync Button
        binding.btnSync.setOnClickListener {
            performSync()
        }
        
        // Master Deck Selection
        binding.btnSetMaster1.setOnClickListener {
            djayController.setMasterTrack("deck1")
            binding.tvSyncStatus.text = "Master: Deck 1"
        }
        
        binding.btnSetMaster2.setOnClickListener {
            djayController.setMasterTrack("deck2")
            binding.tvSyncStatus.text = "Master: Deck 2"
        }
    }
    
    private fun togglePlayback(deckId: String) {
        if (isPlaying) {
            djayController.stopSync()
            binding.btnPlayDeck1.text = "▶ PLAY"
            binding.btnPlayDeck2.text = "▶ PLAY"
            isPlaying = false
        } else {
            djayController.startSync()
            binding.btnPlayDeck1.text = "⏸ PAUSE"
            binding.btnPlayDeck2.text = "⏸ PAUSE"
            isPlaying = true
        }
    }
    
    private fun performSync() {
        val status = djayController.getSyncStatus()
        lifecycleScope.launch {
            binding.tvSyncStatus.text = "Syncing...\nMaster: ${status.masterTrackId}\nTempo: ${String.format("%.1f", status.averageTempo)} BPM"
        }
    }
    
    override fun onSyncUpdate(event: SyncUpdateEvent) {
        lifecycleScope.launch {
            binding.tvSyncStatus.text = 
                "Synced!\n" +
                "Tempo: ${String.format("%.1f", event.syncStatus.averageTempo)} BPM\n" +
                "Stability: ${String.format("%.1f%%", event.beatStability * 100)}"
            
            // Update beat indicators
            if (event.analysis.isBeat) {
                binding.ivBeatIndicator1.setBackgroundColor(android.graphics.Color.GREEN)
                binding.ivBeatIndicator2.setBackgroundColor(android.graphics.Color.GREEN)
                
                binding.root.postDelayed({
                    binding.ivBeatIndicator1.setBackgroundColor(android.graphics.Color.GRAY)
                    binding.ivBeatIndicator2.setBackgroundColor(android.graphics.Color.GRAY)
                }, 100)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        djayController.stopSync()
        djayController.destroy()
        mediaPlayers.values.forEach { it.release() }
    }
}
