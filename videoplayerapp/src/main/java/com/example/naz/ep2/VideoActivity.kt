/*
 * Copyright 2018 Nazmul Idris. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.naz.ep2

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioRendererEventListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.activity_video.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class VideoActivity : AppCompatActivity(), AnkoLogger {

    lateinit var playerHolder: PlayerHolder
    val state = PlayerState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
    }

    override fun onResume() {
        super.onResume()
        playerHolder = PlayerHolder(this, exoplayerview_activity_video, state)
    }

    override fun onPause() {
        super.onPause()
        playerHolder.release()
    }
}

enum class Source {
    local_audio, local_video, http_audio, http_video
}

data class PlayerState(var window: Int = 0,
                       var position: Long = 0,
                       var whenReady: Boolean = true,
                       var source: Source = Source.local_video)

class PlayerHolder(val ctx: Context,
                   val playerView: SimpleExoPlayerView,
                   val state: PlayerState) : AnkoLogger {
    val player: SimpleExoPlayer =
            // Create the player
            ExoPlayerFactory.newSimpleInstance(ctx, DefaultTrackSelector())
                    .apply {
                        // Bind to the view
                        playerView.player = this
                        // Pick the media to play
                        val uri = selectMediaToPlay(state.source)
                        // Load media
                        prepare(buildMediaSource(uri))
                        // Start auto playback
                        playWhenReady = true
                        // Add logging (note, player hasn't been initialized yet, so passing this)
                        attachLogging(this)
                        // Restore state
                        with(state) {
                            playWhenReady = whenReady
                            seekTo(window, position)
                        }
                    }

    fun selectMediaToPlay(source: Source): Uri {
        return when (source) {
            Source.local_audio -> Uri.parse("asset:///audio/cielo.mp4")
            Source.local_video -> Uri.parse("asset:///video/stock_footage_video.mp4")
            Source.http_audio -> Uri.parse("http://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
            Source.http_video -> Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4")
        }
    }

    fun buildMediaSource(uri: Uri): ExtractorMediaSource {
        return ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(ctx, "exoplayer-learning")).createMediaSource(uri)
    }

    fun release() {
        with(player) {
            // Save state
            with(state) {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }
            // Release the player
            release()
        }
    }

    fun attachLogging(exoPlayer: SimpleExoPlayer) {
        // Attach logging
        exoPlayer.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                info { "playerStateChanged: ${getStateString(playbackState)}, $playWhenReady" }
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                info { "playerError: $error" }
            }

            fun getStateString(state: Int): String {
                when (state) {
                    Player.STATE_BUFFERING -> return "STATE_BUFFERING"
                    Player.STATE_ENDED -> return "STATE_ENDED"
                    Player.STATE_IDLE -> return "STATE_IDLE"
                    Player.STATE_READY -> return "STATE_READY"
                    else -> return "?"
                }
            }
        })

        exoPlayer.addAudioDebugListener(object : AudioRendererEventListener {
            override fun onAudioSinkUnderrun(bufferSize: Int, bufferSizeMs: Long, elapsedSinceLastFeedMs: Long) {
            }

            override fun onAudioEnabled(counters: DecoderCounters?) {
            }

            override fun onAudioInputFormatChanged(format: Format?) {
            }

            override fun onAudioSessionId(audioSessionId: Int) {
                info { "onAudioSessionId $audioSessionId" }
            }

            override fun onAudioDecoderInitialized(decoderName: String?, initializedTimestampMs: Long, initializationDurationMs: Long) {
            }

            override fun onAudioDisabled(counters: DecoderCounters?) {
            }
        })

        exoPlayer.addMetadataOutput {
            info { "metaDataOutput: $it" }
        }
    }
}