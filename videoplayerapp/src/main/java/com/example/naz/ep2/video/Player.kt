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

package com.example.naz.ep2.video

import android.content.Context
import android.net.Uri
import com.example.naz.ep2.video.Source.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioRendererEventListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

enum class Source {
    local_audio, local_video, http_audio, http_video, playlist;
}

data class PlayerState(var window: Int = 0,
                       var position: Long = 0,
                       var whenReady: Boolean = true,
                       var source: Source = local_audio)

class PlayerHolder : AnkoLogger {
    val mContext: Context
    val mPlayerView: SimpleExoPlayerView
    val mState: PlayerState

    val mMediaMap: Map<Source, Uri>
    val mPlayer: SimpleExoPlayer

    constructor(context: Context, playerView: SimpleExoPlayerView, state: PlayerState) {
        mContext = context
        mPlayerView = playerView
        mState = state

        // List of media Uris that can be played
        mMediaMap = mapOf<Source, Uri>(
                local_audio to Uri.parse("asset:///audio/cielo.mp3"),
                local_video to Uri.parse("asset:///video/stock_footage_video.mp4"),
                http_audio to Uri.parse("http://storage.googleapis.com/exoplayer-test-media-0/play.mp3"),
                http_video to Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4")
        )

        // Create the player
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, DefaultTrackSelector())
                .apply {
                    // Bind to the view
                    playerView.player = this
                    // Load media
                    prepare(buildMediaSource(state.source))
                    // Start playback when media has buffered enough
                    playWhenReady = true
                    // Add logging (note, player hasn't been initialized yet, so passing this)
                    attachLogging(this)
                    // Restore state
                    with(state) {
                        playWhenReady = whenReady
                        seekTo(window, position)
                    }
                    warn { "SimpleExoPlayer created" }
                }
    }

    fun buildMediaSource(source: Source): MediaSource {
        return when (source) {
            playlist -> {
                return ConcatenatingMediaSource(
                        createExtractorMediaSource(local_audio),
                        createExtractorMediaSource(local_video),
                        createExtractorMediaSource(http_audio),
                        createExtractorMediaSource(http_video)
                )
            }
            else -> {
                return createExtractorMediaSource(source)            }
        }
    }

    private fun createExtractorMediaSource(source: Source): MediaSource {
        return ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(mContext,
                        "exoplayer-learning"))
                .createMediaSource(mMediaMap.get(source))
    }

    fun release() {
        with(mPlayer) {
            // Save state
            with(mState) {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }
            // Release the player
            release()
        }
        warn { "SimpleExoPlayer is released" }
    }

    fun attachLogging(exoPlayer: SimpleExoPlayer) {
        // Attach logging
        exoPlayer.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                warn { "playerStateChanged: ${getStateString(playbackState)}, $playWhenReady" }
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                warn { "playerError: $error" }
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
                warn { "onAudioSessionId $audioSessionId" }
            }

            override fun onAudioDecoderInitialized(decoderName: String?, initializedTimestampMs: Long, initializationDurationMs: Long) {
            }

            override fun onAudioDisabled(counters: DecoderCounters?) {
            }
        })

        exoPlayer.addMetadataOutput {
            warn { "metaDataOutput: $it" }
        }
    }

}