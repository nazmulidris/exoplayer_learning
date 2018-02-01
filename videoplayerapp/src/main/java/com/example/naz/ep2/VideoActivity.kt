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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_video.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn


class VideoActivity : AppCompatActivity(), AnkoLogger {

    lateinit var playerHolder: PlayerHolder
    val state = PlayerState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
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
        playerHolder = PlayerHolder(this, exoplayerview_activity_video, state)
        playerHolder.mPlayer.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        warn { "playback ended, show spinner" }
                    }
                    Player.STATE_READY -> when (playWhenReady) {
                        true -> {
                            warn { "playback started, hide spinner" }
                        }
                        false -> {
                            warn { "playback paused, show spinner" }
                        }
                    }
                }
            }
        })
    }

    fun releasePlayer() {
        playerHolder.release()
    }

}
