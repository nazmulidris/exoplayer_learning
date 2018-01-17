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

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class AudioActivity : AppCompatActivity() {

    lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)
    }

    override fun onResume() {
        super.onResume()
        initPlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    fun initPlayer() {
        // Create the player
        val renderersFactory = DefaultRenderersFactory(this, null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        val trackSelector = DefaultTrackSelector()
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector)

        // Pick the media to play
        val userAgent = Util.getUserAgent(this, this.javaClass.simpleName)
        val uri = Uri.parse("asset:///cielo.mp3")
        val mediaSource = ExtractorMediaSource
                .Factory(DefaultDataSourceFactory(this, userAgent))
                .createMediaSource(uri)

        // Tell the player to play the media
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true
    }

    fun releasePlayer() = exoPlayer.release()

}
